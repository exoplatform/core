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
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeEventListener;
import org.exoplatform.services.organization.MembershipTypeHandler;

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
public class CacheableMembershipTypeHandlerImpl implements MembershipTypeHandler
{

   private final ExoCache<String, MembershipType> membershipTypeCache;

   private final ExoCache membershipCache;

   private final MembershipTypeHandler membershipTypeHandler;

   /**
    * CacheableMembershipTypeHandler  constructor.
    *
    * @param organizationCacheHandler
    *             - organization cache handler
    * @param membershipTypeHandler
    *             - membership type handler
    */
   public CacheableMembershipTypeHandlerImpl(OrganizationCacheHandler organizationCacheHandler,
      MembershipTypeHandler membershipTypeHandler)
   {
      this.membershipTypeCache = organizationCacheHandler.getMembershipTypeCache();
      this.membershipCache = organizationCacheHandler.getMembershipCache();
      this.membershipTypeHandler = membershipTypeHandler;
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      MembershipType membershipType = membershipTypeHandler.createMembershipType(mt, broadcast);
      membershipTypeCache.put(membershipType.getName(), membershipType);

      return membershipType;
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType createMembershipTypeInstance()
   {
      return membershipTypeHandler.createMembershipTypeInstance();
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType findMembershipType(String name) throws Exception
   {
      MembershipType membershipType = (MembershipType)membershipTypeCache.get(name);
      if (membershipType != null)
         return membershipType;

      membershipType = membershipTypeHandler.findMembershipType(name);
      if (membershipType != null)
         membershipTypeCache.put(name, membershipType);

      return membershipType;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<MembershipType> findMembershipTypes() throws Exception
   {

      Collection<MembershipType> membershipTypes = membershipTypeHandler.findMembershipTypes();
      for (MembershipType membershipType : membershipTypes)
         membershipTypeCache.put(membershipType.getName(), membershipType);

      return membershipTypes;
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception
   {
      MembershipType membershipType = membershipTypeHandler.removeMembershipType(name, broadcast);
      if (membershipType != null)
      {
         membershipTypeCache.remove(name);

         List<Membership> memberships = membershipCache.getCachedObjects();
         for (Membership membership : memberships)
         {
            if (membership.getMembershipType().equals(name))
            {
               membershipCache.remove(membership.getId());
               membershipCache.remove(new MembershipCacheKey(membership));
            }
         }
      }

      return membershipType;
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      MembershipType membershipType = membershipTypeHandler.saveMembershipType(mt, broadcast);
      membershipTypeCache.put(membershipType.getName(), membershipType);

      return membershipType;
   }

   /**
    * {@inheritDoc}
    */
   public void addMembershipTypeEventListener(MembershipTypeEventListener listener)
   {
      membershipTypeHandler.addMembershipTypeEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeMembershipTypeEventListener(MembershipTypeEventListener listener)
   {
      membershipTypeHandler.removeMembershipTypeEventListener(listener);
   }
}
