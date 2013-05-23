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
 * Created by The eXo Platform SAS <br>
 * This class is acted as a sub  component of the organization service.
 * It is used to manage the user account
 * and broadcast the user event to all the registered listener in the
 * organization service. The user event can be: new user event, update user
 * event and delete user event. Each event should have 2 phases: pre event and
 * post event. The method createUser , saveUser and removeUser broadcast the
 * event at each phase so the listeners can handle the event properly
 * @author <a href="mailto:tuan08@users.sourceforge.net">Tuan Nguyen</a>
 * @LevelAPI Platform
 */
public interface UserHandler
{
   final static public String PRE_DELETE_USER_EVENT = "organization.user.preDelete";

   final static public String POST_DELETE_USER_EVENT = "organization.user.postDelete";

   final static public String PRE_CREATE_USER_EVENT = "organization.user.preCreate";

   final static public String POST_CREATE_USER_EVENT = "organization.user.postCreate";

   final static public String PRE_UPDATE_USER_EVENT = "organization.user.preUpdate";

   final static public String POST_UPDATE_USER_EVENT = "organization.user.postUpdate";

   /**
    * @deprecated This method create an User instance that implement the User
    *             interface. The user instance is not persisted yet
    * @return  User instance
    */
   public User createUserInstance();

   /**
    * This method create an User instance that implement the User interface. The
    * user instance is not persisted yet
    * 
    * @param username Username for new user instance.
    * @return  the user object
    */
   public User createUserInstance(String username);

   /**
    * This method is used to persist a new user object.
    * 
    * @param user The user object to save
    * @param broadcast If the broadcast value is true , then the UserHandler
    *          should broadcast the event to all the listener that register with
    *          the organization service. For example, the portal service register
    *          an user event listener with the organization service. when a new
    *          account is created, a portal configuration should be created for
    *          the new user account at the same time. In this case the portal
    *          user event listener will be called in the createUser method.
    * @throws Exception: The exception can be throwed if the the UserHandler
    *           cannot persist the user object or any listeners fail to handle
    *           the user event.
    */
   public void createUser(User user, boolean broadcast) throws Exception;

   /**
    * This method is used to update an existing User object
    * 
    * @param user The user object to update
    * @param broadcast If the broadcast is true , then all the user event
    *          listener that register with the organization service will be
    *          called
    * @throws Exception The exception can be throwed if the the UserHandler
    *           cannot save the user object or any listeners fail to handle the
    *           user event.
    */
   public void saveUser(User user, boolean broadcast) throws Exception;

   /**
    * Remove an user and broadcast the event to all the registerd listener. When
    * the user is removed , the user profile and all the membershisp of the user
    * should be removed as well.
    * 
    * @param userName The user should be removed from the user database
    * @param broadcast If broadcast is true, the the delete user event should be
    *          broadcasted to all registered listener
    * @return return the User object after that user has beed removed from
    *         database
    * @throws Exception    
    */
   public User removeUser(String userName, boolean broadcast) throws Exception;

   /**
    * @param userName the user that the user handler should search for
    * @return The method return null if there no user matchs the given username.
    *         The method return an User object if an user that mathch the
    *         username.
    * @throws Exception The exception is throwed if the method fail to access the
    *           user database or more than one user object with the same username
    *           is found
    */
   public User findUserByName(String userName) throws Exception;

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
   public PageList<User> findUsersByGroup(String groupId) throws Exception;

   /**
    * This method should search and return the list of the users in a given
    * group.
    *
    * @param groupId id of the group. The return users list should be in this
    *          group
    * @return return a page list iterator of a group of the user in the database
    * @throws Exception any exception
    */
   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception;

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
   public PageList<User> getUserPageList(int pageSize) throws Exception;

   /**
    * This method is used to get all the users in the database
    *
    * @return return a page list iterator. The page list should allow the
    *         developer get all the users or get a page of users if the return
    *         number of users is too large.
    * @throws Exception any exception
    */
   public ListAccess<User> findAllUsers() throws Exception;

   /**
    * This method search for the users accordding to a search criteria, the query
    * 
    * @param query The query object contains the search criteria.
    * @return return the found users in a page list according to the query.
    * @throws Exception throw exception if the service cannot access the database
    * @deprecated use {@link #findUsersByQuery(Query)} instead
    */
   @Deprecated
   public PageList<User> findUsers(Query query) throws Exception;

   /**
    * This method search for the users accordding to a search criteria, the query
    *
    * @param query The query object contains the search criteria.
    * @return return the found users in a page list according to the query.
    * @throws Exception throw exception if the service cannot access the database
    */
   public ListAccess<User> findUsersByQuery(Query query) throws Exception;

   /**
    * Check if the username and the password of an user is valid.
    * 
    * @param username the name of user to authenticate
    * @param password the password of user to authenticate
    * @return return true if the username and the password is match with an user
    *         record in the database, else return false.
    * @throws Exception throw an exception if cannot access the database
    */
   public boolean authenticate(String username, String password) throws Exception;

   /**
    * This method is used to register an user event listener
    * 
    * @param listener the user event listener to register
    */
   public void addUserEventListener(UserEventListener listener);

   /**
    * This method is used to unregister an user event listener
    * 
    * @param listener the user event listener to unregister
    */
   public void removeUserEventListener(UserEventListener listener);
}
