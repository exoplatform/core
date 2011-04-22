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
/*
 * Created on Feb 3, 2005
 */
package org.exoplatform.services.organization.impl.mock;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.exoplatform.services.organization.ExtendedUserHandler;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.services.security.PasswordEncrypter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author benjaminmestrallet
 * 
 * @version $Id: DummyOrganizationService.java 34415 2009-07-23 14:33:34Z dkatayev $
 */
public class DummyOrganizationService extends BaseOrganizationService
{

   public static final String GROUPNAME_PLATFORM = "platform";

   public static final String GROUPID_PLATFORM = "/platform";

   public static final String GROUPNAME_USERS = "users";

   public static final String GROUPID_USERS = "/platform/users";

   public static final String GROUPNAME_ADMINISTRATORS = "administrators";

   public static final String GROUPID_ADMINISTRATORS = "/platform/administrators";
   
   public DummyOrganizationService()
   {
      this.userDAO_ = new UserHandlerImpl();
      this.groupDAO_ = new GroupHandlerImpl();
      this.membershipDAO_ = new MembershipHandlerImpl();
      this.userProfileDAO_ = new DummyUserProfileHandler();
   }

   static public class MembershipHandlerImpl implements MembershipHandler
   {

      public void addMembershipEventListener(MembershipEventListener listener)
      {
      }

      public void createMembership(Membership m, boolean broadcast) throws Exception
      {
      }

      public Membership createMembershipInstance()
      {
         return null;
      }

      public Membership findMembership(String id) throws Exception
      {
         return null;
      }

      public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
      {
         return null;
      }

      public Collection findMembershipsByGroup(Group group) throws Exception
      {
         return null;
      }

      public Collection findMembershipsByUser(String userName) throws Exception
      {
         Collection memberships = new ArrayList();
         if ("root".equals(userName) || "john".equals(userName) || "admin".equals(userName))
         {
            MembershipImpl admin = new MembershipImpl();
            admin.setMembershipType("*");
            admin.setUserName(userName);
            admin.setGroupId(GROUPID_ADMINISTRATORS);
            memberships.add(admin);
         }

         MembershipImpl membership = new MembershipImpl();
         membership.setMembershipType("*");
         membership.setUserName(userName);
         membership.setGroupId(GROUPID_USERS);
         memberships.add(membership);

         return memberships;
      }

      public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
      {
         return null;
      }

      public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception
      {
      }

      public Membership removeMembership(String id, boolean broadcast) throws Exception
      {
         return null;
      }

      public Collection removeMembershipByUser(String username, boolean broadcast) throws Exception
      {
         return null;
      }
   }

   static public class UserHandlerImpl implements UserHandler, ExtendedUserHandler
   {

      private static final int DEFAULT_LIST_SIZE = 6;

      private LazyListImpl users;

      public UserHandlerImpl()
      {

         users = new LazyListImpl();

         User usr = new UserImpl("exo");
         usr.setPassword("exo");
         users.add(usr);

         usr = new UserImpl("admin");
         usr.setPassword("admin");
         users.add(usr);

         // TODO for what?
         usr = new UserImpl("weblogic");
         usr.setPassword("11111111");
         users.add(usr);

         usr = new UserImpl("__anonim");
         users.add(usr);

         // webos users
         usr = new UserImpl("root");
         usr.setPassword("exo");
         users.add(usr);

         usr = new UserImpl("john");
         usr.setPassword("exo");
         users.add(usr);

         usr = new UserImpl("james");
         usr.setPassword("exo");
         users.add(usr);

         usr = new UserImpl("mary");
         usr.setPassword("exo");
         users.add(usr);

         usr = new UserImpl("marry");
         usr.setPassword("exo");
         users.add(usr);

         usr = new UserImpl("demo");
         usr.setPassword("exo");
         users.add(usr);
      }

      public User createUserInstance()
      {
         User usr = new UserImpl();
         users.add(usr);

         return usr;
      }

