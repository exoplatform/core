/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.organization.cache;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;

import java.util.Collection;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 2009
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a> 
 * @version $Id$
 */
public class CacheableGroupHandlerImpl implements GroupHandler
{

   private final ExoCache<String, Object> groupCache;

   private final ExoCache membershipCache;

   private final GroupHandler groupHandler;

   /**
    * CacheableUserHandler  constructor.
    *
    * @param OrganizationCacheHandler
    *             - organization cache handler
    * @param handler
    *             - user handler
    */
   public CacheableGroupHandlerImpl(OrganizationCacheHandler organizationCacheHandler, GroupHandler handler)
   {
      this.groupCache = organizationCacheHandler.getGroupCache();
      this.membershipCache = organizationCacheHandler.getMembershipCache();
      this.groupHandler = handler;
   }

   /**
    * {@inheritDoc}
    */
   public void addChild(Group parent, Group child, boolean broadcast) throws Exception
   {
      groupHandler.addChild(parent, child, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public void addGroupEventListener(GroupEventListener listener)
   {
      groupHandler.addGroupEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeGroupEventListener(GroupEventListener listener)
   {
      groupHandler.removeGroupEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void createGroup(Group group, boolean broadcast) throws Exception
   {
      groupHandler.createGroup(group, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public Group createGroupInstance()
   {
      return groupHandler.createGroupInstance();
   }

   /**
    * {@inheritDoc}
    */
   public Group findGroupById(String groupId) throws Exception
   {
      Group group = (Group)groupCache.get(groupId);
      if (group != null)
         return group;

      group = groupHandler.findGroupById(groupId);
      if (group != null)
         groupCache.put(groupId, group);

      return group;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findGroupByMembership(String userName, String membershipType) throws Exception
   {
      Collection<Group> groups = groupHandler.findGroupByMembership(userName, membershipType);

      for (Group group : groups)
         groupCache.put(group.getId(), groups);

      return groups;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findGroups(Group parent) throws Exception
   {
      Collection<Group> groups = groupHandler.findGroups(parent);
      for (Group group : groups)
         groupCache.put(group.getId(), groups);

      return groups;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findGroupsOfUser(String user) throws Exception
   {
      Collection<Group> groups = groupHandler.findGroupsOfUser(user);
      for (Group group : groups)
         groupCache.put(group.getId(), groups);

      return groups;
   }

   /**
    * {@inheritDoc}
    */
   public Collection getAllGroups() throws Exception
   {
      Collection<Group> groups = groupHandler.getAllGroups();
      for (Group group : groups)
         groupCache.put(group.getId(), groups);

      return groups;
   }

   /**
    * {@inheritDoc}
    */
   public Group removeGroup(Group group, boolean broadcast) throws Exception
   {
      Group gr = groupHandler.removeGroup(group, broadcast);
      if (gr != null)
      {
         groupCache.remove(gr.getId());

         List<Membership> memberships = membershipCache.getCachedObjects();
         for (Membership membership : memberships)
         {
            if (membership.getGroupId().equals(gr.getId()))
            {
               membershipCache.remove(membership.getId());
               membershipCache.remove(new MembershipCacheKey(membership));
            }
         }
      }

      return gr;
   }

   /**
    * {@inheritDoc}
    */
   public void saveGroup(Group group, boolean broadcast) throws Exception
   {
      groupHandler.saveGroup(group, broadcast);
      groupCache.put(group.getId(), group);
   }

}
