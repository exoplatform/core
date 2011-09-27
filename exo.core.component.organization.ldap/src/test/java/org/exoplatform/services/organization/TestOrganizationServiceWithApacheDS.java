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

import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
import org.apache.directory.server.unit.AbstractServerTest;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.StandaloneContainer;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham
 * hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 27, 2005
 */

@SuppressWarnings("unchecked")
public class TestOrganizationServiceWithApacheDS extends AbstractServerTest
{

   static String GroupParent = "GroupParent";

   static String Group1 = "Group1";

   static String Group2 = "Group2";

   static String Benj = "Benj";

   static String Tuan = "Tuan";

   static String TestMembershipType1 = "TestMembership1";

   static String TestMembershipType2 = "TestMembership2";

   static String TestMembershipType3 = "TestMembership3";

   OrganizationService service_;

   UserHandler userHandler_;

   UserProfileHandler profileHandler_;

   GroupHandler groupHandler_;

   MembershipTypeHandler mtHandler_;

   MembershipHandler membershipHandler_;

   boolean runtest = true;

   public void setUp() throws Exception
   {
      if (!runtest)
      {
         return;
      }

      MutablePartitionConfiguration pcfg = new MutablePartitionConfiguration();

      pcfg.setName("eXoTestPartition");
      pcfg.setSuffix("dc=exoplatform,dc=org");

      Set<String> indexedAttrs = new HashSet<String>();
      indexedAttrs.add("objectClass");
      indexedAttrs.add("o");
      pcfg.setIndexedAttributes(indexedAttrs);

      Attributes attrs = new BasicAttributes(true);
      Attribute attr = new BasicAttribute("objectClass");
      attr.add("top");
      attr.add("organization");
      attrs.put(attr);
      attr = new BasicAttribute("o");
      attr.add("eXoTestPartition");
      attrs.put(attr);
      pcfg.setContextEntry(attrs);
      Set<MutablePartitionConfiguration> pcfgs = new HashSet<MutablePartitionConfiguration>();
      pcfgs.add(pcfg);
      configuration.setContextPartitionConfigurations(pcfgs);
      File workingDirectory = new File("server-work");
      configuration.setWorkingDirectory(workingDirectory);
      super.setUp();

      String containerConf =
         TestOrganizationServiceWithApacheDS.class.getResource("/conf/standalone/test-configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      StandaloneContainer container = StandaloneContainer.getInstance();

      service_ = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      userHandler_ = service_.getUserHandler();
      profileHandler_ = service_.getUserProfileHandler();
      groupHandler_ = service_.getGroupHandler();
      mtHandler_ = service_.getMembershipTypeHandler();
      membershipHandler_ = service_.getMembershipHandler();
   }

   public void tearDown() throws Exception
   {
      if (!runtest)
      {
         return;
      }
      mtHandler_.removeMembershipType(TestMembershipType1, true);
      mtHandler_.removeMembershipType(TestMembershipType2, true);
      mtHandler_.removeMembershipType(TestMembershipType3, true);

      Group gr = groupHandler_.findGroupById("/" + Group1);
      if (gr != null)
      {
         groupHandler_.removeGroup(gr, true);
      }

      gr = groupHandler_.findGroupById("/" + Group2);
      if (gr != null)
      {
         groupHandler_.removeGroup(gr, true);
      }

      gr = groupHandler_.findGroupById("/" + GroupParent);
      if (gr != null)
      {
         groupHandler_.removeGroup(gr, true);
      }

      userHandler_.removeUser(Benj, true);
      userHandler_.removeUser(Tuan, true);
   }

   protected String getDescription()
   {
      if (!runtest)
      {
         return "";
      }

      return "Test hibernate organization service.";
   }

   public void testUserPageSize() throws Exception
   {
      if (!runtest)
      {
         return;
      }

      /* Create an user with UserName: test */
      String USER = "test";
      int s = 15;

      for (int i = 0; i < s; i++)
      {
         createUser(USER + "_" + String.valueOf(i));
      }

      Query query = new Query();
      PageList users = userHandler_.findUsers(query);
      System.out.println("size: " + users.getAvailablePage());

      System.out.println("\npage 1:");
      List list = users.getPage(1);
      System.out.println("size : " + list.size());
      for (Object ele : list)
      {
         User u = (User)ele;
         System.out.println(u.getUserName() + " and " + u.getEmail());
      }
      System.out.println("\n\n");
      //
      try
      {
         for (int i = 0; i < s; i++)
            userHandler_.removeUser(USER + "_" + String.valueOf(i), true);
      }
      catch (Exception exp)
      {
         exp.printStackTrace();
      }
   }

   public void testUser() throws Exception
   {
      /* Create an user with UserName: test */
      String USER = "test";
      User user = createUser(USER);

      // authentication
      user.setPassword("test");
      userHandler_.saveUser(user, true);
      assertTrue("Authentication failed ", userHandler_.authenticate(USER, "test"));

      User u = userHandler_.findUserByName(USER);
      assertTrue("Found user instance", u != null);
      assertEquals("Expect user name is: ", USER, u.getUserName());

      UserProfile up = profileHandler_.createUserProfileInstance(USER);
      profileHandler_.saveUserProfile(up, true);

      up = profileHandler_.findUserProfileByName(USER);
      assertTrue("Expect user profile is found: ", profileHandler_.findUserProfileByName(USER) != null);

      // Update user's information
      u.setFirstName("Exo(Update)");
      userHandler_.saveUser(u, false);
      up.getUserInfoMap().put("user.gender", "male");
      profileHandler_.saveUserProfile(up, true);
      up = profileHandler_.findUserProfileByName(USER);
      assertEquals("expect first name is", "Exo(Update)", u.getFirstName());
      assertEquals("Expect profile is updated: user.gender is ", "male", up.getUserInfoMap().get("user.gender"));

      // Remove a user: Expect result: user and it's profile will be removed
      // NOTE >>>> FIX without listeners remove profile manually
      userHandler_.removeUser(USER, true);
      profileHandler_.removeUserProfile(USER, true);
      assertEquals(null, userHandler_.findUserByName(USER));
      assertTrue(profileHandler_.findUserProfileByName(USER) == null);
   }

   public void testGroup() throws Exception
   {
      if (!runtest)
      {
         return;
      }

      Group groupParent = groupHandler_.createGroupInstance();
      groupParent.setGroupName(GroupParent);
      groupParent.setDescription("This is description");
      groupHandler_.addChild(null, groupParent, true);
      assertTrue(((Group)groupParent).getId() != null);

      groupParent = groupHandler_.findGroupById(groupParent.getId());
      assertEquals(groupParent.getGroupName(), "GroupParent");

      /* Create a child group with name: Group1 */
      Group groupChild = groupHandler_.createGroupInstance();
      groupChild.setGroupName(Group1);
      groupHandler_.addChild(groupParent, groupChild, true);
      groupChild = groupHandler_.findGroupById(groupChild.getId());
      assertEquals(groupChild.getParentId(), groupParent.getId());
      assertEquals("Expect group child's name is: ", Group1, groupChild.getGroupName());

      /* Update groupChild's information */
      groupChild.setLabel("GroupRenamed");
      groupChild.setDescription("new description ");
      groupHandler_.saveGroup(groupChild, true);
      assertEquals(groupHandler_.findGroupById(groupChild.getId()).getLabel(), "GroupRenamed");

      /* Create a group child with name is: Group2 */
      groupChild = groupHandler_.createGroupInstance();
      groupChild.setGroupName(Group2);
      groupHandler_.addChild(groupParent, groupChild, true);
      groupChild = groupHandler_.findGroupById(groupChild.getId());
      assertEquals(groupChild.getParentId(), groupParent.getId());
      assertEquals("Expect group child's name is: ", Group2, groupChild.getGroupName());

      Collection groups = groupHandler_.findGroups(groupParent);
      assertEquals("Expect number of child group in parent group is: ", 2, groups.size());
      Object arraygroups[] = groups.toArray();
      assertEquals("Expect child group's name is: ", Group1, ((Group)arraygroups[0]).getGroupName());
      assertEquals("Expect child group's name is: ", Group2, ((Group)arraygroups[1]).getGroupName());

      groupHandler_.removeGroup(groupHandler_.findGroupById("/" + GroupParent + "/" + Group1), true);
      assertEquals("Expect child group has been removed: ", null, groupHandler_.findGroupById("/" + Group1));
      assertEquals("Expect only 1 child group in parent group", 1, groupHandler_.findGroups(groupParent).size());

      groupHandler_.removeGroup(groupParent, true);
      assertEquals("Expect ParentGroup is removed:", null, groupHandler_.findGroupById(groupParent.getId()));
      assertEquals("Expect all child group is removed: ", 0, groupHandler_.findGroups(groupParent).size());
   }

   public void testMembershipType() throws Exception
   {
      if (!runtest)
      {
         return;
      }

      int bmn = mtHandler_.findMembershipTypes().size();

      MembershipType mt = mtHandler_.createMembershipTypeInstance();
      mt.setName(TestMembershipType1);
      mt.setDescription("This is a test");
      mt.setOwner("exo");
      mtHandler_.createMembershipType(mt, true);
      assertEquals("Expect mebershiptype is:", TestMembershipType1, mtHandler_.findMembershipType(TestMembershipType1)
         .getName());

      String desc = "This is a test (update)";
      mt.setDescription(desc);
      mtHandler_.saveMembershipType(mt, true);
      assertEquals("Expect membershiptype's description", desc, mtHandler_.findMembershipType(TestMembershipType1)
         .getDescription());

      mt = mtHandler_.createMembershipTypeInstance();
      mt.setName(TestMembershipType2);
      mt.setOwner("exo");
      mtHandler_.createMembershipType(mt, true);

      Collection ms = mtHandler_.findMembershipTypes();
      assertEquals("Expect " + (bmn + 2) + " membership in collection: ", bmn + 2, ms.size());

      mtHandler_.removeMembershipType(TestMembershipType1, true);
      assertEquals("Membership type has been removed:", null, mtHandler_.findMembershipType(TestMembershipType1));
      assertEquals("Expect " + (bmn + 1) + " membership in collection(1 is default): ", bmn + 1, mtHandler_
         .findMembershipTypes().size());

      mtHandler_.removeMembershipType(TestMembershipType2, true);
      assertEquals("Membership type has been removed:", null, mtHandler_.findMembershipType(TestMembershipType2));
      assertEquals("Expect  " + bmn + "  membership in collection(default type): ", bmn, mtHandler_
         .findMembershipTypes().size());

   }

   public void testMembership() throws Exception
   {
      if (!runtest)
      {
         return;
      }

      User user = createUser(Benj);
      User user2 = createUser(Tuan);

      Group group1 = groupHandler_.createGroupInstance();
      group1.setGroupName(Group1);
      groupHandler_.addChild(null, group1, true);

      Group group2 = groupHandler_.createGroupInstance();
      group2.setGroupName(Group2);
      groupHandler_.addChild(null, group2, true);

      MembershipType mt1 = mtHandler_.createMembershipTypeInstance();
      mt1.setName(TestMembershipType1);
      mtHandler_.createMembershipType(mt1, true);

      membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group1), mt1, true);
      membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group2), mt1, true);
      membershipHandler_.linkMembership(user2, groupHandler_.findGroupById("/" + Group2), mt1, true);

      MembershipType mt2 = mtHandler_.createMembershipTypeInstance();
      mt2.setName(TestMembershipType2);
      mtHandler_.createMembershipType(mt2, true);
      membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group2), mt2, true);

      MembershipType mt3 = mtHandler_.createMembershipTypeInstance();
      mt3.setName(TestMembershipType3);
      mtHandler_.createMembershipType(mt3, true);
      membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group2), mt3, true);

      System.out.println(" --------- find memberships by group -------------");
      Collection<Membership> mems =
         membershipHandler_.findMembershipsByGroup(groupHandler_.findGroupById("/" + Group2));
      assertEquals("Expect number of membership in group 2 is: ", 4, mems.size());
      for (Membership m : mems)
      {
         System.out.println(m);
      }

      System.out.println(" --------- find memberships by user and group--------------");
      mems = membershipHandler_.findMembershipsByUserAndGroup(Benj, "/" + Group2);
      assertEquals("Expect number of membership in " + Group2 + " relate with benj is: ", 3, mems.size());
      for (Membership m : mems)
      {
         System.out.println(m);
      }

      System.out.println(" --------- find memberships by user-------------");
      mems = membershipHandler_.findMembershipsByUser(Benj);
      assertEquals("expect membership is: ", 4, mems.size());
      for (Membership m : mems)
      {
         System.out.println(m);
      }

      System.out.println("---------- find membership by User, Group and Type-----------");
      Membership membership =
         membershipHandler_.findMembershipByUserGroupAndType(Benj, "/" + Group2, TestMembershipType1);
      assertTrue("Expect membership is found:", membership != null);
      assertEquals("Expect membership type is: ", TestMembershipType1, membership.getMembershipType());
      assertEquals("Expect groupId of this membership is: ", "/" + Group2, membership.getGroupId());
      assertEquals("Expect user of this membership is: ", Benj, membership.getUserName());

      System.out.println(" --------- find groups by user -------------");
      Collection<Group> groups = groupHandler_.findGroupsOfUser(Benj);
      assertEquals("expect group is: ", 2, groups.size());
      for (Group g : groups)
      {
         System.out.println(g);
      }

      System.out.println("---------- find group of a user by membership-----------");
      groups = groupHandler_.findGroupByMembership(Benj, TestMembershipType1);
      assertEquals("expect group is: ", 2, groups.size());
      for (Group g : groups)
      {
         System.out.println(g);
      }

      System.out.println("----------------- removed a membership ---------------------");
      String memId =
         membershipHandler_.findMembershipByUserGroupAndType(Benj, "/" + Group2, TestMembershipType3).getId();
      for (Group g : groups)
      {
         System.out.println(g);
      }
      membershipHandler_.removeMembership(memId, true);
      assertTrue("Membership was removed: ",
         membershipHandler_.findMembershipByUserGroupAndType(Benj, "/" + Group2, TestMembershipType3) == null);
      for (Group g : groups)
      {
         System.out.println(g);
      }

      System.out.println("----------------- removed a user----------------------");
      userHandler_.removeUser(Tuan, true);
      assertTrue("This user was removed", userHandler_.findUserByName(Tuan) == null);
      mems = membershipHandler_.findMembershipsByUser(Tuan);
      assertTrue("All membership related with this user was removed:", mems.isEmpty());

      System.out.println("----------------- removed a group------------");
      groupHandler_.removeGroup(groupHandler_.findGroupById("/" + Group1), true);
      assertTrue("This group was removed", groupHandler_.findGroupById("/" + Group1) == null);

      System.out.println("----------------- removed a membershipType------------");
      mtHandler_.removeMembershipType(TestMembershipType1, true);
      assertTrue("This membershipType was removed: ", mtHandler_.findMembershipType(TestMembershipType1) == null);
      // Check all memberships associate with all groups
      // to guarantee that no membership associate with removed membershipType
      groups = groupHandler_.findGroups(groupHandler_.findGroupById("/"));
      for (Group g : groups)
      {
         mems = membershipHandler_.findMembershipsByGroup(g);
         for (Membership m : mems)
         {
            assertFalse("MembershipType of this membership is not: " + TestMembershipType1, m.getMembershipType()
               .equalsIgnoreCase(TestMembershipType1));
         }
      }

   }

   public User createUser(String userName) throws Exception
   {
      User user = userHandler_.findUserByName(userName);
      if (user != null)
      {
         return user;
      }
      user = userHandler_.createUserInstance(userName);
      user.setPassword("default");
      user.setFirstName("default");
      user.setLastName("default");
      user.setEmail("exo@exoportal.org");
      userHandler_.createUser(user, true);
      return user;
   }
}