      public User createUserInstance(String username)
      {
         User usr = new UserImpl(username);
         users.add(usr);

         return usr;
      }

      public void createUser(User user, boolean broadcast) throws Exception
      {
      }

      public void saveUser(User user, boolean broadcast) throws Exception
      {
      }

      public User removeUser(String userName, boolean broadcast) throws Exception
      {
         return null;
      }

      public User findUserByName(String userName) throws Exception
      {
         Iterator<User> it = users.iterator();

         while (it.hasNext())
         {
            User usr = it.next();
            if (usr.getUserName().equals(userName))
            {
               usr.setFirstName("_" + userName);
               usr.setEmail(userName + "@mail.com");
               return usr;
            }
         }

         return null;
      }

      public PageList<User> findUsersByGroup(String groupId) throws Exception
      {
         return new LazyPageList<User>(findUsersByGroupId(groupId), 10);
      }

      public PageList<User> findUsers(Query query) throws Exception
      {
         return new LazyPageList<User>(users, 10);
      }

      public ListAccess<User> findUsersByQuery(Query query) throws Exception
      {
         return users;
      }

      public ListAccess<User> findUsersByGroupId(String groupId) throws Exception {
         LazyListImpl users = new LazyListImpl();
         if (groupId.equals(GROUPID_USERS))
         {
            users.add(new UserImpl("exo"));
            users.add(new UserImpl("marry"));
            users.add(new UserImpl("mary"));
            users.add(new UserImpl("james"));
            users.add(new UserImpl("demo"));
         }
         if (groupId.equals(GROUPID_ADMINISTRATORS))
         {
            users.add(new UserImpl("root"));
            users.add(new UserImpl("john"));
            users.add(new UserImpl("admin"));
         }
         return users;
      }

      public ListAccess<User> findAllUsers() throws Exception
      {
         return users;
      }

      public PageList<User> getUserPageList(int pageSize) throws Exception
      {
         return new LazyPageList<User>(users, 10);
      }

      public void addUserEventListener(UserEventListener listener)
      {
      }

      public boolean authenticate(String username, String password, PasswordEncrypter pe) throws Exception
      {
         Iterator<User> it = users.iterator();
         User usr = null;
         User temp = null;
         while (it.hasNext())
         {
            temp = it.next();
            if (temp.getUserName().equals(username))
            {
               usr = temp;
               break;
            }
         }

         if (usr != null)
         {
            if (usr.getUserName().equals("__anonim"))
            {
               return true;
            }
            if (pe == null)
            {
               if (usr.getPassword().equals(password))
               {
                  return true;
               }
            }
            // passwordContext != null means that digest authentication is used
            else
            {
               // so we need calculate MD5 cast
               String dp = new String(pe.encrypt(usr.getPassword().getBytes()));
               // to compare it to sent by client
               if (dp.equals(password))
               {
                  return true;
               }
            }
         }

         return false;

      }

      public boolean authenticate(String username, String password) throws Exception
      {
         return authenticate(username, password, null);
      }
   }

   public static class GroupHandlerImpl implements GroupHandler
   {
      public Group createGroupInstance()
      {
         return null;
      }

      public void createGroup(Group group, boolean broadcast) throws Exception
      {
      }

      public void addChild(Group parent, Group child, boolean broadcast) throws Exception
      {
      }

      public void saveGroup(Group group, boolean broadcast) throws Exception
      {
      }

      public Group removeGroup(Group group, boolean broadcast) throws Exception
      {
         return null;
      }

      public Collection findGroupByMembership(String userName, String membershipType) throws Exception
      {
         return null;
      }

