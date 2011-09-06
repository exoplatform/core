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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.database.ObjectQuery;
import org.exoplatform.services.organization.ExtendedUserHandler;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserEventListenerHandler;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.security.PasswordEncrypter;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net Date: Aug 22, 2003 Time: 4:51:21 PM
 */
public class UserDAOImpl implements UserHandler, UserEventListenerHandler, ExtendedUserHandler
{
   public static final String queryFindUserByName =
      "from u in class org.exoplatform.services.organization.impl.UserImpl " + "where u.userName = ?";

   private HibernateService service_;

   private ExoCache cache_;

   private List<UserEventListener> listeners_ = new ArrayList<UserEventListener>(3);

   public UserDAOImpl(HibernateService service, CacheService cservice) throws Exception
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(UserImpl.class.getName());
   }

   final public List getUserEventListeners()
   {
      return listeners_;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserEventListener(UserEventListener listener)
   {
      listeners_.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeUserEventListener(UserEventListener listener)
   {
      listeners_.remove(listener);
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
      if (broadcast)
         preSave(user, true);

      final Session session = service_.openSession();
      Transaction transaction = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Transaction>()
      {
         public Transaction run()
         {
            return session.beginTransaction();
         }
      });

      UserImpl userImpl = (UserImpl)user;
      userImpl.setId(user.getUserName());
      session.save(user);
      if (broadcast)
         postSave(user, true);
      transaction.commit();
   }

   public void saveUser(User user, boolean broadcast) throws Exception
   {
      if (broadcast)
         preSave(user, false);
      Session session = service_.openSession();
      session.merge(user);
      // session.update(user);
      if (broadcast)
         postSave(user, false);
      session.flush();
      cache_.put(user.getUserName(), user);
   }

   void createUserEntry(User user, Session session) throws Exception
   {
      session.save(user);
   }

   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      User foundUser = findUserByName(userName, session);
      if (foundUser == null)
         return null;

      if (broadcast)
         preDelete(foundUser);
      session = service_.openSession();
      session.delete(foundUser);
      if (broadcast)
         postDelete(foundUser);
      session.flush();
      cache_.remove(userName);
      return foundUser;
   }

   public User findUserByName(String userName) throws Exception
   {
      User user = (User)cache_.get(userName);
      if (user != null)
         return user;
      Session session = service_.openSession();
      user = findUserByName(userName, session);
      if (user != null)
         cache_.put(userName, user);
      return user;
   }

   public User findUserByName(String userName, Session session) throws Exception
   {
      User user = (User)service_.findOne(session, queryFindUserByName, userName);
      return user;
   }

   public LazyPageList<User> getUserPageList(int pageSize) throws Exception
   {
      return new LazyPageList<User>(findAllUsers(), 20);
   }

   public ListAccess<User> findAllUsers() throws Exception
   {
      String findQuery = "from o in class " + UserImpl.class.getName();
      String countQuery = "select count(o) from " + UserImpl.class.getName() + " o";

      return new SimpleHibernateUserListAccess(service_, findQuery, countQuery);
   }

   public boolean authenticate(String username, String password) throws Exception
   {
      return authenticate(username, password, null);
   }

   public boolean authenticate(String username, String password, PasswordEncrypter pe) throws Exception
   {
      User user = findUserByName(username);
      if (user == null)
      {
         return false;
      }
      
      boolean authenticated;
      if (pe == null)
      {
         authenticated = user.getPassword().equals(password);
      }
      else
      {
         String encryptedPassword = new String(pe.encrypt(user.getPassword().getBytes()));
         authenticated = encryptedPassword.equals(password);
      }
      if (authenticated)
      {
         UserImpl userImpl = (UserImpl)user;
         userImpl.setLastLoginTime(Calendar.getInstance().getTime());
         saveUser(userImpl, false);
      }
      return authenticated;
   }

   public LazyPageList<User> findUsers(Query q) throws Exception
   {
      return new LazyPageList<User>(findUsersByQuery(q), 20);
   }

   public ListAccess<User> findUsersByQuery(Query q) throws Exception
   {
      ObjectQuery oq = new ObjectQuery(UserImpl.class);
      if (q.getUserName() != null)
      {
         oq.addLIKE("UPPER(userName)", q.getUserName().toUpperCase());
      }
      if (q.getFirstName() != null)
      {
         oq.addLIKE("UPPER(firstName)", q.getFirstName().toUpperCase());
      }
      if (q.getLastName() != null)
      {
         oq.addLIKE("UPPER(lastName)", q.getLastName().toUpperCase());
      }
      oq.addLIKE("email", q.getEmail());
      oq.addGT("lastLoginTime", q.getFromLoginDate());
      oq.addLT("lastLoginTime", q.getToLoginDate());

      return new SimpleHibernateUserListAccess(service_, oq.getHibernateQueryWithBinding(), oq
         .getHibernateCountQueryWithBinding(), oq.getBindingFields());
   }

   public LazyPageList<User> findUsersByGroup(String groupId) throws Exception
   {
      return new LazyPageList<User>(findUsersByGroupId(groupId), 20);
   }

   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception
   {
      String queryFindUsersInGroup =
         "select u " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl "
            + "where m.userName = u.userName " + "     and m.groupId =  '" + groupId + "'";
      String countUsersInGroup =
         "select count(u) " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl "
            + "where m.userName = u.userName " + "  and m.groupId =  '" + groupId + "'";

      return new SimpleHibernateUserListAccess(service_, queryFindUsersInGroup, countUsersInGroup);
   }

   public Collection findUsersByGroupAndRole(String groupName, String role) throws Exception
   {
      String queryFindUsersByGroupAndRole =
         "select u " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl, "
            + "     g in class org.exoplatform.services.organization.impl.GroupImpl " + "where m.user = u "
            + "  and m.group = g " + "  and g.groupName = ? " + "  and m.role = ? ";
      Session session = service_.openSession();
      org.hibernate.Query q =
         session.createQuery(queryFindUsersByGroupAndRole).setString(0, groupName).setString(1, role);
      List users = q.list();
      return users;
   }

   private void preSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.preSave(user, isNew);
   }

   private void postSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.postSave(user, isNew);
   }

   private void preDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.preDelete(user);
   }

   private void postDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.postDelete(user);
   }

   /**
    * {@inheritDoc}
    */
   public List<UserEventListener> getUserListeners()
   {
      return Collections.unmodifiableList(listeners_);
   }
}
