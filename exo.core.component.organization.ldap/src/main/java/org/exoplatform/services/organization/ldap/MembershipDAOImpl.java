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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.CacheHandler;
import org.exoplatform.services.organization.CacheHandler.CacheType;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipEventListenerHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.services.security.PermissionConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen tuan08@users.sourceforge.net Oct 14, 2005. @version
 * andrew00x $
 */
public class MembershipDAOImpl extends BaseDAO implements MembershipHandler, MembershipEventListenerHandler
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.MembershipDAOImpl");

   /**
    * See {@link MembershipEventListener}.
    */
   protected List<MembershipEventListener> listeners;

   /**
    * Organization service;
    */
   protected final OrganizationService service;

   /**
    * @param ldapAttrMapping
    *          mapping LDAP attributes to eXo organization service items (users, groups, etc)
    * @param ldapService
    *          {@link LDAPService}
    * @param service 
    *          Organization service implementation covering the handler. 
    * @param cacheHandler
    *          The Cache Handler
    * @throws Exception
    *           if any errors occurs
    */
   public MembershipDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, OrganizationService service,
      CacheHandler cacheHandler) throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
      this.listeners = new ArrayList<MembershipEventListener>(3);
      this.service = service;
   }

   /**
    * {@inheritDoc}
    */
   public void addMembershipEventListener(MembershipEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeMembershipEventListener(MembershipEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.remove(listener);
   }

   /**
    * {@inheritDoc}
    */
   public final Membership createMembershipInstance()
   {
      return new MembershipImpl();
   }

   /**
    * {@inheritDoc}
    */
   public void createMembership(Membership m, boolean broadcast) throws Exception
   {
      if (service.getMembershipTypeHandler().findMembershipType(m.getMembershipType()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId() + " because membership type "
            + m.getMembershipType() + " does not exists.");
      }

      if (service.getGroupHandler().findGroupById(m.getGroupId()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId() + ", because group "
            + m.getGroupId() + " does not exist.");
      }

      if (service.getUserHandler().findUserByName(m.getUserName()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId() + ", because user "
            + m.getGroupId() + " does not exist.");
      }

      // check if we already have membership record
      if (findMembershipByUserGroupAndType(m.getUserName(), m.getGroupId(), m.getMembershipType()) != null)
      {
         return;
      }

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               String userDN = getDNFromUsername(ctx, m.getUserName());
               String groupDN = getGroupDNFromGroupId(m.getGroupId());
               String membershipDN =
                  ldapAttrMapping.membershipTypeNameAttr + "=" + BaseDAO.escapeDN(m.getMembershipType()) + ","
                     + groupDN;

               Attributes attrs = null;
               try
               {
                  attrs = ctx.getAttributes(membershipDN);
               }
               catch (NameNotFoundException e)
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug(e.getLocalizedMessage(), e);
               }
               // if not found
               if (attrs == null)
               {
                  if (broadcast)
                     preSave(m, true);
                  ctx.createSubcontext(membershipDN, ldapAttrMapping.membershipToAttributes(m, userDN)).close();
                  if (broadcast)
                     postSave(m, true);
                  cacheHandler.put(cacheHandler.getMembershipKey(m), m, CacheType.MEMBERSHIP);
                  return;
               }

               // if contains membership
               List<?> members = getAttributes(attrs, ldapAttrMapping.membershipTypeMemberValue);
               if (members.contains(userDN))
                  return;

               // if need modification
               ModificationItem[] mods = new ModificationItem[1];
               mods[0] =
                  new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(
                     ldapAttrMapping.membershipTypeMemberValue, userDN));
               if (broadcast)
                  preSave(m, true);
               ctx.modifyAttributes(membershipDN, mods);
               if (broadcast)
                  postSave(m, true);
               cacheHandler.put(cacheHandler.getMembershipKey(m), m, CacheType.MEMBERSHIP);
               return;

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
   public void linkMembership(User user, Group group, MembershipType mt, boolean broadcast) throws Exception
   {
      if (user == null)
      {
         throw new InvalidNameException("Can not create membership record because user is null");
      }

      if (group == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because group is null");
      }

      if (mt == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because membership type is null");
      }

      createMembership(createMembershipObject(user.getUserName(), group.getId(), mt.getName()), broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public Membership removeMembership(String id, boolean broadcast) throws Exception
   {
      String[] membershipParts = id.split(",");
      if (membershipParts.length < 3)
         return null;
      String username = membershipParts[0];
      String membershipType = membershipParts[1];
      String groupId = membershipParts[2];

      MembershipImpl m = new MembershipImpl();
      m.setGroupId(groupId);
      m.setId(id);
      m.setMembershipType(membershipType);
      m.setUserName(username);

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               String userDN = getDNFromUsername(ctx, username).trim();
               String groupDN = getGroupDNFromGroupId(groupId);
               String membershipDN =
                  ldapAttrMapping.membershipTypeNameAttr + "=" + BaseDAO.escapeDN(membershipType) + ", " + groupDN;
               Attributes attrs = ctx.getAttributes(membershipDN);
               // Group does exist, is userDN in it?
               List<Object> members = this.getAttributes(attrs, ldapAttrMapping.membershipTypeMemberValue);
               boolean remove = false;
               for (int i = 0; i < members.size(); i++)
               {
                  if (String.valueOf(members.get(i)).trim().equalsIgnoreCase(userDN))
                  {
                     remove = true;
                     break;
                  }
               }

               if (!remove)
                  return m;

               if (members.size() > 1)
               {
                  ModificationItem[] mods = new ModificationItem[1];
                  mods[0] =
                     new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(
                        ldapAttrMapping.membershipTypeMemberValue, userDN));
                  if (broadcast)
                     preSave(m, true);
                  ctx.modifyAttributes(membershipDN, mods);
                  if (broadcast)
                     postSave(m, true);
                  cacheHandler.put(cacheHandler.getMembershipKey(m), m, CacheType.MEMBERSHIP);
               }
               else
               {
                  if (broadcast)
                     preDelete(m);
                  ctx.destroySubcontext(membershipDN);
                  if (broadcast)
                     postDelete(m);
                  cacheHandler.remove(cacheHandler.getMembershipKey(m), CacheType.MEMBERSHIP);
               }
               return m;
            }
            catch (NamingException e1)
            {
               ctx = reloadCtx(ctx, err, e1);
            }
         }
      }
      catch (NameNotFoundException e2)
      {
         if (LOG.isDebugEnabled())
            LOG.debug(e2.getLocalizedMessage(), e2);
         return null;
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Membership> removeMembershipByUser(String username, boolean broadcast) throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               String userDN = getDNFromUsername(ctx, username);
               return removeMembershipByUserDN(ctx, username, userDN, broadcast);
            }
            catch (NamingException e2)
            {
               ctx = reloadCtx(ctx, err, e2);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   Collection<Membership> removeMembershipByUserDN(LdapContext ctx, String username, String userDN, boolean broadcast) throws Exception
   {
      NamingEnumeration<SearchResult> results = null;

      ArrayList<Membership> memberships;
      try
      {
         memberships = new ArrayList<Membership>();
         // if userDN equals null than there is no such user
         // so we return empty collection
         if (userDN == null)
         {
            return memberships;
         }
         String filter = ldapAttrMapping.membershipTypeMemberValue + "=" + escapeDN(userDN);
         SearchControls constraints = new SearchControls();
         constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
         results = ctx.search(ldapAttrMapping.groupsURL, filter, constraints);
         while (results.hasMoreElements())
         {
            SearchResult sr = results.next();
            try
            {
               Attributes attrs = sr.getAttributes();

               String membershipDN = sr.getNameInNamespace();
               Group group = getGroupFromMembershipDN(ctx, membershipDN);
               String type = explodeDN(membershipDN, true)[0];
               Membership membership = createMembershipObject(username, group.getId(), type);
               memberships.add(membership);
               if (broadcast)
                  preDelete(membership);

               if (attrs.get(ldapAttrMapping.membershipTypeMemberValue).size() > 1)
               {
                  ModificationItem[] mods = new ModificationItem[1];
                  mods[0] =
                     new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(
                        ldapAttrMapping.membershipTypeMemberValue, userDN));
                  ctx.modifyAttributes(membershipDN, mods);
                  cacheHandler.put(cacheHandler.getMembershipKey(membership), membership, CacheType.MEMBERSHIP);
               }
               else
               {
                  ctx.destroySubcontext(membershipDN);
                  cacheHandler.remove(cacheHandler.getMembershipKey(membership), CacheType.MEMBERSHIP);
               }

               if (broadcast)
                  postDelete(membership);
            }
            catch (Exception e1)
            {
               LOG.error(e1.getLocalizedMessage(), e1);
            }
         }
      }
      finally
      {
         if (results != null)
            results.close();
      }
      return memberships;
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembership(String id) throws Exception
   {
      String[] membershipParts = id.split(",");
      Membership membership =
         findMembershipByUserGroupAndType(membershipParts[0], membershipParts[2], membershipParts[1]);
      return membership;
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {
      MembershipImpl membership =
         (MembershipImpl)cacheHandler.get(cacheHandler.getMembershipKey(userName, groupId, type), CacheType.MEMBERSHIP);
      if (membership != null)
      {
         return membership;
      }

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            membership = null;
            try
            {
               String userDN = getDNFromUsername(ctx, userName);
               if (userDN == null)
                  return null;

               userDN = userDN.trim();
               String mbfilter = membershipClassFilter();
               String filter =
                  "(&" + mbfilter + "(" + ldapAttrMapping.membershipTypeNameAttr + "=" + BaseDAO.escapeDN(type) + ")("
                     + ldapAttrMapping.membershipTypeMemberValue + "=" + userDN + "))";

               NamingEnumeration<SearchResult> results = findMembershipsInGroup(ctx, groupId, filter);
               try
               {
                  if (results.hasMoreElements())
                  {
                     membership = createMembershipObject(userName, groupId, type);
                  }
               }
               finally
               {
                  results.close();
               }

               if (membership != null)
               {
                  cacheHandler.put(cacheHandler.getMembershipKey(membership), membership, CacheType.MEMBERSHIP);
               }
               return membership;
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
   public Collection<Membership> findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      ArrayList<Membership> memberships = new ArrayList<Membership>();
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            // remove all items that can be added in previous iteration
            memberships.clear();
            try
            {
               // check if user exists
               String userDN = getDNFromUsername(ctx, userName);
               if (userDN == null)
                  return memberships;
               userDN = userDN.trim();

               String userFilter = "(" + ldapAttrMapping.membershipTypeMemberValue + "=" + userDN + ")";
               String mbfilter = membershipClassFilter();
               String filter = "(&" + userFilter + mbfilter + ")";

               results = findMembershipsInGroup(ctx, groupId, filter);
               // add memberships matching user
               while (results.hasMoreElements())
               {
                  SearchResult sr = results.next();
                  String type = explodeDN(sr.getNameInNamespace(), true)[0];
                  Membership membership = createMembershipObject(userName, groupId, type);
                  memberships.add(membership);
               }
               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Retrieved " + memberships.size() + " memberships from ldap for user " + userName
                     + " in group " + groupId);
               }
               return memberships;
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
            }
            finally
            {
               if (results != null)
                  results.close();
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * List memberships of a group by applying the membershipObjectFilter.
    * 
    * @param ctx
    *          {@link LdapContext}
    * @param groupId
    *          id of the group to retrieve
    * @param filter
    *          filter to apply to search
    * @return search results
    * @throws NamingException
    *           if {@link NamingException} occurs
    */
   private NamingEnumeration<SearchResult> findMembershipsInGroup(LdapContext ctx, String groupId, String filter)
      throws NamingException
   {
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      String groupDN = getGroupDNFromGroupId(groupId);
      try
      {
         return ctx.search(groupDN, filter, constraints);
      }
      catch (NamingException ne)
      {
         // we assume that NamingException means that
         // groupDN represent not existing sequence of elements
         // so the result should be an empty NamingEnumeration
         return new NamingEnumeration<SearchResult>()
         {

            public boolean hasMoreElements()
            {
               return false;
            }

            public SearchResult nextElement()
            {
               return null;
            }

            public SearchResult next() throws NamingException
            {
               return null;
            }

            public boolean hasMore() throws NamingException
            {
               return false;
            }

            public void close() throws NamingException
            {
            }
         };
      }
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Membership> findMembershipsByUser(String userName) throws Exception
   {
      ArrayList<Membership> memberships = new ArrayList<Membership>();
      String mbfilter = membershipClassFilter();

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            // remove all items that can be added in previous iteration
            memberships.clear();
            try
            {
               // check if user exists
               String userDN = getDNFromUsername(ctx, userName);
               if (userDN == null)
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug("User " + userName + " not found. ");
                  return memberships;
               }

               userDN = userDN.trim();
               String userFilter = "(" + ldapAttrMapping.membershipTypeMemberValue + "=" + userDN + ")";
               String filter = "(&" + userFilter + mbfilter + ")";

               SearchControls constraints = new SearchControls();
               constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

               results = ctx.search(ldapAttrMapping.groupsURL, filter, constraints);
               // add memberships matching user
               while (results.hasMoreElements())
               {
                  SearchResult sr = results.next();
                  String membershipDN = sr.getNameInNamespace();
                  String groupId = getGroupIdFromGroupDN(getGroupDNFromMembershipDN(membershipDN));
                  String type = explodeDN(membershipDN, true)[0];
                  Membership membership = createMembershipObject(userName, groupId, type);
                  memberships.add(membership);
               }
               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Retrieved " + memberships.size() + " memberships from ldap for user " + userName);
               }
               return memberships;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
            finally
            {
               if (results != null)
                  results.close();
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
   public Collection<Membership> findMembershipsByGroup(Group group) throws Exception
   {
      ArrayList<Membership> memberships = new ArrayList<Membership>();
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            memberships.clear();
            try
            {
               if (LOG.isDebugEnabled())
                  LOG.debug("Searching memberships of group " + group.getId() + ": ");
               String groupDN = this.getGroupDNFromGroupId(group.getId());
               SearchControls constraints = new SearchControls();
               constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
               results = ctx.search(groupDN, ldapAttrMapping.membershipObjectClassFilter, constraints);
               while (results.hasMoreElements())
               {
                  SearchResult sr = results.next();
                  String membershipType = explodeDN(sr.getNameInNamespace(), true)[0];
                  Attributes attrs = sr.getAttributes();
                  Attribute attr = attrs.get(ldapAttrMapping.membershipTypeMemberValue);
                  String userName;
                  for (int i = 0; i < attr.size(); i++)
                  {
                     String userDN = String.valueOf(attr.get(i));
                     if (ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userUsernameAttr))
                     {
                        userName = explodeDN(userDN, true)[0];
                     }
                     else
                     {
                        userName = findUserByDN(ctx, userDN).getUserName();
                     }
                     Membership membership = createMembershipObject(userName, group.getId(), membershipType);
                     if (LOG.isDebugEnabled())
                        LOG.debug("  found " + membership.toString());
                     memberships.add(membership);
                  }
               }
               return memberships;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
            finally
            {
               if (results != null)
                  results.close();
            }
         }
      }
      catch (NamingException e)
      {
         return new ArrayList<Membership>();
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception
   {
      return new MembershipsByGroupLdapListAccess(ldapService, this, ldapAttrMapping, group.getId());
   }

   //

   /**
    * Create {@link Membership} instance.
    * 
    * @param userName
    *          user name
    * @param groupId
    *          group ID
    * @param type
    *          membership type
    * @return newly created instance of {@link Membership}
    */
   protected MembershipImpl createMembershipObject(String userName, String groupId, String type)
   {
      MembershipImpl membership = new MembershipImpl();
      membership.setGroupId(groupId);
      membership.setUserName(userName);
      membership.setMembershipType(type);
      membership.setId(userName + "," + type + "," + groupId);
      return membership;
   }

   // listeners

   /**
    * For details see {@link MembershipEventListener#postDelete(Membership)}.
    * 
    * @param membership
    *          Membership
    * @throws Exception
    *           if any errors occurs
    */
   private void postDelete(Membership membership) throws Exception
   {
      for (MembershipEventListener listener : listeners)
         listener.postDelete(membership);
   }

   /**
    * For details see {@link MembershipEventListener#preDelete(Membership))}.
    * 
    * @param membership
    *          Membership
    * @throws Exception
    *           if any errors occurs
    */
   private void preDelete(Membership membership) throws Exception
   {
      for (MembershipEventListener listener : listeners)
         listener.preDelete(membership);
   }

   /**
    * For details see {@link MembershipEventListener#postSave(Membership, boolean)}.
    * 
    * @param membership
    *          Membership
    * @param isNew
    *          is newly created membership
    * @throws Exception
    *           if any errors occurs
    */
   private void postSave(Membership membership, boolean isNew) throws Exception
   {
      for (MembershipEventListener listener : listeners)
         listener.postSave(membership, isNew);
   }

   /**
    * For details see {@link MembershipEventListener#preSave(Membership, boolean)}.
    * 
    * @param membership
    *          Membership
    * @param isNew
    *          is newly created membership
    * @throws Exception
    *           if any errors occurs
    */
   private void preSave(Membership membership, boolean isNew) throws Exception
   {
      for (MembershipEventListener listener : listeners)
         listener.preSave(membership, isNew);
   }

   /**
    * {@inheritDoc}
    */
   public List<MembershipEventListener> getMembershipListeners()
   {
      return Collections.unmodifiableList(listeners);
   }
}
