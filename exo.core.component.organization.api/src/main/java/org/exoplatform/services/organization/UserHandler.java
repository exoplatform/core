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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 13, 2005 This class is acted as a sub
 * component of the organization service. It is used to manage the user account
 * and broadcast the user event to all the registered listener in the
 * organization service. The user event can be: new user event, update user
 * event and delete user event. Each event should have 2 phases: pre event and
 * post event. The method createUser , saveUser and removeUser broadcast the
 * event at each phase so the listeners can handle the event properly
 * @LevelAPI Platform
 */
public interface UserHandler
{
   /**
    * @deprecated This method create an User instance that implement the User
    *             interface. The user instance is not persisted yet
    * @return
    */
   User createUserInstance();

   /**
    * This method create an User instance that implement the User interface. The
    * user instance is not persisted yet
    * 
    * @param username Name of the user to use for a new user instance.
    * @return  the user object
    */
   User createUserInstance(String username);

   /**
    * This method is used to persist a new user object.
    * 
    * @param user: The user object to save
    * @param broadcast: If the broadcast value is true , then the UserHandler
    *          should broadcast the event to all the listener that register with
    *          the organization service. For example, the portal service register
    *          an user event listener with the organization service. when a new
    *          account is created, a portal configuration should be created for
    *          the new user account at the same time. In this case the portal
    *          user event listener will be called in the createUser method.
    * @throws Exception: The exception can be thrown if the UserHandler
    *           cannot persist the user object or any listeners fail to handle
    *           the user event.
    */
   void createUser(User user, boolean broadcast) throws Exception;

   /**
    * This method is used to update an existing User object
    * 
    * @param user The user object to update
    * @param broadcast If the broadcast is true , then all the user event
    *          listener that register with the organization service will be
    *          called
    * @throws Exception The exception can be thrown if the UserHandler
    *           cannot save the user object or any listeners fail to handle the
    *           user event.
    * @throws DisabledUserException in case the target user account is disabled
    *         or an other exception prevents to check if the user account is disabled
    */
   void saveUser(User user, boolean broadcast) throws Exception, DisabledUserException;

   /**
    * Remove an user and broadcast the event to all the registered listener. When
    * the user is removed , the user profile and all the membership of the user
    * should be removed as well.
    * 
    * @param userName The user should be removed from the user database
    * @param broadcast If broadcast is true, the delete user event should be
    *          broadcasted to all registered listener
    * @return the User object after that user has been removed from
    *         database
    * @throws Exception The exception can be thrown if the user could not
    *            be removed or any listeners fail to handle the
    *           user event.
    */
   User removeUser(String userName, boolean broadcast) throws Exception;

   /**
    * Enables/Disables the given user. If the user is already enabled/disabled, the
    * method won't do anything, it will simply return the corresponding User object.
    * 
    * @param userName the user name corresponding to the user account 
    *          that we would like to enable/disable
    * @param enabled if set to <code>true</code> the user will be enabled, otherwise
    *          it will be disabled
    * @param broadcast If broadcast is true, the setEnabled user event should be
    *          broadcasted to all registered listener
    * @return the User object after it has been enabled/disabled or <code>null</code>
    *         if the user could not be found.
    * @throws Exception The exception can be thrown if we could not
    *            enable or disable the user or any listeners fail to handle the
    *           user event.
    * @throws UnsupportedOperationException in case the implementation doesn't support
    *            such type of operation.
    */
   User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception, UnsupportedOperationException;

   /**
    * @param userName the user that the user handler should search for. This
    * method is equivalent to <code>findUserByName(userName, true)</code>
    * @return The method return null if there is no user that matches the given username.
    *         The method return an User object if an user matches the
    *         username.
    * @throws Exception The exception is thrown if the method fail to access the
    *           user database or more than one user object with the same username
    *           is found
    */
   User findUserByName(String userName) throws Exception;

   /**
    * @param userName the user that the user handler should search for
    * @param enabledOnly indicates whether only enabled user should be returned
    * @return The method return <code>null</code> if there is no user that matches the 
    *         given username or <code>enabledOnly</code> was set to <code>true</code> 
    *         and the matching user is disabled.
    *         The method return an User object if an user matches the
    *         username.
    * @throws Exception The exception is thrown if the method fail to access the
    *           user database or more than one user object with the same username
    *           is found
    */
   User findUserByName(String userName, boolean enabledOnly) throws Exception;

