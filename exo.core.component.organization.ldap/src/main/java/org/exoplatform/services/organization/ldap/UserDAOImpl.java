/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.organization.ldap;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.ldap.ObjectClassAttribute;
import org.exoplatform.services.organization.CacheHandler;
import org.exoplatform.services.organization.CacheHandler.CacheType;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserEventListenerHandler;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.security.PermissionConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SchemaViolationException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 14, 2005. @version andrew00x $
 */
@Deprecated
public class UserDAOImpl extends BaseDAO implements UserHandler, UserEventListenerHandler
{

   /**
    * AD user's account controls attribute for disabled account.
    */
   static final int UF_ACCOUNTDISABLE = 0x0002;

   /**
    * User event listeners.
    * 
    * @see org.exoplatform.services.organization.UserEventListener.
    */
   private final List<UserEventListener> listeners = new ArrayList<UserEventListener>(5);

   /**
    * Organization service instance
    */
   private final OrganizationService os;

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items (users, groups, etc)
    * @param ldapService {@link LDAPService}
    * @param cacheHandler 
    *          The Cache Handler
    * @throws Exception if any errors occurs
    */
   public UserDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, CacheHandler cacheHandler,
      OrganizationService os) throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
      if (ldapAttrMapping.userAccountControlAttr == null || ldapAttrMapping.userAccountControlAttr.isEmpty())
      {
         setDefaultUserAccountControlAttr(ldapAttrMapping);
      }
      if (ldapAttrMapping.userAccountControlFilter == null || ldapAttrMapping.userAccountControlFilter.isEmpty())
      {
         setDefaultUserAccountControlFilter(ldapAttrMapping);
      }
      this.os = os;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserEventListener(UserEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeUserEventListener(UserEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.remove(listener);
   }

   /**
    * {@inheritDoc}
    */
   public User createUserInstance()
   {
      return new UserImpl();
   }

   /**
    * {@inheritDoc}
    */
   public User createUserInstance(String username)
   {
      return new UserImpl(username);
   }

   /**
    * {@inheritDoc}
    */
   public void createUser(User user, boolean broadcast) throws Exception
   {
      String dnKeyValue = getDNKeyValue(user);
      String userDN = ldapAttrMapping.userDNKey + "=" + dnKeyValue + "," + ldapAttrMapping.userURL;
      Attributes attrs = ldapAttrMapping.userToAttributes(user);
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               if (broadcast)
                  preSave(user, true);
               ctx.createSubcontext(userDN, attrs).close();
               if (broadcast)
                  postSave(user, true);

               cacheHandler.put(user.getUserName(), user, CacheType.USER);
               break;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void saveUser(User user, boolean broadcast) throws Exception
   {
      if (user != null && !user.isEnabled())
         throw new DisabledUserException(user.getUserName());
      
      LdapContext ctx = ldapService.getLdapContext();
      String userDN = null;
      User existingUser = null;
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               userDN = getDNFromUsername(ctx, user.getUserName());
               if (userDN == null)
                  return;
               existingUser = getUserFromUsername(ctx, user.getUserName());
               ModificationItem[] mods = createUserModification(user, existingUser);
               if (broadcast)
                  preSave(user, false);
               ctx.modifyAttributes(userDN, mods);
               if (broadcast)
                  postSave(user, false);

               cacheHandler.put(user.getUserName(), user, CacheType.USER);
               break;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }

      if (existingUser != null && (!user.getPassword().equals(existingUser.getPassword())))
      {
         saveUserPassword(user, userDN);
      }
   }

   /**
    * Change user password.
    * 
    * @param user User
    * @param userDN Distinguished Name
    * @throws Exception if any errors occurs
    */
   protected void saveUserPassword(User user, String userDN) throws Exception
   {
      ModificationItem[] mods =
         new ModificationItem[]{new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
            ldapAttrMapping.userPassword, user.getPassword()))};
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               ctx.modifyAttributes(userDN, mods);
               break;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               User user = getUserFromUsername(ctx, userName);
               if (user == null)
               {
                  return null;
               }
               String userDN = getDNFromUsername(ctx, userName);

               if (broadcast)
               {
                  preDelete(user);
               }

               ctx.destroySubcontext(userDN);
               os.getUserProfileHandler().removeUserProfile(userName, false);
               ((MembershipDAOImpl)os.getMembershipHandler()).removeMembershipByUserDN(ctx, userName, userDN, false);
               cacheHandler.remove(userName, CacheType.USER);
               cacheHandler.remove(CacheHandler.USER_PREFIX + userName, CacheType.MEMBERSHIP);

               if (broadcast)
               {
                  postDelete(user);
               }
               return user;
            }  
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }

      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName) throws Exception
   {
      return findUserByName(userName, UserStatus.ENABLED);
   }

   public LazyPageList<User> findUsersByGroup(String groupId) throws Exception
   {
      return new LazyPageList<User>(findUsersByGroupId(groupId), 10);
   }

   /**
   * {@inheritDoc}
   */
   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception
   {
      return findUsersByGroupId(groupId, UserStatus.ENABLED);
   }

   public LazyPageList<User> getUserPageList(int pageSize) throws Exception
   {
      return new LazyPageList<User>(findAllUsers(), 10);
   }

   /**
   * {@inheritDoc}
   */
   public ListAccess<User> findAllUsers() throws Exception
   {
      return findAllUsers(UserStatus.ENABLED);
   }

   public LazyPageList<User> findUsers(Query q) throws Exception
   {
      return new LazyPageList<User>(findUsersByQuery(q), 10);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByQuery(Query q) throws Exception
   {
      return findUsersByQuery(q, UserStatus.ENABLED);
   }

   /**
    * Simple utility method to add asterisks symbol ('*')
    * to the very beginning and the end of the string.
    * @param s to be surrounded with asterisks
    * @return
    */
   private String addAsterisks(String s)
   {
      StringBuilder sb = new StringBuilder(s);
      if (!s.startsWith("*"))
      {
         sb.insert(0, "*");
      }
      if (!s.endsWith("*"))
      {
         sb.append("*");
      }

      return sb.toString();
   }
   /**
    * {@inheritDoc}
    */
   public boolean authenticate(String username, String password) throws Exception
   {
      String userDN = getDNFromUsername(username, UserStatus.ENABLED);
      if (userDN == null)
         return false;
      try
      {
         return ldapService.authenticate(userDN, password);
      }
      catch (NamingException exp)
      {
         return false;
      }
   }

   // helpers

   private String getDNKeyValue(User user)
   {
      String dnKeyValue = user.getUserName();
      if (!ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userUsernameAttr))
      {
         if (ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userLastNameAttr))
         {
            dnKeyValue = user.getLastName();
         }
         else if (ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userFirstNameAttr))
         {
            dnKeyValue = user.getFirstName();
         }
         else if (ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userMailAttr))
         {
            dnKeyValue = user.getEmail();
         }
         else if (ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userDisplayNameAttr))
         {
            dnKeyValue = user.getFullName();
         }
      }
      return dnKeyValue;
   }

   /**
    * @param user user for saving
    * @param existingUser existing user
    * @return array of {@link ModificationItem}
    */
   private ModificationItem[] createUserModification(User user, User existingUser)
   {
      ArrayList<ModificationItem> modifications = new ArrayList<ModificationItem>();

      // update displayName & description
      if (!user.getFullName().equals(existingUser.getFullName()))
      {
         ModificationItem mod =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userDisplayNameAttr,
               user.getFullName()));
         modifications.add(mod);
         mod =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.ldapDescriptionAttr,
               user.getFullName()));
         modifications.add(mod);
      }
      // update account name
      if (!user.getUserName().equals(existingUser.getUserName()))
      {
         ModificationItem mod =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userUsernameAttr,
               user.getUserName()));
         modifications.add(mod);
      }
      // update last name
      if (!user.getLastName().equals(existingUser.getLastName()))
      {
         ModificationItem mod =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userLastNameAttr,
               user.getLastName()));
         modifications.add(mod);
      }
      // update first name
      if (!user.getFirstName().equals(existingUser.getFirstName()))
      {
         ModificationItem mod =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userFirstNameAttr,
               user.getFirstName()));
         modifications.add(mod);
      }
      // update email
      if (!user.getEmail().equals(existingUser.getEmail()))
      {
         ModificationItem mod =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userMailAttr, user
               .getEmail()));
         modifications.add(mod);
      }
      ModificationItem[] mods = new ModificationItem[modifications.size()];
      return modifications.toArray(mods);
   }

   // listeners

   /**
    * For details see {@link UserEventListener#preSave(User, boolean)}.
    * 
    * @param user User
    * @param isNew is newly created
    * @throws Exception if any errors occurs
    */
   protected void preSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners)
         listener.preSave(user, isNew);
   }

   /**
    * For details see {@link UserEventListener#postSave(User, boolean)}.
    * 
    * @param user User
    * @param isNew is newly created
    * @throws Exception if any errors occurs
    */
   protected void postSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners)
         listener.postSave(user, isNew);
   }

   /**
    * For details see {@link UserEventListener#preDelete(User)}.
    * 
    * @param user User
    * @throws Exception if any errors occurs
    */
   protected void preDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners)
         listener.preDelete(user);
   }

   /**
    * For details see {@link UserEventListener#postDelete(User)}.
    * 
    * @param user User
    * @throws Exception if any errors occurs
    */
   protected void postDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners)
         listener.postDelete(user);
   }

   /**
    * For details see {@link UserEventListener#preSetEnabled(User)}.
    * 
    * @param user User
    * @throws Exception if any errors occurs
    */
   protected void preSetEnabled(User user) throws Exception
   {
      for (UserEventListener listener : listeners)
         listener.preSetEnabled(user);
   }

   /**
    * For details see {@link UserEventListener#postSetEnabled(User)}.
    * 
    * @param user User
    * @throws Exception if any errors occurs
    */
   protected void postSetEnabled(User user) throws Exception
   {
      for (UserEventListener listener : listeners)
         listener.postSetEnabled(user);
   }

   /**
    * {@inheritDoc}
    */
   public List<UserEventListener> getUserListeners()
   {
      return Collections.unmodifiableList(listeners);
   }

   /**
    * {@inheritDoc}
    */
   public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception
   {
      if (!ldapAttrMapping.hasUserAccountControl())
      {
         throw new UnsupportedOperationException();
      }

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               LDAPUserImpl user = (LDAPUserImpl)getUserFromUsername(ctx, userName);
               if (user == null || user.isEnabled() == enabled)
               {
                  return user;
               }
               String userDN = getDNFromUsername(ctx, userName);
               if (userDN == null)
               {
                  return user;
               }
               ModificationItem[] mods = createSetEnabledModification(user.getUserAccountControl(), enabled);

               user.setEnabled(enabled);
               if (broadcast)
               {
                  preSetEnabled(user);
               }

               try
               {
                  ctx.modifyAttributes(userDN, mods);
               }
               catch (SchemaViolationException e)
               {
                  ModificationItem[] modsWithUpgrade = new ModificationItem[mods.length + 1];
                  modsWithUpgrade[0] =
                     new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new ObjectClassAttribute(
                        LDAPAttributeMapping.USER_LDAP_CLASSES));
                  System.arraycopy(mods, 0, modsWithUpgrade, 1, mods.length);

                  ctx.setRequestControls(new Control[]{new BasicControl("1.3.6.1.4.1.4203.666.5.12")});
                  try
                  {
                     ctx.modifyAttributes(userDN, modsWithUpgrade);
                  }
                  finally
                  {
                     ctx.setRequestControls(null);
                  }
               }

               if (broadcast)
               {
                  postSetEnabled(user);
               }
               cacheHandler.put(user.getUserName(), user, CacheType.USER);
               return user;
            }  
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * @param userAccountControl the current value of the attribute <code>userAccountControlAttr</code>
    * @param enabled new value of the enabled flag
    * @return an array of all the modification to apply to enable/disable the user
    */
   protected ModificationItem[] createSetEnabledModification(int userAccountControl, boolean enabled)
   {
      ModificationItem[] mods = new ModificationItem[1];
      mods[0] =
         new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userAccountControlAttr,
            Integer.toString(enabled ? 0 : UF_ACCOUNTDISABLE)));
      return mods;
   }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName, UserStatus status) throws Exception
   {
      User user = (User)cacheHandler.get(userName, CacheType.USER);
      if (user != null)
      {
         return status.matches(user.isEnabled()) ? user : null;
      }

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               user = getUserFromUsername(ctx, userName);
               if (user != null)
               {
                  cacheHandler.put(user.getUserName(), user, CacheType.USER);
                  return status.matches(user.isEnabled()) ? user : null;
               }
               return user;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByGroupId(String groupId, UserStatus status) throws Exception
   {
      String searchBase = this.getGroupDNFromGroupId(groupId);
      String filter = ldapAttrMapping.membershipObjectClassFilter;
      return new ByGroupLdapUserListAccess(ldapAttrMapping, ldapService, searchBase, filter, status);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findAllUsers(UserStatus status) throws Exception
   {
      String searchBase = ldapAttrMapping.userURL;
      String filter;
      if (status != UserStatus.ANY && ldapAttrMapping.hasUserAccountControl())
      {
         StringBuilder buffer = new StringBuilder();
         buffer.append("(&(");
         buffer.append(ldapAttrMapping.userObjectClassFilter);
         buffer.append(")(");
         if (status == UserStatus.ENABLED)
            buffer.append(ldapAttrMapping.userAccountControlFilter);
         else
         {
            buffer.append("!(");
            buffer.append(ldapAttrMapping.userAccountControlFilter);
            buffer.append(")");
         }
         buffer.append("))");
         filter = buffer.toString();
      }
      else
      {
         filter = ldapAttrMapping.userObjectClassFilter;
      }
      return new SimpleLdapUserListAccess(ldapAttrMapping, ldapService, searchBase, filter);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByQuery(Query q, UserStatus status) throws Exception
   {
      List<String> list = new ArrayList<String>();
      if (q.getUserName() != null && q.getUserName().length() > 0)
      {
         list.add("(" + ldapAttrMapping.userUsernameAttr + "=" + addAsterisks(q.getUserName()) + ")");
      }
      if (q.getFirstName() != null && q.getFirstName().length() > 0)
      {
         list.add("(" + ldapAttrMapping.userFirstNameAttr + "=" + q.getFirstName() + ")");
      }
      if (q.getLastName() != null && q.getLastName().length() > 0)
      {
         list.add("(" + ldapAttrMapping.userLastNameAttr + "=" + q.getLastName() + ")");
      }
      if (q.getEmail() != null && q.getEmail().length() > 0)
      {
         list.add("(" + ldapAttrMapping.userMailAttr + "=" + q.getEmail() + ")");
      }
      if (status != UserStatus.ANY && ldapAttrMapping.hasUserAccountControl())
      {
         if (status == UserStatus.ENABLED)
            list.add("(" + ldapAttrMapping.userAccountControlFilter + ")");
         else
            list.add("(!(" + ldapAttrMapping.userAccountControlFilter + "))");
      }

      String filter;
      if (!list.isEmpty())
      {
         StringBuilder buffer = new StringBuilder();
         buffer.append("(&");
         for (int x = 0; x < list.size(); x++)
         {
            buffer.append(list.get(x));
         }
         buffer.append("(" + ldapAttrMapping.userObjectClassFilter + "))");
         filter = buffer.toString();
      }
      else
      {
         filter = "(" + ldapAttrMapping.userObjectClassFilter + ")";
      }
      String searchBase = ldapAttrMapping.userURL;

      return new SimpleLdapUserListAccess(ldapAttrMapping, ldapService, searchBase, filter);
   }

   /**
    * Set a default value to the field {@link LDAPAttributeMapping#userAccountControlAttr}
    * 
    * @param ldapAttrMapping the mapping to modify
    */
   protected void setDefaultUserAccountControlAttr(LDAPAttributeMapping ldapAttrMapping)
   {
      // For performance reason, we set it to null in case it is an empty string
      ldapAttrMapping.userAccountControlAttr = null;
   }

   /**
    * Set a default value to the field {@link LDAPAttributeMapping#userAccountControlFilter}
    * 
    * @param ldapAttrMapping the mapping to modify
    */
   protected void setDefaultUserAccountControlFilter(LDAPAttributeMapping ldapAttrMapping)
   {
      if (ldapAttrMapping.hasUserAccountControl())
      {
         ldapAttrMapping.userAccountControlFilter =
            "!(" + ldapAttrMapping.userAccountControlAttr + "=" + UF_ACCOUNTDISABLE + ")";
      }
   }
}