      public Group findGroupById(String groupId) throws Exception
      {
         if (groupId.equals(GROUPID_ADMINISTRATORS))
         {
            return new DummyGroup(GROUPID_PLATFORM, GROUPID_ADMINISTRATORS, GROUPNAME_ADMINISTRATORS);
         }
         else if (groupId.equals(GROUPID_USERS))
         {
            return new DummyGroup(GROUPID_PLATFORM, GROUPID_USERS, GROUPNAME_USERS);
         }
         else
         {
            // TODO is it right?
            return new DummyGroup("", "/" + groupId, groupId);
         }
      }

      public Collection findGroups(Group parent) throws Exception
      {
         if (parent.getId().equals(GROUPID_PLATFORM))
         {
            List<Group> groups = new ArrayList<Group>();
            groups.add(new DummyGroup(GROUPID_PLATFORM, GROUPID_USERS, GROUPNAME_USERS));
            groups.add(new DummyGroup(GROUPID_PLATFORM, GROUPID_ADMINISTRATORS, GROUPNAME_ADMINISTRATORS));
            return groups;
         }

         return null;
      }

      public void addGroupEventListener(GroupEventListener listener)
      {
      }

      public Collection getAllGroups()
      {
         List<Group> groups = new ArrayList<Group>();
         groups.add(new DummyGroup("", GROUPID_PLATFORM, GROUPNAME_PLATFORM));
         groups.add(new DummyGroup(GROUPID_PLATFORM, GROUPID_USERS, GROUPNAME_USERS));
         groups.add(new DummyGroup(GROUPID_PLATFORM, GROUPID_ADMINISTRATORS, GROUPNAME_ADMINISTRATORS));
         return groups;
      }

      public Collection findGroupsOfUser(String user) throws Exception
      {
         List<Group> groups = new ArrayList<Group>(1);
         if (user.startsWith("exo") || user.equals("demo") || user.equals("mary") || user.equals("marry")
            || user.equals("james"))
         {
            groups.add(new DummyGroup(GROUPID_PLATFORM, GROUPID_USERS, GROUPNAME_USERS));
         }
         else if (user.equals("root") || user.equals("john") || user.equals("admin"))
         {
            groups.add(new DummyGroup(GROUPID_PLATFORM, GROUPID_ADMINISTRATORS, GROUPNAME_ADMINISTRATORS));
         }
         return groups;
      }
   }

   public static class DummyGroup implements Group
   {

      private String id;

      private String parentId;

      private String name;

      private String label;

      private String desc;

      public DummyGroup(String parentId, String id, String name)
      {
         this.name = name;
         this.id = id;
         this.parentId = parentId;
         
         this.desc = "group " + id;
         this.label = name;
      }

      public String getId()
      {
         return id;
      }

      public void setId(String id)
      {
         this.id = id;
         this.desc = "group " + id;
      }

      public String getParentId()
      {
         return parentId;
      }

      public void setParentId(String parentId)
      {
         this.parentId = parentId;
      }

      public String getGroupName()
      {
         return name;
      }

      public void setGroupName(String name)
      {
         this.name = name;
         this.label = name;
      }

      public String getLabel()
      {
         return label;
      }

      public void setLabel(String s)
      {
         label = s;
      }

      public String getDescription()
      {
         return desc;
      }

      public void setDescription(String s)
      {
         desc = s;
      }

      public String toString()
      {
         return "Group[" + id + "|" + name + "]";
      }
   }

   public class DummyUserProfileHandler implements UserProfileHandler
   {

      public void addUserProfileEventListener(UserProfileEventListener listener)
      {
      }

      public UserProfile createUserProfileInstance()
      {
         return new UserProfileImpl();
      }

      public UserProfile createUserProfileInstance(String userName)
      {
         return new UserProfileImpl(userName);
      }

      public UserProfile findUserProfileByName(String userName) throws Exception
      {
         return createUserProfileInstance(userName);
      }

      public Collection findUserProfiles() throws Exception
      {
         return new ArrayList();
      }

      public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception
      {
         return new UserProfileImpl();
      }

      public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception
      {
      }

   }

}
