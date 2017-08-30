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

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br>Date: 2009
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a> 
 * @version $Id$
 */
@SuppressWarnings("rawtypes")
public class OrganizationCacheHandler
{

   /**
   * Use cache.
   */
   private final ExoCache userCache;

   /**
    * User profile cache.
    */
   private final ExoCache userProfileCache;

   /**
    * Membership cache.
    */
   private final ExoCache membershipCache;

   /**
    * Membership type cache.
    */
   private final ExoCache membershipTypeCache;

   /**
    * Group cache. 
    */
   private final ExoCache groupCache;

   /**
    * OrganizationCacheHandler  constructor.
    *
    * @param cservice
    *          cache service
    */
   public OrganizationCacheHandler(CacheService cservice)
   {
      userCache = cservice.getCacheInstance("portal.User");
      userProfileCache = cservice.getCacheInstance("portal.Profile");
      membershipCache = cservice.getCacheInstance("portal.Membership");
      membershipTypeCache = cservice.getCacheInstance("portal.Role");
      groupCache = cservice.getCacheInstance("portal.Group");
   }

   /**
    * OrganizationCacheHandler  constructor.
    *
    * @param cservice
    *          cache service
    * @param lifeTime
    *          lifetime cache param
    */
   public OrganizationCacheHandler(CacheService cservice, long lifeTime)
   {
      this(cservice);

      userCache.setLiveTime(lifeTime);
      userProfileCache.setLiveTime(lifeTime);
      membershipCache.setLiveTime(lifeTime);
      membershipTypeCache.setLiveTime(lifeTime);
      groupCache.setLiveTime(lifeTime);
   }

   /**
    * Returns user profile cache.
    *
    * @return user profile cache
    */
   public ExoCache getUserProfileCache()
   {
      return userProfileCache;
   }

   /**
    * Returns user cache.
    *
    * @return user cache
    */
   public ExoCache getUserCache()
   {
      return userCache;
   }

   /**
    * Returns membership type cache.
    *
    * @return membership type cache
    */
   public ExoCache getMembershipTypeCache()
   {
      return membershipTypeCache;
   }

   /**
    * Returns membership cache.
    *
    * @return membership cache
    */
   public ExoCache getMembershipCache()
   {
      return membershipCache;
   }

   /**
    * Returns group cache.
    *
    * @return group cache
    */
   public ExoCache getGroupCache()
   {
      return groupCache;
   }
}
