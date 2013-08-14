/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.tck.organization;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipEventListenerHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMembershipImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestMembershipHandler extends AbstractOrganizationServiceTest
{
   private MyMembershipEventListener listener;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      listener = new MyMembershipEventListener();
      mHandler.addMembershipEventListener(listener);
   }

   @Override
   public void tearDown() throws Exception
   {
      mHandler.removeMembershipEventListener(listener);
      super.tearDown();
   }
   
   /**
    * Find membership.
    */
   public void testFindMembership() throws Exception
   {
      createMembership(userName, groupName1, membershipType);

      Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
      assertNotNull(m);
      assertEquals(membershipType, m.getMembershipType());
      assertNotNull(m = mHandler.findMembership(m.getId()));
      assertEquals(membershipType, m.getMembershipType());

      // try to find not existed membership. We are supposed to get Exception
      try
      {
         assertNull(mHandler.findMembership("not-existed-id"));
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
         
      }

      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);

      m =
         mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName());
      assertNotNull(m);
      assertEquals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName(), m.getMembershipType());
      assertNotNull(m = mHandler.findMembership(m.getId()));
      assertEquals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName(), m.getMembershipType());

      // Check the listener's counters
      assertEquals(2, listener.preSaveNew);
      assertEquals(2, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Find membership by user and group.
    */
   public void testFindMembershipByUserGroupAndType() throws Exception
   {
      Membership m = mHandler.findMembershipByUserGroupAndType("marry", "/platform/users", "member");

      assertNotNull(m);
      assertEquals(m.getGroupId(), "/platform/users");
      assertEquals(m.getMembershipType(), "member");
      assertEquals(m.getUserName(), "marry");

      // try to find not existed membership. We are supposed to get null instead of Exception
      try
      {
         assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/platform/users", "member"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      try
      {
         assertNull(mHandler.findMembershipByUserGroupAndType("marry", "/" + groupName1, "member"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      try
      {
         assertNull(mHandler.findMembershipByUserGroupAndType("marry", "/platform/users", membershipType));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      createMembership(userName, groupName1, membershipType);

      assertNotNull(m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      mHandler.removeMembership(m.getId(), true);

      assertNull(m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      createMembership(userName + "2", groupName2, membershipType + "2");

      assertNotNull(mHandler.findMembershipByUserGroupAndType(userName + "2", "/" + groupName2, membershipType + "2"));

      mHandler.removeMembershipByUser(userName + "2", true);

      assertNull(mHandler.findMembershipByUserGroupAndType(userName + "2", "/" + groupName2, membershipType + "2"));

      createMembership(userName + "3", groupName2 + "3", membershipType + "3");

      assertNotNull(mHandler.findMembershipByUserGroupAndType(userName + "3", "/" + groupName2 + "3", membershipType + "3"));
 
      uHandler.removeUser(userName + "3", false);

      assertNull(mHandler.findMembershipByUserGroupAndType(userName + "3", "/" + groupName2 + "3", membershipType + "3"));

      createMembership(userName + "4", groupName2 + "4", membershipType + "4");

      assertNotNull(mHandler.findMembershipByUserGroupAndType(userName + "4", "/" + groupName2 + "4", membershipType + "4"));

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName2 + "4"), false);

      assertNull(mHandler.findMembershipByUserGroupAndType(userName + "4", "/" + groupName2 + "4", membershipType + "4"));

      // Check the listener's counters
      assertEquals(4, listener.preSaveNew);
      assertEquals(4, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(2, listener.preDelete);
      assertEquals(2, listener.postDelete);
   }

   /**
    * Find membership by group.
    */
   public void testFindMembershipsByGroup() throws Exception
   {
      Group g = gHandler.findGroupById("/platform/users");
      assertSizeEquals(4, mHandler.findMembershipsByGroup(g));

      // try to find for non-existing group
      g = gHandler.createGroupInstance();
      g.setGroupName(groupName1);
      g.setLabel("label");
      gHandler.addChild(null, g, false);
      assertEquals(g.getId(), gHandler.findGroupById("/" + groupName1).getId());
      g = gHandler.removeGroup(g, false);
      assertSizeEquals(0, mHandler.findMembershipsByGroup(g));


      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Find membership by group.
    */
   public void testFindAllMembershipsByGroup() throws Exception
   {
      Group g = gHandler.findGroupById("/platform/users");
      ListAccess<Membership> memberships = mHandler.findAllMembershipsByGroup(g);
      assertSizeEquals(4, memberships);

      try
      {
         Membership[] m = memberships.load(0, 4);
         assertEquals(4, m.length);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      try
      {
         Membership[] m = memberships.load(1, 2);
         assertEquals(2, m.length);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      try
      {
         Membership[] m = memberships.load(1, 4);
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }

      // try to find for non-existing group
      g = gHandler.createGroupInstance();
      g.setGroupName(groupName1);
      g.setLabel("label");
      gHandler.addChild(null, g, false);
      assertEquals(g.getId(), gHandler.findGroupById("/" + groupName1).getId());
      g = gHandler.removeGroup(g, true);
      assertSizeEquals(0, mHandler.findMembershipsByGroup(g));

      createMembership(userName, groupName1, membershipType);

      g = gHandler.findGroupById("/" + groupName1);

      assertSizeEquals(1, mHandler.findAllMembershipsByGroup(g));

      Membership m;
      assertNotNull(m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      mHandler.linkMembership(uHandler.findUserByName(userName), g, MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);

      assertSizeEquals(2, mHandler.findAllMembershipsByGroup(g));

      Set<String> membershipTypes = new HashSet<String>();
      for (Membership mem : mHandler.findAllMembershipsByGroup(g).load(0, 2))
      {
         membershipTypes.add(mem.getMembershipType());
      }
      assertTrue(membershipTypes.contains(membershipType));
      assertTrue(membershipTypes.contains(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      mHandler.removeMembership(m.getId(), true);

      assertSizeEquals(1, mHandler.findAllMembershipsByGroup(g));

      assertNotNull(m =
         mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      mHandler.removeMembership(m.getId(), true);

      assertSizeEquals(0, mHandler.findAllMembershipsByGroup(g));

      createMembership(userName + "2", groupName2, membershipType + "2");

      g = gHandler.findGroupById("/" + groupName2);

      assertSizeEquals(1, mHandler.findAllMembershipsByGroup(g));

      mHandler.removeMembershipByUser(userName + "2", true);

      assertSizeEquals(0, mHandler.findAllMembershipsByGroup(g));

      createMembership(userName + "3", groupName2 + "3", membershipType + "3");

      g = gHandler.findGroupById("/" + groupName2 + "3");

      assertSizeEquals(1, mHandler.findAllMembershipsByGroup(g));

      uHandler.removeUser(userName + "3", false);

      assertSizeEquals(0, mHandler.findAllMembershipsByGroup(g));

      createMembership(userName + "4", groupName2 + "4", membershipType + "4");

      g = gHandler.findGroupById("/" + groupName2 + "4");

      assertSizeEquals(1, mHandler.findAllMembershipsByGroup(g));

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName2 + "4"), false);

      assertSizeEquals(0, mHandler.findAllMembershipsByGroup(g));

      // Check the listener's counters
      assertEquals(5, listener.preSaveNew);
      assertEquals(5, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(3, listener.preDelete);
      assertEquals(3, listener.postDelete);
   }

   /**
    * Find all memberships by user.
    */
   public void testFindMembershipsByUser() throws Exception
   {
      assertSizeEquals(3, mHandler.findMembershipsByUser("john"));
      assertSizeEquals(0, mHandler.findMembershipsByUser("not-existed-user"));

      createMembership(userName, groupName1, membershipType);

      assertSizeEquals(1, mHandler.findMembershipsByUser(userName));

      Membership m;
      assertNotNull(m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);

      assertSizeEquals(2, mHandler.findMembershipsByUser(userName));

      Set<String> membershipTypes = new HashSet<String>();
      for (Membership mem : mHandler.findMembershipsByUser(userName))
      {
         membershipTypes.add(mem.getMembershipType());
      }
      assertTrue(membershipTypes.contains(membershipType));
      assertTrue(membershipTypes.contains(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      mHandler.removeMembership(m.getId(), true);

      assertSizeEquals(1, mHandler.findMembershipsByUser(userName));

      assertNotNull(m =
         mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      mHandler.removeMembership(m.getId(), true);

      assertSizeEquals(0, mHandler.findMembershipsByUser(userName));

      createMembership(userName + "2", groupName2, membershipType + "2");

      assertSizeEquals(1, mHandler.findMembershipsByUser(userName + "2"));

      mHandler.removeMembershipByUser(userName + "2", true);

      assertSizeEquals(0, mHandler.findMembershipsByUser(userName + "2"));

      createMembership(userName + "3", groupName2 + "3", membershipType + "3");

      assertSizeEquals(1, mHandler.findMembershipsByUser(userName + "3"));

      uHandler.removeUser(userName + "3", false);

      assertSizeEquals(0, mHandler.findMembershipsByUser(userName + "3"));

      createMembership(userName + "4", groupName2 + "4", membershipType + "4");

      assertSizeEquals(1, mHandler.findMembershipsByUser(userName + "4"));

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName2 + "4"), false);

      assertSizeEquals(0, mHandler.findMembershipsByUser(userName + "4"));

      // Check the listener's counters
      assertEquals(5, listener.preSaveNew);
      assertEquals(5, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(3, listener.preDelete);
      assertEquals(3, listener.postDelete);
   }

   /**
    * Find all membership by user and group.
    */
   public void testFindMembershipsByUserAndGroup() throws Exception
   {
      assertSizeEquals(1, mHandler.findMembershipsByUserAndGroup("john", "/platform/users"));

      // try to find not existed membership. We are supposed to get null instead of Exception
      try
      {
         assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup("non-existed-john", "/platform/users"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      try
      {
         assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup("john", "/non-existed-group"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      createMembership(userName, groupName1, membershipType);

      assertSizeEquals(1, mHandler.findMembershipsByUserAndGroup(userName, "/" + groupName1));

      Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
      assertNotNull(m);
      assertEquals(membershipType, m.getMembershipType());

      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);

      assertSizeEquals(2, mHandler.findMembershipsByUserAndGroup(userName, "/" + groupName1));

      Set<String> membershipTypes = new HashSet<String>();
      for (Membership mem : mHandler.findMembershipsByUserAndGroup(userName, "/" + groupName1))
      {
         membershipTypes.add(mem.getMembershipType());
      }
      assertTrue(membershipTypes.contains(membershipType));
      assertTrue(membershipTypes.contains(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      m = mHandler.removeMembership(m.getId(), true);
      assertNotNull(m);
      assertEquals(membershipType, m.getMembershipType());

      assertSizeEquals(1, mHandler.findMembershipsByUserAndGroup(userName, "/" + groupName1));

      m =
         mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName());
      assertNotNull(m);
      assertEquals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName(), m.getMembershipType());

      m = mHandler.removeMembership(m.getId(), true);

      assertNotNull(m);
      assertEquals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName(), m.getMembershipType());

      assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup(userName, "/" + groupName1));

      createMembership(userName + "2", groupName2, membershipType + "2");

      assertSizeEquals(1, mHandler.findMembershipsByUserAndGroup(userName + "2", "/" + groupName2));
 
      mHandler.removeMembershipByUser(userName + "2", true);

      assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup(userName + "2", "/" + groupName2));

      createMembership(userName + "3", groupName2 + "3", membershipType + "3");

      assertSizeEquals(1, mHandler.findMembershipsByUserAndGroup(userName + "3", "/" + groupName2 + "3"));

      uHandler.removeUser(userName + "3", false);

      assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup(userName + "3", "/" + groupName2 + "3"));

      createMembership(userName + "4", groupName2 + "4", membershipType + "4");

      assertSizeEquals(1, mHandler.findMembershipsByUserAndGroup(userName + "4", "/" + groupName2 + "4"));

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName2 + "4"), false);

      assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup(userName + "4", "/" + groupName2 + "4"));

      // Check the listener's counters
      assertEquals(5, listener.preSaveNew);
      assertEquals(5, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(3, listener.preDelete);
      assertEquals(3, listener.postDelete);
 }

   /**
    * Link membership.
    */
   public void testLinkMembership() throws Exception
   {
      createUser(userName);
      createGroup(null, groupName1, "lable", "desc");
      createMembershipType(membershipType, "desc");

      // link membership
      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1), mtHandler
               .findMembershipType(membershipType), true);

      Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
      assertNotNull(m);

      // try to create already existed membership. Exception should not be thrown
      try
      {
         mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
            mtHandler.findMembershipType(membershipType), true);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // we expect only 1 membership record
      assertEquals(1, mHandler.findMembershipsByUser(userName).size());

      // test deprecated memthod create membership
      mHandler.removeMembership(m.getId(), true);
      mHandler.createMembership(m, true);
      m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
      assertNotNull(m);

      // try to link membership with not existed entries. We are supposed to get Exception
      Group group = createGroupInstance(null, "not-existed-group", "lable", "desc");
      try
      {
         mHandler.linkMembership(uHandler.findUserByName(userName), group,
                  mtHandler.findMembershipType(membershipType), true);
         fail("Exception  should be thrown");
      }
      catch (Exception e)
      {
      }

      User user = uHandler.createUserInstance("not-existed-user");
      try
      {
         mHandler.linkMembership(user, gHandler.findGroupById("/" + groupName1), mtHandler
                  .findMembershipType(membershipType), true);
         fail("Exception  should be thrown");
      }
      catch (Exception e)
      {
      }

      MembershipType mt = mtHandler.createMembershipTypeInstance();
      mt.setName("not-existed-mt");
      try
      {
         mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1), mt, true);
         fail("Exception  should be thrown");
      }
      catch (Exception e)
      {
      }

      try
      {
         mHandler.linkMembership(uHandler.findUserByName(userName), null, mtHandler.findMembershipType(membershipType),
                  true);
         fail("Exception  should be thrown");
      }
      catch (Exception e)
      {
      }

      try
      {
         mHandler.linkMembership(null, gHandler.findGroupById("/" + groupName1), mtHandler
                  .findMembershipType(membershipType), true);
         fail("Exception  should be thrown");
      }
      catch (Exception e)
      {
      }

      try
      {
         mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1), null,
                  true);
         fail("Exception  should be thrown");
      }
      catch (Exception e)
      {
      }

      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);
      m =
         mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName());
      assertNotNull(m);

      // Check the listener's counters
      assertEquals(3, listener.preSaveNew);
      assertEquals(3, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Remove membership
    */
   public void testRemoveMembership() throws Exception
   {

      createMembership(userName, groupName1, membershipType);
      Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);

      assertNotNull(m);

      m = mHandler.removeMembership(m.getId(), true);
      assertEquals(m.getGroupId(), "/" + groupName1);
      assertEquals(m.getMembershipType(), membershipType);
      assertEquals(m.getUserName(), userName);

      assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      
      // try to remove not existed membership. We are supposed to get "null" instead of Exception
      try
      {
         assertNull(mHandler.removeMembership("not-existed-id", true));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Remove membership by user.
    */
   public void testRemoveMembershipByUser() throws Exception
   {
      createMembership(userName, groupName1, membershipType);
      assertNotNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);

      assertNotNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      Collection<Membership> memberships;
      assertSizeEquals(2, memberships = mHandler.removeMembershipByUser(userName, true));
      Set<String> membershipNames = new HashSet<String>();
      for (Membership m : memberships)
      {
         membershipNames.add(m.getMembershipType());
      }
      assertTrue(membershipNames.contains(membershipType));
      assertTrue(membershipNames.contains(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));
      assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1,
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()));

      assertSizeEquals(0, mHandler.findMembershipsByUserAndGroup(userName, "/" + groupName1));
      assertSizeEquals(0, mHandler.findMembershipsByUser(userName));

      assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/group", membershipType));

      // try to remove memberships by not existed users. We are supposed to get empty list instead of Exception
      try
      {
         assertSizeEquals(0, mHandler.removeMembershipByUser("not-existed-user", true));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // Check the listener's counters
      assertEquals(2, listener.preSaveNew);
      assertEquals(2, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(2, listener.preDelete);
      assertEquals(2, listener.postDelete);
   }

   /**
    * Find group by membership.
    */
   public void testFindGroupByMembership() throws Exception
   {
      createMembership(userName, groupName1, membershipType);
      assertSizeEquals(1, gHandler.findGroupByMembership(userName, membershipType));

      // try to find groups by not existed entries. We supposed to get empty list instead of Exception
      try
      {
         assertSizeEquals(0, gHandler.findGroupByMembership("not-existed-john", membershipType));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      mHandler.removeMembershipByUser(userName, true);
      try
      {
         assertSizeEquals(0, gHandler.findGroupByMembership(userName, membershipType));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      createMembership(userName + "2", groupName2, "foo");

      assertSizeEquals(1, gHandler.findGroupByMembership(userName + "2", "foo"));

      uHandler.removeUser(userName + "2", false);

      try
      {
         assertSizeEquals(0, gHandler.findGroupByMembership(userName + "2", "foo"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // Check the listener's counters
      assertEquals(2, listener.preSaveNew);
      assertEquals(2, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Find groups of user.
    */
   public void testFindGroupsOfUser() throws Exception
   {
      assertSizeEquals(3, gHandler.findGroupsOfUser("john"));

      // try to find groups by not existed entries. We supposed to get empty list instead of Exception
      try
      {
         assertSizeEquals(0, gHandler.findGroupsOfUser("not-existed-james"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Find users by group.
    */
   public void testFindUsersByGroupId() throws Exception
   {
      ListAccess<User> users = uHandler.findUsersByGroupId("/platform/users");

      assertSizeEquals(4, users);

      for (User u : users.load(0, users.getSize()))
      {
         User currentUrer = uHandler.findUserByName(u.getUserName());
         assertNotNull(currentUrer);
         
         assertEquals(currentUrer.getUserName(), u.getUserName());
         assertEquals(currentUrer.getFirstName(), u.getFirstName());
         assertEquals(currentUrer.getLastName(), u.getLastName());
         assertEquals(currentUrer.getEmail(), u.getEmail());
         assertEquals(currentUrer.getOrganizationId(), u.getOrganizationId());
      }

      // try to find users by not existed entries. We supposed to get empty list instead of Exception
      try
      {
         assertSizeEquals(0, uHandler.findUsersByGroupId("/not-existed-group"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      User[] allPage = users.load(0, 4);
      User[] page1 = users.load(0, 2);
      User[] page2 = users.load(2, 2);

      assertEquals(allPage[0].getUserName(), page1[0].getUserName());
      assertEquals(allPage[1].getUserName(), page1[1].getUserName());
      assertEquals(allPage[2].getUserName(), page2[0].getUserName());
      assertEquals(allPage[3].getUserName(), page2[1].getUserName());

      try
      {
         users.load(0, 0);
      }
      catch (Exception e)
      {
         fail("Exception is not expected");
      }

      // try to load more than exist
      try
      {
         users.load(0, 5);
         fail("Exception is expected");
      }
      catch (Exception e)
      {
      }

      // try to load more than exist
      try
      {
         users.load(1, 4);
         fail("Exception is expected");
      }
      catch (Exception e)
      {
      }

      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Find users by group.
    */
   public void testFindUsersByGroup() throws Exception
   {
      PageList<User> usersList = uHandler.findUsersByGroup("/platform/users");
      
      assertSizeEquals(4, usersList.getAll());

      for (User u : usersList.getAll())
      {
         User currentUrer = uHandler.findUserByName(u.getUserName());
         assertNotNull(currentUrer);

         assertEquals(currentUrer.getUserName(), u.getUserName());
         assertEquals(currentUrer.getFirstName(), u.getFirstName());
         assertEquals(currentUrer.getLastName(), u.getLastName());
         assertEquals(currentUrer.getEmail(), u.getEmail());
         assertEquals(currentUrer.getOrganizationId(), u.getOrganizationId());
      }

      // try to find users by not existed entries. We supposed to get empty list instead of Exception
      try
      {
         assertSizeEquals(0, uHandler.findUsersByGroup("/not-existed-group").getAll());
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Remove membership type.
    */
   public void testRemoveMembershipType() throws Exception
   {
      createMembership(userName, groupName1, membershipType);

      mtHandler.removeMembershipType("type", true);
      assertNull(mtHandler.findMembershipType("type"));
      assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Remove group.
    */
   public void testRemoveGroup() throws Exception
   {
      createMembership(userName, groupName1, membershipType);

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName1), true);

      assertNull(gHandler.findGroupById("/" + groupName1));
      assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));


      // try to remove not existed groups. We are supposed to get Exception
      try
      {
         Group group = createGroupInstance(null, "not-existed-group", "lable", "desc");

         gHandler.removeGroup(group, true);

         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }

      try
      {
         gHandler.removeGroup(null, true);
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Remove user.
    */
   public void testRemoveUser() throws Exception
   {
      String userName = "testRemoveUser";
      createMembership(userName, groupName1, membershipType);

      uHandler.removeUser(userName, true);

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Test get listeners.
    */
   public void testGetListeners() throws Exception
   {
      if (mHandler instanceof MembershipEventListenerHandler)
      {
         List<MembershipEventListener> list = ((MembershipEventListenerHandler) mHandler).getMembershipListeners();
         try
         {
            list.clear();
            fail("We are not supposed to change list of listners");
         }
         catch (Exception e)
         {
         }
      }
   }

   private static class MyMembershipEventListener extends MembershipEventListener
   {
      public int preSaveNew, postSaveNew;
      public int preSave, postSave;
      public int preDelete, postDelete;

      @Override
      public void preSave(Membership m, boolean isNew) throws Exception
      {
         if (m == null)
            return;
         if (!m.getMembershipType().startsWith("type") && !m.getMembershipType().equals("foo")
            && !m.getMembershipType().equals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName()))
         {
            throw new Exception("Unexpected membership type, it should be 'type' or '"
               + MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName() + "' but was " + m.getMembershipType());
         }
         if (isNew)
            preSaveNew++;
         else
            preSave++;
      }

      @Override
      public void postSave(Membership m, boolean isNew) throws Exception
      {
         if (m == null)
            return;
         if (isNew)
            postSaveNew++;
         else
            postSave++;
      }

      @Override
      public void preDelete(Membership m) throws Exception
      {
         if (m == null)
            return;
         preDelete++;
      }

      @Override
      public void postDelete(Membership m) throws Exception
      {
         if (m == null)
            return;
         postDelete++;
      }
   }
}
