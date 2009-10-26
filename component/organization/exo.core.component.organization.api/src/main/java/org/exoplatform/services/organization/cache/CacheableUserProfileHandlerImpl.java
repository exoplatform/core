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
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;

import java.util.Collection;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 2009
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a> 
 * @version $Id$
 */
public class CacheableUserProfileHandlerImpl implements UserProfileHandler
{

   private final ExoCache userProfileCache;

   private final UserProfileHandler userProfileHandler;

   /**
    * CacheableUserHandler  constructor.
    *
    * @param organizationCacheHandler
    *             - organization cache handler
    * @param userProfileHandler
    *             - user profile handler
    */
   public CacheableUserProfileHandlerImpl(OrganizationCacheHandler organizationCacheHandler,
      UserProfileHandler userProfileHandler)
   {
      this.userProfileCache = organizationCacheHandler.getUserProfileCache();
      this.userProfileHandler = userProfileHandler;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserProfileEventListener(UserProfileEventListener listener)
   {
      userProfileHandler.addUserProfileEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile createUserProfileInstance()
   {
      return userProfileHandler.createUserProfileInstance();
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile createUserProfileInstance(String userName)
   {
      return userProfileHandler.createUserProfileInstance(userName);
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile findUserProfileByName(String userName) throws Exception
   {
      UserProfile userProfile = (UserProfile)userProfileCache.get(userName);
      if (userProfile != null)
         return userProfile;

      userProfile = userProfileHandler.findUserProfileByName(userName);
      if (userProfile != null)
         userProfileCache.put(userName, userProfile);

      return userProfile;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findUserProfiles() throws Exception
   {
      Collection<UserProfile> userProfiles = userProfileHandler.findUserProfiles();
      for (UserProfile userProfile : userProfiles)
         userProfileCache.put(userProfile.getUserName(), userProfile);

      return userProfiles;
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception
   {
      UserProfile userProfile = userProfileHandler.removeUserProfile(userName, broadcast);
      if (userProfile != null)
         userProfileCache.remove(userName);

      return userProfile;
   }

   /**
    * {@inheritDoc}
    */
   public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception
   {
      userProfileHandler.saveUserProfile(profile, broadcast);
      userProfileCache.put(profile.getUserName(), profile);
   }

}
