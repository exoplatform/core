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

import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupEventListenerHandler;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.ldap.CacheHandler.CacheType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen.
 * tuan08@users.sourceforge.net Oct 14, 2005
 * 
 * @version andrew00x $
 */
public class GroupDAOImpl extends BaseDAO implements GroupHandler, GroupEventListenerHandler
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.GroupDAOImpl");

   /**
    * See {@link GroupEventListener}.
    */
   protected List<GroupEventListener> listeners;

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items (users, groups, etc)
    * @param ldapService {@link LDAPService}
    * @param cacheHandler
    *          The Cache Handler
    * @throws Exception if any errors occurs
    */
   public GroupDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, CacheHandler cacheHandler)
      throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
      this.listeners = new ArrayList<GroupEventListener>(3);
   }

   /**
    * {@inheritDoc}
    */
   public void addGroupEventListener(GroupEventListener listener)
   {
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeGroupEventListener(GroupEventListener listener)
   {
      listeners.remove(listener);
   }

   /**
    * {@inheritDoc}
    */
   public final Group createGroupInstance()
   {
      return new GroupImpl();
   }

   /**
    * {@inheritDoc}
    */
   public void createGroup(Group group, boolean broadcast) throws Exception
   {
      addChild(null, group, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public void addChild(Group parent, Group child, boolean broadcast) throws Exception
   {
      setId(parent, child);
      String searchBase = createSubDN(parent);
      String groupDN = ldapAttrMapping.groupDNKey + "=" + child.getGroupName() + "," + searchBase;
      String filter = ldapAttrMapping.groupNameAttr + "=" + child.getGroupName();

      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            try
            {
               results = ctx.search(searchBase, filter, constraints);

               if (results.hasMore())
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug("Group " + child + ", parent  " + parent + " already exists. ");
                  return;
               }

               GroupImpl group = (GroupImpl)child;
               if (broadcast)
                  preSave(group, true);
               ctx.createSubcontext(groupDN, ldapAttrMapping.groupToAttributes(child));
               if (broadcast)
                  postSave(group, true);
               
               cacheHandler.put(child.getId(), group, CacheType.GROUP);
               return;
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
   public void saveGroup(Group group, boolean broadcast) throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               Group parent = findGroupById(ctx, group.getParentId());
               setId(parent, group);
               String groupDN = ldapAttrMapping.groupDNKey + "=" + group.getGroupName() + "," + createSubDN(parent);

               ArrayList<ModificationItem> modifications = new ArrayList<ModificationItem>();
               ModificationItem mod =
                  new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                     ldapAttrMapping.ldapDescriptionAttr, group.getDescription()));
               modifications.add(mod);

               mod =
                  new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.groupLabelAttr,
                     group.getLabel()));
               modifications.add(mod);

               ModificationItem[] mods = new ModificationItem[modifications.size()];
               modifications.toArray(mods);
               if (broadcast)
                  preSave(group, true);
               ctx.modifyAttributes(groupDN, mods);
               if (broadcast)
                  postSave(group, true);

               cacheHandler.put(group.getId(), group, CacheType.GROUP);
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
   public Group removeGroup(Group group, boolean broadcast) throws Exception
   {
      String filter = ldapAttrMapping.groupNameAttr + "=" + group.getGroupName();
      String searchBase = this.createSubDN(group.getParentId());
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            try
            {
               results = ctx.search(searchBase, filter, constraints);

               if (!results.hasMoreElements())
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug("Nothing for removing, group " + group);
                  return group;
               }

               SearchResult sr = results.next();
               // NameParser parser = ctx.getNameParser("");
               // Name entryName = parser.parse(new
               // CompositeName(sr.getName()).get(0));
               // String groupDN = entryName + "," + searchBase;
               String groupDN = sr.getNameInNamespace();

               group = getGroupByDN(ctx, groupDN);
               if (group == null)
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug("Nothing for removing, group " + group);
                  return group;
               }

               if (broadcast)
                  preDelete(group);
               removeAllSubtree(ctx, groupDN);
               if (broadcast)
                  postDelete(group);

               return group;

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
   @SuppressWarnings("unchecked")
   public Collection findGroupByMembership(String userName, String membershipType) throws Exception
   {
      List<Group> groups = new ArrayList<Group>();
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            groups.clear();
            try
            {
               String filter =
                  "(&(" + ldapAttrMapping.membershipTypeMemberValue + "=" + getDNFromUsername(ctx, userName) + ")("
                     + ldapAttrMapping.membershipTypeRoleNameAttr + "=" + membershipType + "))";
               SearchControls constraints = new SearchControls();
               constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

               results = ctx.search(ldapAttrMapping.groupsURL, filter, constraints);
               while (results.hasMoreElements())
               {
                  SearchResult sr = results.next();
                  NameParser parser = ctx.getNameParser("");
                  Name entryNameName = parser.parse(new CompositeName(sr.getName()).get(0));
                  String entryName =
                     String.valueOf(entryNameName).substring(entryNameName.getSuffix(1).toString().length() + 1);
                  String groupDN = entryName + "," + ldapAttrMapping.groupsURL;
                  Group group = getGroupByDN(ctx, groupDN);
                  if (group != null)
                     addGroup(groups, group);
               }

               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Retrieved " + groups.size() + " groups from ldap for user " + userName
                     + " with membershiptype " + membershipType);
               }
               return groups;
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
   public Group findGroupById(String groupId) throws Exception
   {
      if (groupId == null)
         return null;

      Group group = (Group)cacheHandler.get(groupId, CacheType.GROUP);
      if (group != null)
      {
         return group;
      }
      
      String parentId = null;
      StringBuffer buffer = new StringBuffer();
      String[] groupIdParts = groupId.split("/");
      for (int x = 1; x < groupIdParts.length; x++)
      {
         buffer.append("/" + groupIdParts[x]);
         if (x == (groupIdParts.length - 2))
            parentId = buffer.toString();
      }
      String groupDN = getGroupDNFromGroupId(groupId);
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               Attributes attrs = ctx.getAttributes(groupDN);
               group = ldapAttrMapping.attributesToGroup(attrs);
               ((GroupImpl)group).setId(groupId);
               ((GroupImpl)group).setParentId(parentId);

               if (group != null)
               {
                  cacheHandler.put(groupId, group, CacheType.GROUP);
               }
               return group;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      catch (NameNotFoundException e)
      {
         return null;
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * Find group by group ID using supplied context.
    * 
    * @param ctx {@link LdapContext}
    * @param groupId group ID
    * @return Group or null if nothing found
    * @throws Exception if any error occurs
    */
   private Group findGroupById(LdapContext ctx, String groupId) throws Exception
   {
      if (groupId == null)
         return null;

      Group group = (Group)cacheHandler.get(groupId, CacheType.GROUP);
      if (group != null)
      {
         return group;
      }

      String parentId = null;
      StringBuffer buffer = new StringBuffer();
      String[] groupIdParts = groupId.split("/");
      for (int x = 1; x < groupIdParts.length; x++)
      {
         buffer.append("/" + groupIdParts[x]);
         if (x == (groupIdParts.length - 2))
            parentId = buffer.toString();
      }
      String groupDN = getGroupDNFromGroupId(groupId);
      try
      {
         Attributes attrs = ctx.getAttributes(groupDN);
         group = ldapAttrMapping.attributesToGroup(attrs);
         ((GroupImpl)group).setId(groupId);
         ((GroupImpl)group).setParentId(parentId);

         if (group != null)
         {
            cacheHandler.put(groupId, group, CacheType.GROUP);
         }
         return group;
      }
      catch (NameNotFoundException e)
      {
         if (LOG.isDebugEnabled())
            LOG.debug(e.getLocalizedMessage(), e);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Collection getAllGroups() throws Exception
   {
      List<Group> groups = new ArrayList<Group>();

      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

      LdapContext ctx = ldapService.getLdapContext();
      String groupName = "*";
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            groups.clear();
            try
            {
               try
               {
                  results =
                     ctx.search(ldapAttrMapping.groupsURL, "(" + ldapAttrMapping.groupNameAttr + "=" + groupName + ")",
                        constraints);
               }
               catch (NamingException e1)
               {
                  // if connection error let process it in common way
                  if (isConnectionError(e1))
                     throw e1;
                  if (LOG.isDebugEnabled())
                     LOG.debug("Failed to get all groups. ", e1);
                  return groups;
               }
               while (results.hasMoreElements())
               {
                  SearchResult sr = results.next();
                  NameParser parser = ctx.getNameParser("");
                  CompositeName name = new CompositeName(sr.getName());
                  if (name.size() > 0)
                  {
                     Name entryName = parser.parse(name.get(0));
                     String groupDN = entryName + "," + ldapAttrMapping.groupsURL;
                     Group group = this.buildGroup(groupDN, sr.getAttributes());
                     if (group != null)
                        addGroup(groups, group);
                  }
               }
               return groups;
            }
            catch (NamingException e2)
            {
               ctx = reloadCtx(ctx, err, e2);
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
   @SuppressWarnings("unchecked")
   public Collection findGroups(Group parent) throws Exception
   {
      List<Group> groups = new ArrayList<Group>();
      String groupsBaseDN = ldapAttrMapping.groupsURL;
      StringBuffer buffer = new StringBuffer();

      if (parent != null)
      {
         String[] dnParts = parent.getId().split("/");
         for (int x = (dnParts.length - 1); x > 0; x--)
         {
            buffer.append(ldapAttrMapping.groupDNKey + "=" + dnParts[x] + ", ");
         }
      }
      buffer.append(groupsBaseDN);

      LdapContext ctx = ldapService.getLdapContext();
      String searchBase = buffer.toString();
      String filter = ldapAttrMapping.groupObjectClassFilter;
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            groups.clear();
            try
            {
               try
               {
                  results = ctx.search(searchBase, filter, constraints);
               }
               catch (NamingException e1)
               {
                  // if connection error let process it in common way
                  if (isConnectionError(e1))
                     throw e1;
                  if (LOG.isDebugEnabled())
                     LOG.debug("Failed to get groups from parent " + parent.getId() + ". ", e1);
                  return groups;
               }
               while (results.hasMoreElements())
               {
                  SearchResult sr = results.next();
                  NameParser parser = ctx.getNameParser("");
                  CompositeName name = new CompositeName(sr.getName());
                  if (name.size() > 0)
                  {
                     Name entryName = parser.parse(name.get(0));
                     String groupDN = entryName + "," + searchBase;
                     Group group = this.buildGroup(groupDN, sr.getAttributes());
                     if (group != null)
                        addGroup(groups, group);
                  }
               }
               return groups;
            }
            catch (NamingException e2)
            {
               ctx = reloadCtx(ctx, err, e2);
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
   @SuppressWarnings("unchecked")
   public Collection findGroupsOfUser(String userName) throws Exception
   {
      List<Group> groups = new ArrayList<Group>();

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            groups.clear();
            NamingEnumeration<SearchResult> results = null;
            try
            {
               // check if user exists
               String userDN = getDNFromUsername(ctx, userName);
               if (userDN == null)
                  return groups;
               userDN = userDN.trim();

               SearchControls constraints = new SearchControls();
               constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
               String mbfilter = membershipClassFilter();
               String userFilter = "(" + ldapAttrMapping.membershipTypeMemberValue + "=" + userDN + ")";
               String filter = "(&" + userFilter + mbfilter + ")";
               results = ctx.search(ldapAttrMapping.groupsURL, filter, constraints);

               // add groups for memberships matching user
               Set<String> uniqueGroupsDN = new HashSet<String>();
               while (results != null && results.hasMore())
               {
                  SearchResult sr = results.next();
                  NameParser parser = ctx.getNameParser("");
                  CompositeName name = new CompositeName(sr.getName());
                  if (name.size() < 1)
                     break;
                  Name entryName = parser.parse(name.get(0));
                  String membershipDN = entryName + "," + ldapAttrMapping.groupsURL;
                  uniqueGroupsDN.add(this.getGroupDNFromMembershipDN(membershipDN));
               }
               for(String groupDN : uniqueGroupsDN)
               {
                    Group group = this.getGroupByDN(ctx, groupDN);
                    if (group != null)
                        addGroup(groups, group);
               }
               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Retrieved " + groups.size() + " groups from ldap for user " + userName);
               }
               return groups;
            }
            catch (NamingException e2)
            {
               ctx = reloadCtx(ctx, err, e2);
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
    * Add group in list if list does not contains it yet.
    * 
    * @param groups list of groups
    * @param g group to be added in list
    */
   protected void addGroup(List<Group> groups, Group g)
   {
      for (int i = 0; i < groups.size(); i++)
         if (groups.get(i).getId().equals(g.getId()))
            return;
      groups.add(g);
   }

   // listeners

   /**
    * For details, see {@link GroupEventListener#preSave(Group, boolean)}.
    * 
    * @param group group
    * @param isNew is group newly created
    * @throws Exception if any errors occurs
    */
   protected void preSave(Group group, boolean isNew) throws Exception
   {
      for (GroupEventListener listener : listeners)
         listener.preSave(group, isNew);
   }

   /**
    * For details, see {@link GroupEventListener#postSave(Group, boolean)}.
    * 
    * @param group group
    * @param isNew is group newly created
    * @throws Exception if any errors occurs
    */
   protected void postSave(Group group, boolean isNew) throws Exception
   {
      for (GroupEventListener listener : listeners)
         listener.postSave(group, isNew);
   }

   /**
    * For details, see {@link GroupEventListener#preDelete(Group)}.
    * 
    * @param group group
    * @throws Exception if any errors occurs
    */
   protected void preDelete(Group group) throws Exception
   {
      for (GroupEventListener listener : listeners)
         listener.preDelete(group);
   }

   /**
    * For details, see {@link GroupEventListener#postDelete(Group)}.
    * 
    * @param group group
    * @throws Exception if any errors occurs
    */
   protected void postDelete(Group group) throws Exception
   {
      for (GroupEventListener listener : listeners)
         listener.postDelete(group);
   }

   //

   protected String createSubDN(Group parent)
   {
      if (parent == null)
         return createSubDN("");
      return createSubDN(parent.getId());
   }

   protected String createSubDN(String parentId)
   {
      StringBuffer buffer = new StringBuffer();
      if (parentId != null && parentId.length() > 0)
      {
         String[] dnParts = parentId.split("/");
         for (int x = (dnParts.length - 1); x > 0; x--)
            buffer.append(ldapAttrMapping.groupDNKey + "=" + dnParts[x] + ", ");
      }
      buffer.append(ldapAttrMapping.groupsURL);
      return buffer.toString();
   }

   protected void setId(Group parent, Group g)
   {
      GroupImpl group = (GroupImpl)g;
      if (parent == null)
      {
         group.setId("/" + group.getGroupName());
         return;
      }
      group.setId(parent.getId() + "/" + group.getGroupName());
      group.setParentId(parent.getId());
   }

   /**
    * {@inheritDoc}
    */
   public List<GroupEventListener> getGroupListeners()
   {
      return Collections.unmodifiableList(listeners);
   }
}
