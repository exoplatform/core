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

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestOrganizationService.java 111 2008-11-11 11:11:11Z $
 */
public class TestGroupHandler extends AbstractOrganizationServiceTest
{

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
   }

   /**
    * Find groups by user.
    */
   public void testFindGroupsByUser() throws Exception
   {
      assertEquals(gHandler.findGroupsOfUser("john").size(), 3);
   }

   /**
    * Find groups.
    */
   public void testFindGroups() throws Exception
   {
      assertEquals(gHandler.findGroups(null).size(), 4);
      assertEquals(gHandler.findGroups(gHandler.findGroupById("/organization/operations")).size(), 2);
      assertEquals(gHandler.findGroups(gHandler.findGroupById("/organization/management/executive-board")).size(), 0);
   }

   /**
    * Get all groups.
    */
   public void testGetAllGroups() throws Exception
   {
      assertEquals(gHandler.getAllGroups().size(), 16);
   }

   /**
    * Remove group.
    */
   public void testRemoveGroup() throws Exception
   {
      createUser(userName);
      createGroup("/organization", groupName1, "label", "desc");
      createGroup("/organization/" + groupName1, groupName1, "label", "desc");
      
      createMembership(newUserName, groupName2, membershipType);
      assertEquals("We expect to find single membership for user " + newUserName, 1,
         mHandler.findMembershipsByUser(newUserName).size());

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

      gHandler.removeGroup(gHandler.findGroupById("/" + groupName1), true);
      assertNull(gHandler.findGroupById("/" + groupName1));
      assertNull(gHandler.findGroupById("/" + groupName1 + "/" + groupName2));
   }

   /**
    * Add child.
    */
   public void testAddChild() throws Exception
   {
      Group parent = gHandler.createGroupInstance();
      parent.setGroupName(groupName1);

      Group child = gHandler.createGroupInstance();
      child.setGroupName(groupName2);

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
   }

   /**
    * Create group.
    */
   public void testCreateGroup() throws Exception
   {
      Group group = gHandler.createGroupInstance();
      group.setGroupName(groupName1);
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
}
