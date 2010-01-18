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
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.DBObjectMapper;
import org.exoplatform.services.database.DBObjectQuery;
import org.exoplatform.services.database.ExoDatasource;
import org.exoplatform.services.database.StandardSQLDAO;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;

import java.util.Calendar;
import java.util.List;

/**
 * Created by The eXo Platform SAS Apr 7, 2007
 */
public class UserDAOImpl extends StandardSQLDAO<UserImpl> implements UserHandler
{

   protected static Log log = ExoLogger.getLogger("organization:UserDAOImpl");

   protected ListenerService listenerService_;

   public UserDAOImpl(ListenerService lService, ExoDatasource datasource, DBObjectMapper<UserImpl> mapper)
   {
      super(datasource, mapper, UserImpl.class);
      listenerService_ = lService;
   }

   public User createUserInstance()
   {
      return new UserImpl();
   }

   public User createUserInstance(String username)
   {
      return new UserImpl(username);
   }

   public void createUser(User user, boolean broadcast) throws Exception
   {
      if (log.isDebugEnabled())
         log.debug("----------- CREATE USER " + user.getUserName());
      UserImpl userImpl = (UserImpl)user;
      if (broadcast)
         listenerService_.broadcast(UserHandler.PRE_CREATE_USER_EVENT, this, userImpl);
      super.save(userImpl);
      if (broadcast)
         listenerService_.broadcast(UserHandler.POST_CREATE_USER_EVENT, this, userImpl);
   }

   public boolean authenticate(String username, String password) throws Exception
   {
      User user = findUserByName(username);
      if (user == null)
         return false;

      boolean authenticated = user.getPassword().equals(password);
      if (log.isDebugEnabled())
         log.debug("+++++++++++AUTHENTICATE USERNAME " + username + " AND PASS " + password + " - " + authenticated);
      if (authenticated)
      {
         UserImpl userImpl = (UserImpl)user;
         userImpl.setLastLoginTime(Calendar.getInstance().getTime());
         saveUser(userImpl, false);
      }
      return authenticated;
   }

   public User findUserByName(String userName) throws Exception
   {
      DBObjectQuery<UserImpl> query = new DBObjectQuery<UserImpl>(UserImpl.class);
      query.addLIKE("USER_NAME", userName);
      User user = loadUnique(query.toQuery());;
      if (log.isDebugEnabled())
         log.debug("+++++++++++FIND USER BY USER NAME " + userName + " - " + (user != null));
      return user;
   }

   public LazyPageList<User> findUsers(org.exoplatform.services.organization.Query orgQuery) throws Exception
   {
      return new LazyPageList<User>(findUsersByQuery(orgQuery), 20);
   }

   /**
    * Query( name = "" , standardSQL = "..." oracleSQL = "..." )
    */
   public ListAccess<User> findUsersByQuery(Query orgQuery) throws Exception
   {
      DBObjectQuery dbQuery = new DBObjectQuery<UserImpl>(UserImpl.class);
      dbQuery.addLIKE("USER_NAME", orgQuery.getUserName());
      dbQuery.addLIKE("FIRST_NAME", orgQuery.getFirstName());
      dbQuery.addLIKE("LAST_NAME", orgQuery.getLastName());
      dbQuery.addLIKE("EMAIL", orgQuery.getEmail());
      dbQuery.addGT("LAST_LOGIN_TIME", orgQuery.getFromLoginDate());
      dbQuery.addLT("LAST_LOGIN_TIME", orgQuery.getToLoginDate());

      return new SimpleJDBCUserListAccess(this, dbQuery.toQuery(), dbQuery.toCountQuery());
   }

   public LazyPageList<User> findUsersByGroup(String groupId) throws Exception
   {
      return new LazyPageList<User>(findUsersByGroupId(groupId), 20);
   }

   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception
   {
      if (log.isDebugEnabled())
         log.debug("+++++++++++FIND USER BY GROUP_ID " + groupId);
      PortalContainer manager = PortalContainer.getInstance();
      OrganizationService service = (OrganizationService)manager.getComponentInstanceOfType(OrganizationService.class);
      MembershipHandler membershipHandler = service.getMembershipHandler();
      GroupHandler groupHandler = service.getGroupHandler();
      Group group = groupHandler.findGroupById(groupId);
      @SuppressWarnings("unchecked")
      List<Membership> members = (List<Membership>)membershipHandler.findMembershipsByGroup(group);

      DBObjectQuery dbQuery = new DBObjectQuery<UserImpl>(UserImpl.class);
      for (Membership member : members)
      {
         dbQuery.addLIKE("USER_NAME", member.getUserName());
         /*
               User g = findUserByName(member.getUserName());
               if (g != null)
                 users.add(g);
         */
      }

      return new SimpleJDBCUserListAccess(this, dbQuery.toQueryUseOR(), dbQuery.toCountQueryUseOR());
   }

   public LazyPageList<User> getUserPageList(int pageSize) throws Exception
   {
      return new LazyPageList<User>(findAllUsers(), pageSize);
   }

   public ListAccess<User> findAllUsers() throws Exception
   {
      DBObjectQuery dbQuery = new DBObjectQuery<UserImpl>(UserImpl.class);
      return new SimpleJDBCUserListAccess(this, dbQuery.toQuery(), dbQuery.toCountQuery());
   }

   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      UserImpl userImpl = (UserImpl)findUserByName(userName);
      if (userImpl == null)
         return null;
      if (broadcast)
         listenerService_.broadcast(UserHandler.PRE_DELETE_USER_EVENT, this, userImpl);
      super.remove(userImpl);
      if (broadcast)
         listenerService_.broadcast(UserHandler.POST_DELETE_USER_EVENT, this, userImpl);
      return userImpl;
   }

   public void saveUser(User user, boolean broadcast) throws Exception
   {
      UserImpl userImpl = (UserImpl)user;
      if (broadcast)
         listenerService_.broadcast(UserHandler.PRE_UPDATE_USER_EVENT, this, userImpl);
      super.update(userImpl);
      if (broadcast)
         listenerService_.broadcast(UserHandler.POST_UPDATE_USER_EVENT, this, userImpl);
   }

   @SuppressWarnings("unused")
   public void addUserEventListener(UserEventListener listener)
   {
   }

}
