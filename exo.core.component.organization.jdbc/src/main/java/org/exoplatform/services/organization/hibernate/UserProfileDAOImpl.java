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
package org.exoplatform.services.organization.hibernate;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileEventListenerHandler;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.UserProfileData;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Aug 22, 2003 Time: 4:51:21 PM
 */
public class UserProfileDAOImpl implements UserProfileHandler, UserProfileEventListenerHandler
{
   static private UserProfile NOT_FOUND = new UserProfileImpl();

   private static final String queryFindUserProfileByName =
      "from u in class org.exoplatform.services.organization.impl.UserProfileData " + "where u.userName = ?";

   private HibernateService service_;

   private ExoCache cache_;

   private List<UserProfileEventListener> listeners_;

   public UserProfileDAOImpl(HibernateService service, CacheService cservice) throws Exception
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(getClass().getName());
      listeners_ = new ArrayList<UserProfileEventListener>(3);
   }

   public void addUserProfileEventListener(UserProfileEventListener listener)
   {
      listeners_.add(listener);
   }

   final public UserProfile createUserProfileInstance()
   {
      return new UserProfileImpl();
   }

   public UserProfile createUserProfileInstance(String userName)
   {
      return new UserProfileImpl(userName);
   }

   void createUserProfileEntry(UserProfile up, Session session) throws Exception
   {
      UserProfileData upd = new UserProfileData();
      upd.setUserProfile(up);
      session.save(upd);
      session.flush();
      cache_.remove(up.getUserName());
   }

   public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      UserProfileData upd =
         (UserProfileData)service_.findOne(session, queryFindUserProfileByName, profile.getUserName());
      if (upd == null)
      {
         upd = new UserProfileData();
         upd.setUserProfile(profile);
         if (broadcast)
            preSave(profile, true);
         session.save(profile.getUserName(), upd);
         if (broadcast)
            postSave(profile, true);
         session.flush();
      }
      else
      {
         upd.setUserProfile(profile);
         if (broadcast)
            preSave(profile, false);
         session.update(upd);
         if (broadcast)
            postSave(profile, false);
         session.flush();
      }
      cache_.put(profile.getUserName(), profile);
   }

   public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      try
      {
         UserProfileData upd = (UserProfileData)service_.findExactOne(session, queryFindUserProfileByName, userName);
         UserProfile profile = upd.getUserProfile();
         if (broadcast)
            preDelete(profile);
         session.delete(upd);
         if (broadcast)
            postDelete(profile);
         session.flush();
         cache_.remove(userName);
         return profile;
      }
      catch (Exception exp)
      {
         return null;
      }
   }

   public UserProfile findUserProfileByName(String userName) throws Exception
   {
      UserProfile up = (UserProfile)cache_.get(userName);
      if (up != null)
      {
         if (NOT_FOUND == up)
            return null;
         return up;
      }
      Session session = service_.openSession();
      up = findUserProfileByName(userName, session);
      if (up != null)
         cache_.put(userName, up);
      else
         cache_.put(userName, NOT_FOUND);
      return up;
   }

   public UserProfile findUserProfileByName(String userName, Session session) throws Exception
   {
      UserProfileData upd = (UserProfileData)service_.findOne(session, queryFindUserProfileByName, userName);
      if (upd != null)
      {
         return upd.getUserProfile();
      }
      return null;
   }

   static void removeUserProfileEntry(String userName, Session session) throws Exception
   {
      Object user = session.createQuery(queryFindUserProfileByName).setString(0, userName).uniqueResult();
      if (user != null)
         session.delete(user);
   }

   public Collection findUserProfiles() throws Exception
   {
      return null;
   }

   private void preSave(UserProfile profile, boolean isNew) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.preSave(profile, isNew);
   }

   private void postSave(UserProfile profile, boolean isNew) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.postSave(profile, isNew);
   }

   private void preDelete(UserProfile profile) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.preDelete(profile);
   }

   private void postDelete(UserProfile profile) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.postDelete(profile);
   }

   /**
    * {@inheritDoc}
    */
   public List<UserProfileEventListener> getUserProfileListeners()
   {
      return Collections.unmodifiableList(listeners_);
   }

}