   /**
    * This method should search and return the list of the users in a given
    * group.
    * 
    * @param groupId id of the group. The return users list should be in this
    *          group
    * @return return a page list iterator of a group of the user in the database
    * @throws Exception
    * @deprecated use {@link #findUsersByGroupId(String)} instead
    */
   @Deprecated
   PageList<User> findUsersByGroup(String groupId) throws Exception;

   /**
    * This method should search and return the list of the users in a given
    * group. This method is equivalent to <code>findUsersByGroupId(groupId, true)</code>
    *
    * @param groupId id of the group. The return users list should be in this
    *          group
    * @return return a page list iterator of a group of the user in the database
    * @throws Exception any exception
    */
   ListAccess<User> findUsersByGroupId(String groupId) throws Exception;

   /**
    * This method should search and return the list of the users in a given
    * group.
    *
    * @param groupId id of the group. The return users list should be in this
    *          group
    * @param enabledOnly indicates whether we expect to have both enabled
    *          and disabled users. If set to <code>true</code> only enabled users
    *          will be returned.
    * @return return a page list iterator of a group of the user in the database
    * @throws Exception any exception
    */
   ListAccess<User> findUsersByGroupId(String groupId, boolean enabledOnly) throws Exception;

   /**
    * This method is used to get all the users in the database
    * 
    * @param pageSize The number of user in each page
    * @return return a page list iterator. The page list should allow the
    *         developer get all the users or get a page of users if the return
    *         number of users is too large.
    * @throws Exception
    * @deprecated use {@link #findAllUsers() } instead
    */
   @Deprecated
   PageList<User> getUserPageList(int pageSize) throws Exception;

   /**
    * This method is used to get all the users in the database
    * This method is equivalent to <code>findAllUsers(true)</code>
    *
    * @return return a page list iterator. The page list should allow the
    *         developer get all the users or get a page of users if the return
    *         number of users is too large.
    * @throws Exception any exception
    */
   ListAccess<User> findAllUsers() throws Exception;

   /**
    * This method is used to get all the users in the database
    *
    * @param enabledOnly indicates whether we expect to have both enabled
    *          and disabled users. If set to <code>true</code> only enabled users
    *          will be returned.
    * @return return a page list iterator. The page list should allow the
    *         developer get all the users or get a page of users if the return
    *         number of users is too large.
    * @throws Exception any exception
    */
   ListAccess<User> findAllUsers(boolean enabledOnly) throws Exception;

   /**
    * This method search for the users according to a search criteria, the query
    * 
    * @param query The query object contains the search criteria.
    * @return return the found users in a page list according to the query.
    * @throws Exception throw exception if the service cannot access the database
    * @deprecated use {@link #findUsersByQuery(Query)} instead
    */
   @Deprecated
   PageList<User> findUsers(Query query) throws Exception;

   /**
    * This method search for the users according to a search criteria, the query
    * This method is equivalent to <code>findUsersByQuery(query, true)</code>
    *
    * @param query The query object contains the search criteria.
    * @return return the found users in a page list according to the query.
    * @throws Exception throw exception if the service cannot access the database
    */
   ListAccess<User> findUsersByQuery(Query query) throws Exception;

   /**
    * This method search for the users according to a search criteria, the query
    *
    * @param query The query object contains the search criteria.
    * @param enabledOnly indicates whether we expect to have both enabled
    *          and disabled users. If set to <code>true</code> only enabled users
    *          will be returned.
    * @return return the found users in a page list according to the query.
    * @throws Exception throw exception if the service cannot access the database
    */
   ListAccess<User> findUsersByQuery(Query query, boolean enabledOnly) throws Exception;

   /**
    * Check if the username and the password of an user is valid.
    * 
    * @param username the name of user to authenticate
    * @param password the password of user to authenticate
    * @return return true if the username and the password is match with an user
    *         record in the database, else return false.
    * @throws Exception throw an exception if cannot access the database
    * @throws DisabledUserException in case the target user account is disabled
    *         or an other exception prevents to check if the user account is disabled
    */
   boolean authenticate(String username, String password) throws Exception, DisabledUserException;

   /**
    * This method is used to register an user event listener
    * 
    * @param listener the user event listener to register
    */
   void addUserEventListener(UserEventListener listener);

   /**
    * This method is used to unregister an user event listener
    * 
    * @param listener the user event listener to unregister
    */
   void removeUserEventListener(UserEventListener listener);
}
