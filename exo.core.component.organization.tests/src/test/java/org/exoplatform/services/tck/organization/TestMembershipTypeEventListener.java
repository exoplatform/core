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
import org.exoplatform.services.organization.MembershipTypeEventListener;
import org.exoplatform.services.organization.MembershipTypeEventListenerHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="dmitry.kuleshov@exoplatform.com">Dmitry Kuleshov</a>
 * @version $Id: TestMembershipTypeHandlerImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestMembershipTypeEventListener extends AbstractOrganizationServiceTest
{

   /**
    * Test get listeners.
    */
   public void testGetListeners() throws Exception
   {
      if (mtHandler instanceof MembershipTypeEventListenerHandler)
      {
         List<MembershipTypeEventListener> list =
            ((MembershipTypeEventListenerHandler)mtHandler).getMembershipTypeListeners();

         try
         {
            list.clear();
            fail("We are not supposed to change list of listeners");
         }
         catch (Exception e)
         {
         }
      }
   }

   /**
    * Test events.
    */
   public void testMembershipTypeEventListener() throws Exception
   {
      TesterMembershipTypeEventListener testListener = new TesterMembershipTypeEventListener();
      int currentSize = 1;
      if (mtHandler instanceof MembershipTypeEventListenerHandler)
      {
         List<MembershipTypeEventListener> list =
            ((MembershipTypeEventListenerHandler)mtHandler).getMembershipTypeListeners();

         currentSize = list.size();
      }
      mtHandler.addMembershipTypeEventListener(testListener);

      if (mtHandler instanceof MembershipTypeEventListenerHandler)
      {
         List<MembershipTypeEventListener> list =
            ((MembershipTypeEventListenerHandler)mtHandler).getMembershipTypeListeners();

         assertEquals(currentSize + 1, list.size());
      }

      // Create new membership type. In preSave event there is not recored in db.
      createMembershipType(membershipType, "desc");

      assertEquals(2, testListener.mtInEvent.size());
      assertEquals(2, testListener.mtInStorage.size());

      // preSave Event
      assertEquals(membershipType, testListener.mtInEvent.get(0).getName());
      assertNull(testListener.mtInStorage.get(0));

      // postSave Event
      assertEquals(membershipType, testListener.mtInEvent.get(1).getName());
      assertNotNull(testListener.mtInStorage.get(1));
      assertEquals(membershipType, testListener.mtInStorage.get(1).getName());

      testListener.mtInEvent.clear();
      testListener.mtInStorage.clear();

      // Modify membership type. In preSave event there is old record in storage.
      MembershipType mt = mtHandler.findMembershipType(membershipType);
      mt.setDescription("newDesc");

      mtHandler.saveMembershipType(mt, true);

      // preSave Event
      assertEquals(2, testListener.mtInEvent.size());
      assertEquals(2, testListener.mtInStorage.size());

      assertEquals("newDesc", testListener.mtInEvent.get(0).getDescription());
      assertEquals("desc", testListener.mtInStorage.get(0).getDescription());

      // postSave Event
      assertEquals("newDesc", testListener.mtInEvent.get(1).getDescription());
      assertEquals("newDesc", testListener.mtInStorage.get(1).getDescription());

      testListener.mtInEvent.clear();
      testListener.mtInStorage.clear();

      // Remove membership type. In preDelete Event there is still record in storage
      mtHandler.removeMembershipType(membershipType, true);

      assertEquals(2, testListener.mtInEvent.size());
      assertEquals(2, testListener.mtInStorage.size());

      // preDelete Event
      assertEquals(membershipType, testListener.mtInEvent.get(0).getName());
      assertNotNull(testListener.mtInStorage.get(0));

      // postDelete Event
      assertEquals(membershipType, testListener.mtInEvent.get(1).getName());
      assertNull(testListener.mtInStorage.get(1));

      testListener.mtInEvent.clear();
      testListener.mtInStorage.clear();

   }

   private class TesterMembershipTypeEventListener extends MembershipTypeEventListener
   {
      List<MembershipType> mtInEvent = new ArrayList<MembershipType>();

      List<MembershipType> mtInStorage = new ArrayList<MembershipType>();

      public void preSave(MembershipType type, boolean isNew) throws Exception
      {
         mtInEvent.add(type);
         mtInStorage.add(mtHandler.findMembershipType(type.getName()));
      }

      public void postSave(MembershipType type, boolean isNew) throws Exception
      {
         mtInEvent.add(type);
         mtInStorage.add(mtHandler.findMembershipType(type.getName()));
      }

      public void preDelete(MembershipType type) throws Exception
      {
         mtInEvent.add(type);
         mtInStorage.add(mtHandler.findMembershipType(type.getName()));
      }

      public void postDelete(MembershipType type) throws Exception
      {
         mtInEvent.add(type);
         mtInStorage.add(mtHandler.findMembershipType(type.getName()));
      }
   }
}
