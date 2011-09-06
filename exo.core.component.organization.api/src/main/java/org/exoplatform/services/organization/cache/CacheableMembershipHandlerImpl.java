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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;

import java.util.Collection;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 2009
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a> 
 * @version $Id$
 */
public class CacheableMembershipHandlerImpl implements MembershipHandler
{

   private final ExoCache membershipCache;

   private final MembershipHandler membershipHandler;

   /**
    * CacheableUserHandler  constructor.
    *
    * @param organizationCacheHandler
    *             - organization cache handler
    * @param membershipHandler
    *             - membership handler
    */
   public CacheableMembershipHandlerImpl(OrganizationCacheHandler organizationCacheHandler,
      MembershipHandler membershipHandler)
   {
      this.membershipCache = organizationCacheHandler.getMembershipCache();
      this.membershipHandler = membershipHandler;
   }

   /**
    * {@inheritDoc}
    */
   public void addMembershipEventListener(MembershipEventListener listener)
   {
      membershipHandler.addMembershipEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeMembershipEventListener(MembershipEventListener listener)
   {
      membershipHandler.removeMembershipEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void createMembership(Membership m, boolean broadcast) throws Exception
   {
      membershipHandler.createMembership(m, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public Membership createMembershipInstance()
   {
      return membershipHandler.createMembershipInstance();
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembership(String id) throws Exception
   {
      Membership membership = (Membership)membershipCache.get(id);
      if (membership != null)
         return membership;

      membership = membershipHandler.findMembership(id);

      if (membership != null)
      {
         membershipCache.put(membership.getId(), membership);
         membershipCache.put(new MembershipCacheKey(membership), membership);
      }

      return membership;
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {
      Membership membership = (Membership)membershipCache.get(new MembershipCacheKey(userName, groupId, type));
      if (membership != null)
         return membership;

      membership = membershipHandler.findMembershipByUserGroupAndType(userName, groupId, type);

      if (membership != null)
      {
         membershipCache.put(membership.getId(), membership);
         membershipCache.put(new MembershipCacheKey(membership), membership);
      }

      return membership;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findMembershipsByGroup(Group group) throws Exception
   {
      Collection<Membership> memberships = membershipHandler.findMembershipsByGroup(group);
      for (Membership membership : memberships)
      {
         membershipCache.put(membership.getId(), membership);
         membershipCache.put(new MembershipCacheKey(membership), membership);
      }

      return memberships;
   }

   public ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception
   {
      return membershipHandler.findAllMembershipsByGroup(group);
   }

   /**
    * {@inheritDoc}
    */
   public Collection findMembershipsByUser(String userName) throws Exception
   {
      Collection<Membership> memberships = membershipHandler.findMembershipsByUser(userName);
      for (Membership membership : memberships)
      {
         membershipCache.put(membership.getId(), membership);
         membershipCache.put(new MembershipCacheKey(membership), membership);
      }

      return memberships;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      Collection<Membership> memberships = membershipHandler.findMembershipsByUserAndGroup(userName, groupId);
      for (Membership membership : memberships)
      {
         membershipCache.put(membership.getId(), membership);
         membershipCache.put(new MembershipCacheKey(membership), membership);
      }

      return memberships;
   }

   /**
    * {@inheritDoc}
    */
   public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception
   {
      membershipHandler.linkMembership(user, group, m, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public Membership removeMembership(String id, boolean broadcast) throws Exception
   {
      Membership membership = membershipHandler.removeMembership(id, broadcast);
      if (membership != null)
      {
         membershipCache.remove(membership.getId());
         membershipCache.remove(new MembershipCacheKey(membership));
      }

      return membership;
   }

   /**
    * {@inheritDoc}
    */
   public Collection removeMembershipByUser(String username, boolean broadcast) throws Exception
   {
      Collection<Membership> memberships = membershipHandler.removeMembershipByUser(username, broadcast);

      for (Membership membership : memberships)
      {
         membershipCache.remove(membership.getId());
         membershipCache.remove(new MembershipCacheKey(membership));
      }

      return memberships;
   }

}
