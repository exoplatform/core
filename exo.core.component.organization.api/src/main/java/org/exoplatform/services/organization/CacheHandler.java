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
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache handler is an entity used to organize access to underlying {@link ExoCache} from Organization Service. 
 * It manages caches for each type of Organization Service entries internally.  
 * <p>
 * Some organization services relies on multiple data-sources, like JCROrgService. 
 * The last one can use multiple repositories. For this case, CacheHandler provides a way for customizing access  
 * to underlying caches for managing multiple sources of data. Caches for data-sources are isolated using key's
 * prefixes. Providing an isolation, CacheHandler doesn't create additional cache instances offering better 
 * memory usage. 
 * <p>
 * Descendant CacheHandler classes that requires caches for multiple data-sources must override following 
 * protected internal menthods:
 * <ul> 
 *    <li>{@link CacheHandler#createCacheKey(Serializable)}<p>- Taking OrgService key as an argument, it generates 
 *    a key, that contains data-source identifier. This key is used as cache key. I.e. it takes "user1" and 
 *    produces "repository2:user1";</li>
 *    <li>{@link CacheHandler#createOrgServiceKey(Serializable)}<p>
 *    - This is an opposite operation to createCacheKey(Serializable), it produces OrgService key trimming 
 *    data-source metadata. I.e. taking "repostiory2:user1" it returns "user1";</li>
 *    <li>{@link CacheHandler#matchKey(Serializable)}<p> - checks if key provided as argument corresponds to 
 *    current cacheHandler instance. I.e. checks if key starts with current prefix.</li>
 * </ul>
 * 
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
      key = createCacheKey(key);

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
      key = createCacheKey(key);

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
            membershipCache.select(new CachedObjectSelector<Serializable, Membership>()
            {
               public boolean select(Serializable cacheKey, ObjectCacheInfo<? extends Membership> ocinfo)
               {
                  return matchKey(cacheKey);
               }

               public void onSelect(ExoCache<? extends Serializable, ? extends Membership> cache,
                  Serializable cacheKey, ObjectCacheInfo<? extends Membership> ocinfo) throws Exception
               {
                  String mkey = getMembershipKey(ocinfo.get());
                  String key = (String)createOrgServiceKey(cacheKey);
                  if ((mkey.indexOf(key) >= 0))
                  {
                     cache.remove(cacheKey);
                  }
               }
            });
         }
         catch (Exception e)
         {
         }
      }
      else
      {
         key = createCacheKey(key);

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
            final Map<Serializable, Membership> wait4Adding = new HashMap<Serializable, Membership>();
            final Serializable finalNewKey = newKey;
            membershipCache.select(new CachedObjectSelector<Serializable, Membership>()
            {
               public boolean select(Serializable cacheKey, ObjectCacheInfo<? extends Membership> ocinfo)
               {
                  return matchKey(cacheKey);
               }

               public void onSelect(ExoCache<? extends Serializable, ? extends Membership> cache,
                  Serializable cacheKey, ObjectCacheInfo<? extends Membership> ocinfo) throws Exception
               {
                  String mkey = getMembershipKey(ocinfo.get());
                  String key = (String)createOrgServiceKey(cacheKey);
                  if ((mkey.indexOf(key) >= 0))
                  {
                     wait4Adding.put(createCacheKey(mkey.replace(key, (String)finalNewKey)),
                        cache.remove(createCacheKey(mkey)));
                  }
               }

            });
            membershipCache.putMap(wait4Adding);
         }
         catch (Exception e)
         {
         }
      }
      else
      {
         oldKey = createCacheKey(oldKey);
         newKey = createCacheKey(newKey);

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
    * 
    * @param orgServiceKey - key used by Organization Service;
    * @return key, used to define cache item.
    */
   protected Serializable createCacheKey(Serializable orgServiceKey)
   {
      return orgServiceKey;
   }

   /**
    * Asserts whether key is managed by current CacheHandler
    * 
    * @param cacheKey 
    * @return
    */
   protected boolean matchKey(Serializable cacheKey)
   {
      return true;
   }

   /**
    * Opposite to createCacheKey(Serializable), retrieving Organization Service key from
    * Cache key, trimming all data-source metadata.
    * 
    * @param cacheKey
    * @return
    */
   protected Serializable createOrgServiceKey(Serializable cacheKey)
   {
      return cacheKey;
   }
}
