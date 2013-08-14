/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupEventListenerHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestOrganizationService.java 111 2008-11-11 11:11:11Z $
 */
public class TestGroupHandler extends AbstractOrganizationServiceTest
{

   private MyGroupEventListener listener;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      listener = new MyGroupEventListener();
      gHandler.addGroupEventListener(listener);
   }

   @Override
   public void tearDown() throws Exception
   {
      gHandler.removeGroupEventListener(listener);
      super.tearDown();
   }

   /**
    * Find group by id.
    */
   public void testFindGroupById() throws Exception
   {
      Group g = gHandler.findGroupById("/platform/administrators");
      assertNotNull(g);
      assertEquals(g.getDescription(), "the /platform/administrators group");
      assertEquals(g.getGroupName(), "administrators");
      assertEquals(g.getId(), "/platform/administrators");
      assertEquals(g.getLabel(), "Administrators");
      assertEquals(g.getParentId(), "/platform");

      // try to find not existed group. We are supposed to get "null" instead of Exception
      try
      {
         assertNull(gHandler.findGroupById("/not-existed-group"));
      }
      catch (Exception e)
      {
         fail("Exception should be thrown");
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
    * Find groups by user.
    */
   public void testFindGroupsByUser() throws Exception
   {
      assertSizeEquals(3, gHandler.findGroupsOfUser("john"));
      assertSizeEquals(0, gHandler.findGroupsOfUser("fake-user"));

      createMembership(userName, groupName1, membershipType);
      createGroup(null, groupName2, "lable", "desc");
      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName2),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);

      Collection<Group> groups;
      assertSizeEquals(2, groups = gHandler.findGroupsOfUser(userName));
      Set<String> groupNames = new HashSet<String>();
      for (Group g : groups)
      {
         groupNames.add(g.getGroupName());
      }
      assertTrue(groupNames.contains(groupName1));
      assertTrue(groupNames.contains(groupName2));

      Iterator<Group> it = groups.iterator(); 
      gHandler.removeGroup(it.next(), true);
 
      assertSizeEquals(1, gHandler.findGroupsOfUser(userName));

      gHandler.removeGroup(it.next(), true);
 
      assertSizeEquals(0, gHandler.findGroupsOfUser(userName));

      createMembership(userName + "2", groupName2, membershipType + "2");

      assertSizeEquals(1, gHandler.findGroupsOfUser(userName + "2"));

      mHandler.removeMembershipByUser(userName + "2", false);

      assertSizeEquals(0, gHandler.findGroupsOfUser(userName + "2"));

      createMembership(userName + "3", groupName2 + "3", membershipType + "3");

      assertSizeEquals(1, gHandler.findGroupsOfUser(userName + "3"));

      uHandler.removeUser(userName + "3", false);

      assertSizeEquals(0, gHandler.findGroupsOfUser(userName + "3"));

      // Check the listener's counters
      assertEquals(4, listener.preSaveNew);
      assertEquals(4, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(2, listener.preDelete);
      assertEquals(2, listener.postDelete);
   }

   /**
    * Find groups.
    */
   public void testFindGroups() throws Exception
   {
      assertSizeEquals(4, gHandler.findGroups(null));
      assertSizeEquals(2, gHandler.findGroups(gHandler.findGroupById("/organization/operations")));
      assertSizeEquals(0, gHandler.findGroups(gHandler.findGroupById("/organization/management/executive-board")));

      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Get all groups.
    */
   public void testGetAllGroups() throws Exception
   {
      assertSizeEquals(16, gHandler.getAllGroups());

      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
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
      createUser(userName);
      createGroup("/organization", groupName1, "label", "desc");
      createGroup("/organization/" + groupName1, groupName2, "label", "desc");
      
      createMembership(newUserName, groupName2, membershipType);
      assertEquals("We expect to find single membership for user " + newUserName, 1,
         mHandler.findMembershipsByUser(newUserName).size());

      // can not remove group till children exist
      try
      {
         gHandler.removeGroup(gHandler.findGroupById("/organization/group1"), true);
         fail("");
      }
      catch (Exception e)
      {
      }

      gHandler.removeGroup(gHandler.findGroupById("/organization/group1/group2"), true);
      Group group = gHandler.removeGroup(gHandler.findGroupById("/organization/group1"), true);

      assertNull(gHandler.findGroupById("/organization/group1"));
      assertNull(gHandler.findGroupById("/organization/group1/group2"));

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName2), true);
      assertEquals("We expect to find no membership for user " + newUserName, 0,
         mHandler.findMembershipsByUser(newUserName).size());
      
      // try to remove not exited group. Exception should be thrown
      try
      {
         gHandler.removeGroup(group, true);
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }

      // create in root
      createGroup(null, groupName1, "label", "desc");
      createGroup("/" + groupName1, groupName2, "label", "desc");

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName1 + "/" + groupName2), true);
      gHandler.removeGroup(gHandler.findGroupById("/" + groupName1), true);
      assertNull(gHandler.findGroupById("/" + groupName1));
      assertNull(gHandler.findGroupById("/" + groupName1 + "/" + groupName2));

      // Check the listener's counters
      assertEquals(5, listener.preSaveNew);
      assertEquals(5, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(5, listener.preDelete);
      assertEquals(5, listener.postDelete);
   }

   /**
    * Add child.
    */
   public void testAddChild() throws Exception
   {
      Group parent = createGroupInstance(null, groupName1, "lable", "desc");
      Group child = createGroupInstance(null, groupName2, "lable", "desc");

      // try to add child to not existed parent group
      try
      {
         gHandler.addChild(parent, child, false);
         fail("Exception should be thrown.");
      }
      catch (Exception e)
      {
      }

      // add parent group
      gHandler.addChild(null, parent, false);
      assertNotNull(gHandler.findGroupById("/" + groupName1));

      // add child group
      gHandler.addChild(parent, child, false);
      assertNotNull(gHandler.findGroupById("/" + groupName1 + "/" + groupName2));

      // Check the listener's counters
      assertEquals(2, listener.preSaveNew);
      assertEquals(2, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(2, listener.preDelete);
      assertEquals(2, listener.postDelete);
   }

   /**
    * Create group.
    */
   public void testCreateGroup() throws Exception
   {
      Group group = gHandler.createGroupInstance();
      group.setGroupName(groupName1);
      group.setLabel("label");
      gHandler.createGroup(group, true);

      assertNotNull(gHandler.findGroupById("/" + groupName1));
   }

   /**
    * Save group.
    */
   public void testSaveGroup() throws Exception
   {
      createGroup(null, groupName1, "label", "desc");

      // set new description
      Group g = gHandler.findGroupById("/" + groupName1);
      g.setDescription("newDesc");
      gHandler.saveGroup(g, true);

      // check if group has new description
      g = gHandler.findGroupById("/" + groupName1);
      assertEquals(g.getDescription(), "newDesc");

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(1, listener.preSave);
      assertEquals(1, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Test get listeners.
    */
   public void testGetListeners() throws Exception
   {
      if (gHandler instanceof GroupEventListenerHandler)
      {
         List<GroupEventListener> list = ((GroupEventListenerHandler) gHandler).getGroupListeners();
         try
         {
            list.clear();
            fail("We are not supposed to be able to change list of listeners.");
         }
         catch (Exception e)
         {
         }
      }
   }

   private static class MyGroupEventListener extends GroupEventListener
   {
      public int preSaveNew, postSaveNew;
      public int preSave, postSave;
      public int preDelete, postDelete;

      @Override
      public void preSave(Group group, boolean isNew) throws Exception
      {
         if (group == null)
            return;
         if (isNew)
            preSaveNew++;
         else
            preSave++;
      }

      @Override
      public void postSave(Group group, boolean isNew) throws Exception
      {
         if (group == null)
            return;
         if (isNew)
            postSaveNew++;
         else
            postSave++;
      }

      @Override
      public void preDelete(Group group) throws Exception
      {
         if (group == null)
            return;
         preDelete++;
      }

      @Override
      public void postDelete(Group group) throws Exception
      {
         if (group == null)
            return;
         postDelete++;
      }
   }

   public void testFindGroupByMembership() throws Exception
   {
      createMembership(userName, groupName1, membershipType);
      createGroup(null, groupName2, "lable", "desc");
      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName2),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, false);
      Collection<Group> groups = gHandler.findGroupByMembership(userName, membershipType);
      assertNotNull(groups);
      assertEquals(1, groups.size());
      assertEquals(groupName1, groups.iterator().next().getGroupName());

      groups = gHandler.findGroupByMembership(userName, MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName());
      assertNotNull(groups);
      assertEquals(1, groups.size());
      assertEquals(groupName2, groups.iterator().next().getGroupName());
   }

   public void testResolveGroupByMembership() throws Exception
   {
      createMembership(userName, groupName1, membershipType);
      createGroup(null, groupName2, "lable", "desc");
      mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName2),
         MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, false);
      Collection<Group> groups = gHandler.resolveGroupByMembership(userName, membershipType);
      assertNotNull(groups);
      assertEquals(2, groups.size());
      Set<String> groupNames = new HashSet<String>();
      for (Group g : groups)
      {
         groupNames.add(g.getGroupName());
      }
      assertTrue(groupNames.contains(groupName1));
      assertTrue(groupNames.contains(groupName2));

      groups = gHandler.resolveGroupByMembership(userName, MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName());
      assertNotNull(groups);
      assertEquals(1, groups.size());
      assertEquals(groupName2, groups.iterator().next().getGroupName());
   }
}
