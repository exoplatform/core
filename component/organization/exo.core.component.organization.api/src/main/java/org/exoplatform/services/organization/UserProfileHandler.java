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
package org.exoplatform.services.organization;

import java.util.Collection;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 13, 2005 This interface is acted as a sub
 * interface of the organization service. It is used to manage the the
 * UserProfile record, the extra information of an user such address, phone...
 * The interface shoudl allow the developer create , delete and update a
 * UserProfile. and broadcast the event to the user profile event listeners.
 */
public interface UserProfileHandler
{
   /**
    * @return return a new UserProfile implementation instance. This instance is
    *         not persited yet
    */
   public UserProfile createUserProfileInstance();

   /**
    * @return return a new UserProfile implementation instance. This instance is
    *         not persited yet
    * @param userName The user profile record with the username
    */
   public UserProfile createUserProfileInstance(String userName);

   /**
    * This method should persist the profile instance to the database. If the
    * profile is not existed yet. the method should create a new user profile
    * record. If there is an existed record. The method should merge the data
    * with the existed record
    * 
    * @param profile the profile instance to persist.
    * @param broadcast broadcast the event to the listener if broadcast is true
    * @throws Exception throw exception if the method fail to access the database
    *           or any listener fail to handle the event.
    */
   public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception;

   /**
    * This method should remove the user profile record in the database. If any
    * listener fail to handle event. The record should not be removed from the
    * database.
    * 
    * @param userName The user profile record with the username should be removed
    *          from the database
    * @param broadcast Broadcast the event the listeners if broadcast is true.
    * @return The UserProfile instance that has been removed.
    * @throws Exception Throw exception if the method fail to remove the record
    *           or any listener fail to handle the event TODO Should we provide
    *           this method or the user profile should be removed only when the
    *           user is removed
    */
   public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception;

   /**
    * This method should search for and return UserProfile record according to
    * the username
    * 
    * @param userName
    * @return return null if no record match the userName. return an UserProfile
    *         instance if a record match the username.
    * @throws Exception Throw Exception if the method fail to access the database
    *           or find more than one record that match the username.
    * @see UserProfile
    */
   public UserProfile findUserProfileByName(String userName) throws Exception;

   /**
    * Find and return all the UserProfile record in the database
    * 
    * @return
    * @throws Exception Throw exception if the method fail to access the database
    */
   public Collection findUserProfiles() throws Exception;

   /**
    * When a method save , remove are called , the will broadcast an event. You
    * can use this method to register a listener to catch those events
    * 
    * @param listener The listener instance
    * @see UserProfileEventListener
    */
   public void addUserProfileEventListener(UserProfileEventListener listener);
}
