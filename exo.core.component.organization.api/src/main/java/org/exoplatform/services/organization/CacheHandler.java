/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.services.organization;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id: CacheHandler.java 34360 2009-07-22 23:58:59Z tolusha $
 */
public class CacheHandler
{
   public static final String MEMBERSHIPTYPE_PREFIX = "mt=";

   public static final String GROUP_PREFIX = "g=";

   public static final String USER_PREFIX = "u=";

   /**
    * Cache for Users.
    */
   protected final ExoCache<Serializable, User> userCache;

   /**
    * Cache for Users profiles.
    */
   protected final ExoCache<Serializable, UserProfile> userProfileCache;

   /**
    * Cache for MembershipTypes.
    */
   protected final ExoCache<Serializable, MembershipType> membershipTypeCache;

   /**
    * Cache for Memberships.
    */
   protected final ExoCache<Serializable, Membership> membershipCache;

   /**
    * Cache for Groups.
    */
   protected final ExoCache<Serializable, Group> groupCache;

   /**
    * Constructor CacheHandler. 
    * 
    * @param cservice
    *          The cache handler
    */
   public CacheHandler(CacheService cservice)
   {
      this.userCache = cservice.getCacheInstance(this.getClass().getName() + "userCache");
      this.userProfileCache = cservice.getCacheInstance(this.getClass().getName() + "userProfileCache");
      this.membershipTypeCache = cservice.getCacheInstance(this.getClass().getName() + "membershipTypeCache");
      this.groupCache = cservice.getCacheInstance(this.getClass().getName() + "groupCache");
      this.membershipCache = cservice.getCacheInstance(this.getClass().getName() + "membershipCache");
   }

   public void put(Serializable key, Object value, CacheType cacheType)
   {
      key = createInternalKey(key);

      if (cacheType == CacheType.USER)
      {
         userCache.put(key, (User)value);
      }
      else if (cacheType == CacheType.GROUP)
      {
         groupCache.put(key, (Group)value);
      }
      else if (cacheType == CacheType.MEMBERSHIP)
      {
         membershipCache.put(key, (Membership)value);
      }
      else if (cacheType == CacheType.MEMBERSHIPTYPE)
      {
         membershipTypeCache.put(key, (MembershipType)value);
      }
      else if (cacheType == CacheType.USER_PROFILE)
      {
         userProfileCache.put(key, (UserProfile)value);
      }
   }

   public Object get(Serializable key, CacheType cacheType)
   {
      Object obj = null;
      key = createInternalKey(key);

      if (cacheType == CacheType.USER)
      {
         obj = userCache.get(key);
      }
      else if (cacheType == CacheType.GROUP)
      {
         obj = groupCache.get(key);
      }
      else if (cacheType == CacheType.MEMBERSHIP)
      {
         obj = membershipCache.get(key);
      }
      else if (cacheType == CacheType.MEMBERSHIPTYPE)
      {
         obj = membershipTypeCache.get(key);
      }
      else if (cacheType == CacheType.USER_PROFILE)
      {
         obj = userProfileCache.get(key);
      }

      if (obj instanceof ExtendedCloneable)
      {
         return ((ExtendedCloneable)obj).clone();
      }
      return obj;
   }

   public void remove(Serializable key, CacheType cacheType)
   {
      if (cacheType == CacheType.MEMBERSHIP)
      {
         try
         {
            for (Membership m : membershipCache.getCachedObjects())
            {
               String mkey = getMembershipKey(m);
               if (mkey.indexOf((String)key) >= 0)
               {
                  membershipCache.remove(createInternalKey(mkey));
               }
            }
         }
         catch (Exception e)
         {
         }
      }
      else
      {
         key = createInternalKey(key);

         if (cacheType == CacheType.USER)
         {
            userCache.remove(key);
         }
         else if (cacheType == CacheType.GROUP)
         {
            groupCache.remove(key);
         }
         else if (cacheType == CacheType.MEMBERSHIPTYPE)
         {
            membershipTypeCache.remove(key);
         }
         else if (cacheType == CacheType.USER_PROFILE)
         {
            userProfileCache.remove(key);
         }
      }
   }

   public void move(Serializable oldKey, Serializable newKey, CacheType cacheType)
   {
      if (cacheType == CacheType.MEMBERSHIP)
      {
         try
         {
            Map<Serializable, Membership> wait4Adding = new HashMap<Serializable, Membership>();

            for (Membership m : membershipCache.getCachedObjects())
            {
               String mkey = getMembershipKey(m);
               if (mkey.indexOf((String)oldKey) >= 0)
               {
                  wait4Adding.put(createInternalKey(mkey.replace((String)oldKey, (String)newKey)),
                     membershipCache.remove(createInternalKey(mkey)));
               }
            }
            membershipCache.putMap(wait4Adding);
         }
         catch (Exception e)
         {
         }
      }
      else
      {
         oldKey = createInternalKey(oldKey);
         newKey = createInternalKey(newKey);

         if (cacheType == CacheType.USER)
         {
            userCache.put(newKey, userCache.remove(oldKey));
         }
         else if (cacheType == CacheType.GROUP)
         {
            groupCache.put(newKey, groupCache.remove(oldKey));
         }
         else if (cacheType == CacheType.MEMBERSHIPTYPE)
         {
            membershipTypeCache.put(newKey, membershipTypeCache.remove(oldKey));
         }
         else if (cacheType == CacheType.USER_PROFILE)
         {
            userProfileCache.put(newKey, userProfileCache.remove(oldKey));
         }
      }
   }

   public String getMembershipKey(Membership m)
   {
      StringBuilder key = new StringBuilder();
      key.append(GROUP_PREFIX + m.getGroupId());
      key.append(MEMBERSHIPTYPE_PREFIX + m.getMembershipType());
      key.append(USER_PREFIX + m.getUserName());

      return key.toString();
   }

   public String getMembershipKey(String username, String groupId, String type)
   {
      StringBuilder key = new StringBuilder();
      key.append(GROUP_PREFIX + groupId);
      key.append(MEMBERSHIPTYPE_PREFIX + type);
      key.append(USER_PREFIX + username);

      return key.toString();
   }

   public static enum CacheType {
      USER, GROUP, MEMBERSHIP, MEMBERSHIPTYPE, USER_PROFILE
   }

   /**
    * Provide a way of defining cache prefixes for descendant classes.
    * This factory method is executed for each cache operation (put, get, remove)
    */
   protected Serializable createInternalKey(Serializable key)
   {
      return key;
   }
}
