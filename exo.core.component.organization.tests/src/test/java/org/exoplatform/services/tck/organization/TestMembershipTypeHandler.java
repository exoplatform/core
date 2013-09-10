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
import org.exoplatform.services.organization.MembershipTypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMembershipTypeHandlerImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestMembershipTypeHandler extends AbstractOrganizationServiceTest
{
   private MyMembershipTypeEventListener listener;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      listener = new MyMembershipTypeEventListener();
      mtHandler.addMembershipTypeEventListener(listener);
   }

   @Override
   public void tearDown() throws Exception
   {
      mtHandler.removeMembershipTypeEventListener(listener);
      super.tearDown();
   }

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

      // Check the listener's counters
      assertEquals(0, listener.preSaveNew);
      assertEquals(0, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   /**
    * Find membership types.
    */
   public void testFindMembershipTypes() throws Exception
   {
      int initSize = 4;
      assertEquals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, mtHandler.findMembershipTypes().iterator()
         .next().getName());

      assertSizeEquals(initSize, mtHandler.findMembershipTypes());
      
      createMembershipType("type2", "membership type2");
      assertSizeEquals(initSize + 1, mtHandler.findMembershipTypes());

      mtHandler.removeMembershipType("type2", true);
      assertSizeEquals(initSize, mtHandler.findMembershipTypes());

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
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

      assertNotNull(mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));

      try
      {
         mtHandler.removeMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      assertNull(mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));
      try
      {
         assertNull(mtHandler.removeMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true));
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }
      assertNull(mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));

      MembershipType anyMT = mtHandler.createMembershipTypeInstance();
      anyMT.setName(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
      anyMT.setDescription("desc");
      mtHandler.createMembershipType(anyMT, true);

      assertNotNull(mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));

      // Check the listener's counters
      assertEquals(2, listener.preSaveNew);
      assertEquals(2, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(2, listener.preDelete);
      assertEquals(2, listener.postDelete);
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

      MembershipType anyMT = mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
      assertNotNull(anyMT);

      try
      {
         mtHandler.saveMembershipType(anyMT, true);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(2, listener.preSave);
      assertEquals(2, listener.postSave);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
  }

   public void testCreateMembershipType() throws Exception
   {
      createMembershipType(membershipType, "desc");
      MembershipType mt = mtHandler.findMembershipType(membershipType);
      assertNotNull(mt);

      try
      {
         mtHandler.removeMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, true);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      assertNull(mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));

      MembershipType anyMT = mtHandler.createMembershipTypeInstance();
      anyMT.setName(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
      anyMT.setDescription("desc");
      try
      {
         mtHandler.createMembershipType(anyMT, true);
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      assertNotNull(mtHandler.findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));

      // Check the listener's counters
      assertEquals(2, listener.preSaveNew);
      assertEquals(2, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

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

   private static class MyMembershipTypeEventListener extends MembershipTypeEventListener
   {
      public int preSaveNew, postSaveNew;
      public int preSave, postSave;
      public int preDelete, postDelete;

      @Override
      public void preSave(MembershipType type, boolean isNew) throws Exception
      {
         if (type == null)
            return;
         if (isNew)
            preSaveNew++;
         else
            preSave++;
      }

      @Override
      public void postSave(MembershipType type, boolean isNew) throws Exception
      {
         if (type == null)
            return;
         if (isNew)
            postSaveNew++;
         else
            postSave++;
      }

      @Override
      public void preDelete(MembershipType type) throws Exception
      {
         if (type == null)
            return;
         preDelete++;
      }

      @Override
      public void postDelete(MembershipType type) throws Exception
      {
         if (type == null)
            return;
         postDelete++;
      }
   }
}
