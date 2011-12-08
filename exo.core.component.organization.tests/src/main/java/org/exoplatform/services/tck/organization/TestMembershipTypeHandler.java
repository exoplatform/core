/**
 * 
 */
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

import org.exoplatform.services.organization.MembershipType;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMembershipTypeHandlerImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestMembershipTypeHandler extends AbstractOrganizationServiceTest
{

   /**
    * Find membership type.
    */
   public void testFindMembershipType() throws Exception
   {
      MembershipType mt = mtHandler.findMembershipType("manager");
      assertNotNull(mt);
      assertEquals(mt.getName(), "manager");
      assertEquals(mt.getDescription(), "manager membership type");

      // try to find not existed membership type
      assertNull(mtHandler.findMembershipType("manager_"));
   }

   /**
    * Find membership types.
    */
   public void testFindMembershipTypes() throws Exception
   {
      assertEquals(mtHandler.findMembershipTypes().size(), 3);
      
      createMembershipType("*", "All membership types");
      assertEquals(mtHandler.findMembershipTypes().size(), 4);
   }

   /**
    * Remove membership type.
    */
   public void testRemoveMembershipType() throws Exception
   {
      createMembership(userName, groupName1, membershipType);
      assertEquals("We expect to find single membership for user " + userName, 1,
         mHandler.findMembershipsByUser(userName).size());

      MembershipType mt = mtHandler.removeMembershipType("type", true);
      assertEquals(mt.getName(), membershipType);
      assertNull(mtHandler.findMembershipType("type"));
      assertEquals("We expect to find no membership for user " + userName, 0, mHandler.findMembershipsByUser(userName)
         .size());

      // try to remove not existed membership type. We are supposed to get "null" instead of Exception
      try
      {
         assertNull(mtHandler.removeMembershipType("not-existed-mt", true));
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }
   }

   /**
    * Save membership type.
    */
   public void testSaveMembershipType() throws Exception
   {
      createMembershipType(membershipType, "desc");
      MembershipType mt = mtHandler.findMembershipType(membershipType);

      // change description
      mt.setDescription("newDesc");
      mtHandler.saveMembershipType(mt, true);

      mt = mtHandler.findMembershipType(membershipType);
      assertEquals(mt.getDescription(), "newDesc");
   }

}
