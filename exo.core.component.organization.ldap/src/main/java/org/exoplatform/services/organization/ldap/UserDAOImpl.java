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
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.impl.UserImpl;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 14, 2005. @version andrew00x $
 */
public class UserDAOImpl extends BaseDAO implements UserHandler
{

   /**
    * User event listeners.
    * 
    * @see UserEventListener.
    */
   private List<UserEventListener> listeners = new ArrayList<UserEventListener>(5);

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items (users, groups, etc)
    * @param ldapService {@link LDAPService}
    * @throws Exception if any errors occurs
    */
   public UserDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService) throws Exception
   {
      super(ldapAttrMapping, ldapService);
   }

   /**
    * {@inheritDoc}
    */
   public void addUserEventListener(UserEventListener listener)
   {
      listeners.add(listener);
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
               ctx.createSubcontext(userDN, attrs);
               if (broadcast)
                  postSave(user, true);
               break;
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
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
      LdapContext ctx = ldapService.getLdapContext();
      String userDN = null;
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               userDN = getDNFromUsername(ctx, user.getUserName());
               if (userDN == null)
                  return;
               User existingUser = getUserFromUsername(ctx, user.getUserName());
               ModificationItem[] mods = createUserModification(user, existingUser);
               if (broadcast)
                  preSave(user, false);
               ctx.modifyAttributes(userDN, mods);
               if (broadcast)
                  postSave(user, false);
               break;
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
      // TODO really need this ?
      if (!user.getPassword().equals("PASSWORD"))
         saveUserPassword(user, userDN);
   }

   /**
    * Change user password.
    * 
    * @param user User
    * @param userDN Distinguished Name
    * @throws Exception if any errors occurs
    */
   void saveUserPassword(User user, String userDN) throws Exception
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
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
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
                  return null;

               String userDN = getDNFromUsername(ctx, userName);

               if (broadcast)
                  preDelete(user);
               ctx.destroySubcontext(userDN);
               if (broadcast)
                  postDelete(user);
               return user;
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
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
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               return getUserFromUsername(ctx, userName);
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
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
      //    ArrayList<User> users = new ArrayList<User>();
      //    TreeMap<String, User> map = new TreeMap<String, User>();
      //
      //    LdapContext ctx = ldapService.getLdapContext();
      //    try {
      //      NamingEnumeration<SearchResult> results = null;
      //      for (int err = 0;; err++) {
      //        map.clear();
      //        try {
      //          String searchBase = this.getGroupDNFromGroupId(groupId);
      //          String filter = ldapAttrMapping.membershipObjectClassFilter;
      //          SearchControls constraints = new SearchControls();
      //          constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      //          results = ctx.search(searchBase, filter, constraints);
      //          while (results.hasMoreElements()) {
      //            SearchResult sr = results.next();
      //            Attributes attrs = sr.getAttributes();
      //            List<Object> members = this.getAttributes(attrs,
      //                                                      ldapAttrMapping.membershipTypeMemberValue);
      //            for (int x = 0; x < members.size(); x++) {
      //              User user = findUserByDN(ctx, (String) members.get(x));
      //              if (user != null)
      //                map.put(user.getUserName(), user);
      //            }
      //          }
      //          break;
      //        } catch (NamingException e) {
      //          if (isConnectionError(e) && err < getMaxConnectionError())
      //            ctx = ldapService.getLdapContext(true);
      //          else 
      //            throw e;
      //        } finally {
      //          if (results != null)
      //            results.close();
      //        }
      //      }
      //    } finally {
      //      ldapService.release(ctx);
      //    }
      //
      //    for (Iterator<String> i = map.keySet().iterator(); i.hasNext();)
      //      users.add(map.get(i.next()));
      //    
      //    return new ObjectPageList(users, 10);

      String searchBase = this.getGroupDNFromGroupId(groupId);
      String filter = ldapAttrMapping.membershipObjectClassFilter;
      return new ByGroupLdapUserListAccess(ldapAttrMapping, ldapService, searchBase, filter);
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
      String searchBase = ldapAttrMapping.userURL;
      String filter = ldapAttrMapping.userObjectClassFilter;

      //    return new LDAPUserPageList(ldapAttrMapping, ldapService, searchBase, filter, pageSize);

      return new SimpleLdapUserListAccess(ldapAttrMapping, ldapService, searchBase, filter);
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
      String filter = null;
      ArrayList<String> list = new ArrayList<String>();
      if (q.getUserName() != null && q.getUserName().length() > 0)
      {
         list.add("(" + ldapAttrMapping.userUsernameAttr + "=" + q.getUserName() + ")");
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

      if (list.size() > 0)
      {
         StringBuilder buffer = new StringBuilder();
         buffer.append("(&");
         if (list.size() > 1)
         {
            for (int x = 0; x < list.size(); x++)
            {
               if (x == (list.size() - 1))
                  buffer.append(list.get(x));
               else
                  buffer.append(list.get(x) + " || ");
            }
         }
         else
            buffer.append(list.get(0));

         buffer.append("(" + ldapAttrMapping.userObjectClassFilter + ") )");
         filter = buffer.toString();
      }
      else
         filter = ldapAttrMapping.userObjectClassFilter;
      String searchBase = ldapAttrMapping.userURL;

      //    return new LDAPUserPageList(ldapAttrMapping, ldapService, searchBase, filter, 20);

      return new SimpleLdapUserListAccess(ldapAttrMapping, ldapService, searchBase, filter);
   }

   /**
    * {@inheritDoc}
    */
   public boolean authenticate(String username, String password) throws Exception
   {
      String userDN = getDNFromUsername(username);
      if (userDN == null)
         return false;
      try
      {
         return ldapService.authenticate(userDN, password);
      }
      catch (Exception exp)
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
}
