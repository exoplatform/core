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
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserEventListenerHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.NewUserConfig;
import org.exoplatform.services.organization.impl.NewUserEventListener;

import java.util.Calendar;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestOrganizationService.java 111 2008-11-11 11:11:11Z $
 */
public class TestUserHandler extends AbstractOrganizationServiceTest
{
   private MyUserEventListener listener;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      listener = new MyUserEventListener();
      uHandler.addUserEventListener(listener);
   }

   @Override
   public void tearDown() throws Exception
   {
      uHandler.removeUserEventListener(listener);
      super.tearDown();
   }

   /**
    * User authentication.
    */
   public void testAuthenticate() throws Exception
   {
      createUser("testAuthenticate");

      // authentication with existing user and correct password
      assertTrue(uHandler.authenticate("testAuthenticate", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));

      // unknown user authentication
      assertFalse(uHandler.authenticate("testAuthenticate_", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));

      // authentication with wrong password
      assertFalse(uHandler.authenticate("testAuthenticate", "pwdADDSomeSaltToBeCompliantWithSomeIS00_"));

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user testAuthenticate
         uHandler.setEnabled("testAuthenticate", false, true);

         try
         {
            uHandler.authenticate("testAuthenticate", "pwdADDSomeSaltToBeCompliantWithSomeIS00");
            fail("A DisabledUserException was expected");
         }
         catch (DisabledUserException e)
         {
            // expected exception
         }

         // Enable the user testAuthenticate
         uHandler.setEnabled("testAuthenticate", true, true);
         assertTrue(uHandler.authenticate("testAuthenticate", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user testAuthenticate
      uHandler.removeUser("testAuthenticate", true);

      // The user testAuthenticate doesn't exist anymore thus the authentication should fail
      assertFalse(uHandler.authenticate("testAuthenticate", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Find user by name.
    */
   public void testFindUserByName() throws Exception
   {
      createUser("testFindUserByName");

      // try to find existed user
      User u = uHandler.findUserByName("demo");

      assertNotNull(u);
      assertEquals("demo@localhost", u.getEmail());
      assertEquals("Demo", u.getFirstName());
      assertEquals("exo", u.getLastName());
      assertEquals("demo", u.getUserName());
      assertTrue(u.isEnabled());

      // try to find a non existing user. We are supposed to get "null" instead of Exception.
      try
      {
         assertNull(uHandler.findUserByName("not-existed-user"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      try
      {
         assertNull(uHandler.findUserByName("not-existed-user", UserStatus.ENABLED));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      try
      {
         assertNull(uHandler.findUserByName("not-existed-user", UserStatus.DISABLED));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      try
      {
         assertNull(uHandler.findUserByName("not-existed-user", UserStatus.BOTH));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      assertNotNull(uHandler.findUserByName("testFindUserByName"));
      assertTrue(uHandler.findUserByName("testFindUserByName").isEnabled());
      assertNotNull(uHandler.findUserByName("testFindUserByName", UserStatus.ENABLED));
      assertTrue(uHandler.findUserByName("testFindUserByName", UserStatus.ENABLED).isEnabled());
      assertNotNull(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH));
      assertTrue(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH).isEnabled());

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user testFindUserByName
         uHandler.setEnabled("testFindUserByName", false, true);

         // We should not find the user testFindUserByName anymore from the normal method
         assertNull(uHandler.findUserByName("testFindUserByName"));
         assertNull(uHandler.findUserByName("testFindUserByName", UserStatus.ENABLED));
         // We should find it using the method that includes the disabled user account
         assertNotNull(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH));
         assertFalse(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH).isEnabled());
         // We should find it using the method that queries only the disabled user account
         assertNotNull(uHandler.findUserByName("testFindUserByName", UserStatus.DISABLED));
         assertFalse(uHandler.findUserByName("testFindUserByName", UserStatus.DISABLED).isEnabled());

         // Enable the user testFindUserByName
         uHandler.setEnabled("testFindUserByName", true, true);

         // We should find it again whatever the value of the parameter status
         assertNotNull(uHandler.findUserByName("testFindUserByName"));
         assertTrue(uHandler.findUserByName("testFindUserByName").isEnabled());
         assertNotNull(uHandler.findUserByName("testFindUserByName", UserStatus.ENABLED));
         assertTrue(uHandler.findUserByName("testFindUserByName", UserStatus.ENABLED).isEnabled());
         assertNotNull(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH));
         assertTrue(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH).isEnabled());
         // We should not find the user using the method that queries only the disabled user account
         assertNull(uHandler.findUserByName("testFindUserByName", UserStatus.DISABLED));
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user testFindUserByName
      uHandler.removeUser("testFindUserByName", true);

      // try to find a user that doesn't exist anymore. We are supposed to get "null" instead of Exception.
      try
      {
         assertNull(uHandler.findUserByName("testFindUserByName"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      try
      {
         assertNull(uHandler.findUserByName("testFindUserByName", UserStatus.ENABLED));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      try
      {
         assertNull(uHandler.findUserByName("testFindUserByName", UserStatus.DISABLED));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
      try
      {
         assertNull(uHandler.findUserByName("testFindUserByName", UserStatus.BOTH));
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
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Find users by query.
    */
   public void testFindUsersByQuery() throws Exception
   {
      createUser("tolik");
      uHandler.authenticate("tolik", "pwdADDSomeSaltToBeCompliantWithSomeIS00");

      Query query = new Query();
      query.setEmail("email@test");

      // try to find user by email
      assertSizeEquals(1, uHandler.findUsersByQuery(query), UserStatus.ENABLED);
      assertSizeEquals(1, uHandler.findUsersByQuery(query, UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(1, uHandler.findUsersByQuery(query, UserStatus.BOTH), UserStatus.BOTH);

      // try to find user by name with mask
      query = new Query();
      query.setUserName("*tolik*");
      assertSizeEquals(1, uHandler.findUsersByQuery(query));

      // try to find user by name with mask
      query = new Query();
      query.setUserName("tol*");
      assertSizeEquals(1, uHandler.findUsersByQuery(query));

      // try to find user by name with mask
      query = new Query();
      query.setUserName("*lik");
      assertSizeEquals(1, uHandler.findUsersByQuery(query));

      // try to find user by name explicitly
      query = new Query();
      query.setUserName("tolik");
      assertSizeEquals(1, uHandler.findUsersByQuery(query));

      // try to find user by part of name without mask
      query = new Query();
      query.setUserName("tol");
      assertSizeEquals(1, uHandler.findUsersByQuery(query));

      // try to find user by fist and last names
      query = new Query();
      query.setFirstName("first");
      query.setLastName("last");
      assertSizeEquals(1, uHandler.findUsersByQuery(query));

      String skipCISearchTests = System.getProperty("orgservice.test.configuration.skipCISearchTests");
      if (!"true".equals(skipCISearchTests))
      {
         // try to find user by name explicitly, case insensitive search
         query = new Query();
         query.setUserName("Tolik");
         assertSizeEquals(1, uHandler.findUsersByQuery(query));

         // try to find user by fist and last names, case insensitive search
         query = new Query();
         query.setFirstName("fiRst");
         query.setLastName("lasT");
         assertSizeEquals(1, uHandler.findUsersByQuery(query));
      }

      String skipDateTests = System.getProperty("orgservice.test.configuration.skipDateTests");
      if (!"true".equals(skipDateTests))
      {
         // try to find user by login date
         Calendar calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);

         query = new Query();
         query.setFromLoginDate(calc.getTime());
         query.setUserName("tolik");
         assertSizeEquals(1, uHandler.findUsersByQuery(query));

         calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);

         query = new Query();
         query.setFromLoginDate(calc.getTime());
         assertSizeEquals(0, uHandler.findUsersByQuery(query));

         calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);

         query = new Query();
         query.setToLoginDate(calc.getTime());
         assertSizeEquals(0, uHandler.findUsersByQuery(query));

         calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);

         query = new Query();
         query.setToLoginDate(calc.getTime());
         query.setUserName("tolik");
         assertSizeEquals(1, uHandler.findUsersByQuery(query));
      }

      createUser("rolik");
      createUser("bolik");
      createUser("volik");

      query = new Query();
      query.setUserName("olik");

      ListAccess<User> users = uHandler.findUsersByQuery(query);

      assertSizeEquals(4, users, UserStatus.ENABLED);
      assertSizeEquals(4, uHandler.findUsersByQuery(query, UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(4, uHandler.findUsersByQuery(query, UserStatus.BOTH), UserStatus.BOTH);

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

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user tolik
         uHandler.setEnabled("tolik", false, true);

         assertSizeEquals(3, uHandler.findUsersByQuery(query), UserStatus.ENABLED);
         assertSizeEquals(3, uHandler.findUsersByQuery(query, UserStatus.ENABLED), UserStatus.ENABLED);
         assertSizeEquals(1, uHandler.findUsersByQuery(query, UserStatus.DISABLED), UserStatus.DISABLED);
         assertSizeEquals(4, uHandler.findUsersByQuery(query, UserStatus.BOTH), UserStatus.BOTH);

         // Enable the user tolik
         uHandler.setEnabled("tolik", true, true);

         assertSizeEquals(4, uHandler.findUsersByQuery(query), UserStatus.ENABLED);
         assertSizeEquals(4, uHandler.findUsersByQuery(query, UserStatus.ENABLED), UserStatus.ENABLED);
         assertSizeEquals(0, uHandler.findUsersByQuery(query, UserStatus.DISABLED), UserStatus.DISABLED);
         assertSizeEquals(4, uHandler.findUsersByQuery(query, UserStatus.BOTH), UserStatus.BOTH);
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user tolik
      uHandler.removeUser("tolik", true);

      assertSizeEquals(3, uHandler.findUsersByQuery(query), UserStatus.ENABLED);
      assertSizeEquals(3, uHandler.findUsersByQuery(query, UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(3, uHandler.findUsersByQuery(query, UserStatus.BOTH), UserStatus.BOTH);


      // Check the listener's counters
      assertEquals(4, listener.preSaveNew);
      assertEquals(4, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Find users.
    */
   public void testFindUsers() throws Exception
   {
      createUser("tolik");
      uHandler.authenticate("tolik", "pwdADDSomeSaltToBeCompliantWithSomeIS00");

      Query query = new Query();
      query.setEmail("email@test");

      // try to find user by email
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      // try to find user by name with mask
      query = new Query();
      query.setUserName("*tolik*");
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      // try to find user by name with mask
      query = new Query();
      query.setUserName("tol*");
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      // try to find user by name with mask
      query = new Query();
      query.setUserName("*lik");
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      // try to find user by name explicitly
      query = new Query();
      query.setUserName("tolik");
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      // try to find user by part of name without mask
      query = new Query();
      query.setUserName("tol");
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      // try to find user by fist and last names
      query = new Query();
      query.setFirstName("first");
      query.setLastName("last");
      assertSizeEquals(1, uHandler.findUsers(query).getAll());

      String skipCISearchTests = System.getProperty("orgservice.test.configuration.skipCISearchTests");
      if (!"true".equals(skipCISearchTests))
      {
         // try to find user by name explicitly, case insensitive search
         query = new Query();
         query.setUserName("Tolik");
         assertSizeEquals(1, uHandler.findUsers(query).getAll());

         // try to find user by fist and last names, case insensitive search
         query = new Query();
         query.setFirstName("fiRst");
         query.setLastName("lasT");
         assertSizeEquals(1, uHandler.findUsers(query).getAll());
      }

      String skipDateTests = System.getProperty("orgservice.test.configuration.skipDateTests");
      if (!"true".equals(skipDateTests))
      {
         // try to find user by login date
         Calendar calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);

         query = new Query();
         query.setFromLoginDate(calc.getTime());
         query.setUserName("tolik");
         assertSizeEquals(1, uHandler.findUsers(query).getAll());

         calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);

         query = new Query();
         query.setFromLoginDate(calc.getTime());
         assertSizeEquals(0, uHandler.findUsers(query).getAll());

         calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);

         query = new Query();
         query.setToLoginDate(calc.getTime());
         assertSizeEquals(0, uHandler.findUsers(query).getAll());

         calc = Calendar.getInstance();
         calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);

         query = new Query();
         query.setToLoginDate(calc.getTime());
         query.setUserName("tolik");
         assertSizeEquals(1, uHandler.findUsers(query).getAll());
      }
   }

   /**
    * Get users page list.
    */
   public void testGetUserPageList() throws Exception
   {
      assertSizeEquals(4, uHandler.getUserPageList(10).getAll());
   }

   /**
    * Find all users.
    */
   public void testFindAllUsers() throws Exception
   {
      createUser("testFindAllUsers");

      assertSizeEquals(5, uHandler.findAllUsers(), UserStatus.ENABLED);
      assertSizeEquals(5, uHandler.findAllUsers(UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(5, uHandler.findAllUsers(UserStatus.BOTH), UserStatus.BOTH);

      ListAccess<User> users = uHandler.findAllUsers();
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
         users.load(0, 6);
         fail("Exception is expected");
      }
      catch (Exception e)
      {
      }

      // try to load more than exist
      try
      {
         users.load(1, 5);
         fail("Exception is expected");
      }
      catch (Exception e)
      {
      }

      String userName = "testFindAllUsers";

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user
         uHandler.setEnabled(userName, false, true);

         assertSizeEquals(4, uHandler.findAllUsers(), UserStatus.ENABLED);
         assertSizeEquals(4, uHandler.findAllUsers(UserStatus.ENABLED), UserStatus.ENABLED);
         assertSizeEquals(1, uHandler.findAllUsers(UserStatus.DISABLED), UserStatus.DISABLED);
         assertSizeEquals(5, uHandler.findAllUsers(UserStatus.BOTH), UserStatus.BOTH);

         // Enable the user
         uHandler.setEnabled(userName, true, true);

         assertSizeEquals(5, uHandler.findAllUsers(), UserStatus.ENABLED);
         assertSizeEquals(5, uHandler.findAllUsers(UserStatus.ENABLED), UserStatus.ENABLED);
         assertSizeEquals(0, uHandler.findAllUsers(UserStatus.DISABLED), UserStatus.DISABLED);
         assertSizeEquals(5, uHandler.findAllUsers(UserStatus.BOTH), UserStatus.BOTH);
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user
      uHandler.removeUser(userName, true);

      assertSizeEquals(4, uHandler.findAllUsers(), UserStatus.ENABLED);
      assertSizeEquals(4, uHandler.findAllUsers(UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(4, uHandler.findAllUsers(UserStatus.BOTH), UserStatus.BOTH);


      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Remove user.
    */
   public void testRemoveUser() throws Exception
   {
      createMembership(userName, groupName2, membershipType);

      assertEquals("We expect to find single membership for user " + userName, 1,
         mHandler.findMembershipsByUser(userName).size());

      assertNotNull(uHandler.removeUser(userName, true));

      assertNull(upHandler.findUserProfileByName(userName));
      assertEquals("We expect to find no membership for user " + userName, 0, mHandler.findMembershipsByUser(userName)
         .size());

      // try to find user after remove. We are supposed to get "null" instead of exception
      try
      {
         assertNull(uHandler.findUserByName(userName + "_"));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
   }

   /**
    * Save user.
    */
   public void testSaveUser() throws Exception
   {
      String userName = "testSaveUser";
      createUser(userName);

      String newEmail = "new@Email";
      String displayName = "name";

      // change email and check
      User u = uHandler.findUserByName(userName);
      u.setEmail(newEmail);

      uHandler.saveUser(u, true);

      u = uHandler.findUserByName(userName);
      assertEquals(newEmail, u.getEmail());
      assertEquals(u.getDisplayName(), u.getFirstName() + " " + u.getLastName());

      u.setDisplayName(displayName);
      uHandler.saveUser(u, true);

      u = uHandler.findUserByName(userName);
      assertEquals(displayName, u.getDisplayName());

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user
         u = uHandler.setEnabled(userName, false, true);
         u.setDisplayName(displayName + "new-value");
         try
         {
            uHandler.saveUser(u, true);
            fail("A DisabledUserException was expected");
         }
         catch (DisabledUserException e)
         {
            // expected issue
         }

         // Enable the user
         u = uHandler.setEnabled(userName, true, true);
         u.setDisplayName(displayName + "new-value");
         uHandler.saveUser(u, true);

         u = uHandler.findUserByName(userName);
         assertEquals(displayName + "new-value", u.getDisplayName());
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user
      uHandler.removeUser(userName, true);

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(unsupportedOperation ? 2 : 3, listener.preSave);
      assertEquals(unsupportedOperation ? 2 : 3, listener.postSave);
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Change password.
    */
   public void testChangePassword() throws Exception
   {
      createUser("testChangePassword");

      // authentication with existing user and correct password
      assertTrue(uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));

      // authentication with wrong password
      assertFalse(uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00_"));

      User u = uHandler.findUserByName("testChangePassword");
      u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00_");
      uHandler.saveUser(u, true);

      // authentication with existing user and correct password
      assertTrue(uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00_"));

      // authentication with wrong password
      assertFalse(uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user
         u = uHandler.setEnabled("testChangePassword", false, true);
         u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00");

         try
         {
            uHandler.saveUser(u, true);
            fail("A DisabledUserException was expected");
         }
         catch (DisabledUserException e)
         {
            // expected issue
         }

         try
         {
            // authentication with existing user and correct password
            uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00_");
            fail("A DisabledUserException was expected");
         }
         catch (DisabledUserException e)
         {
            // expected issue
         }

         try
         {
            // authentication with wrong password
            uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00");
            fail("A DisabledUserException was expected");
         }
         catch (DisabledUserException e)
         {
            // expected issue
         }

         // Disable the user
         u = uHandler.setEnabled("testChangePassword", true, true);
         u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00");
         uHandler.saveUser(u, true);

         // authentication with existing user and correct password
         assertTrue(uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00"));

         // authentication with wrong password
         assertFalse(uHandler.authenticate("testChangePassword", "pwdADDSomeSaltToBeCompliantWithSomeIS00_"));
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user
      uHandler.removeUser("testChangePassword", true);

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(unsupportedOperation ? 1 : 2, listener.preSave);
      assertEquals(unsupportedOperation ? 1 : 2, listener.postSave);
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Create user.
    */
   public void testCreateUser() throws Exception
   {
      User u = uHandler.createUserInstance(userName);
      u.setEmail("email@test");
      u.setFirstName("first");
      u.setLastName("last");
      u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00");
      uHandler.createUser(u, true);

      // check if user exists
      assertNotNull(uHandler.findUserByName(userName));

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(0, listener.preSetEnabled);
      assertEquals(0, listener.postSetEnabled);
      assertEquals(0, listener.preDelete);
      assertEquals(0, listener.postDelete);
   }

   public void testFindUsersByGroupId() throws Exception
   {
      createMembership(userName, groupName2, membershipType);

      String groupId = "/" + groupName2;
      assertSizeEquals(1, uHandler.findUsersByGroupId(groupId), UserStatus.ENABLED);
      assertSizeEquals(1, uHandler.findUsersByGroupId(groupId, UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(1, uHandler.findUsersByGroupId(groupId, UserStatus.BOTH), UserStatus.BOTH);

      boolean unsupportedOperation = false;
      try
      {
         // Disable the user
         uHandler.setEnabled(userName, false, true);

         assertSizeEquals(0, uHandler.findUsersByGroupId(groupId), UserStatus.ENABLED);
         assertSizeEquals(0, uHandler.findUsersByGroupId(groupId, UserStatus.ENABLED), UserStatus.ENABLED);
         assertSizeEquals(1, uHandler.findUsersByGroupId(groupId, UserStatus.DISABLED), UserStatus.DISABLED);
         assertSizeEquals(1, uHandler.findUsersByGroupId(groupId, UserStatus.BOTH), UserStatus.BOTH);

         // Enable the user
         uHandler.setEnabled(userName, true, true);

         assertSizeEquals(1, uHandler.findUsersByGroupId(groupId), UserStatus.ENABLED);
         assertSizeEquals(1, uHandler.findUsersByGroupId(groupId, UserStatus.ENABLED), UserStatus.ENABLED);
         assertSizeEquals(0, uHandler.findUsersByGroupId(groupId, UserStatus.DISABLED), UserStatus.DISABLED);
         assertSizeEquals(1, uHandler.findUsersByGroupId(groupId, UserStatus.BOTH), UserStatus.BOTH);
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported
         unsupportedOperation = true;
      }

      // Remove the user
      uHandler.removeUser(userName, true);

      assertSizeEquals(0, uHandler.findUsersByGroupId(groupId), UserStatus.ENABLED);
      assertSizeEquals(0, uHandler.findUsersByGroupId(groupId, UserStatus.ENABLED), UserStatus.ENABLED);
      assertSizeEquals(0, uHandler.findUsersByGroupId(groupId, UserStatus.BOTH), UserStatus.BOTH);

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(unsupportedOperation ? 0 : 2, listener.preSetEnabled);
      assertEquals(unsupportedOperation ? 0 : 2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   /**
    * Test get listeners.
    */
   public void testGetListeners() throws Exception
   {
      if (uHandler instanceof UserEventListenerHandler)
      {
         List<UserEventListener> list = ((UserEventListenerHandler) uHandler).getUserListeners();
         try
         {
            // check if we able to modify the list of listeners
            list.clear();
            fail("Exception should not be thrown");
         }
         catch (Exception e)
         {
         }
      }
   }

   public void testSetEnabled() throws Exception
   {
      try
      {
         // Trying to disable a non existing user should not throw any exception
         assertNull(uHandler.setEnabled("foo", false, true));
      }
      catch (UnsupportedOperationException e)
      {
         // This operation can be unsupported, the unit test will be ignored
         return;
      }
      createUser("testSetEnabled");

      // Trying to disable an existing user should return the corresponding user
      User user = uHandler.setEnabled("testSetEnabled", false, true);

      assertNotNull(user);
      assertEquals("testSetEnabled", user.getUserName());
      assertFalse(user.isEnabled());

      // Trying to disable an user already disabled
      user = uHandler.setEnabled("testSetEnabled", false, true);

      assertNotNull(user);
      assertEquals("testSetEnabled", user.getUserName());
      assertFalse(user.isEnabled());

      // Trying to enable the user
      user = uHandler.setEnabled("testSetEnabled", true, true);

      assertNotNull(user);
      assertEquals("testSetEnabled", user.getUserName());
      assertTrue(user.isEnabled());

      // Trying to enable an user already enabled
      user = uHandler.setEnabled("testSetEnabled", true, true);

      assertNotNull(user);
      assertEquals("testSetEnabled", user.getUserName());
      assertTrue(user.isEnabled());

      // Remove the user testSetEnabled
      uHandler.removeUser("testSetEnabled", true);
      assertNull(uHandler.setEnabled("testSetEnabled", false, true));

      // Check the listener's counters
      assertEquals(1, listener.preSaveNew);
      assertEquals(1, listener.postSaveNew);
      assertEquals(0, listener.preSave);
      assertEquals(0, listener.postSave);
      assertEquals(2, listener.preSetEnabled);
      assertEquals(2, listener.postSetEnabled);
      assertEquals(1, listener.preDelete);
      assertEquals(1, listener.postDelete);
   }

   public void testPreDeleteUserEventListenerPreventRemoveUser() throws Exception
   {
      createMembership(userName, groupName2, membershipType);
      createUserProfile(userName);

      assertEquals("Only one membership is expected for the user " + userName, 1,
         mHandler.findMembershipsByUser(userName).size());

      // We ensure that the UserProfile has been created properly
      UserProfile up = upHandler.findUserProfileByName(userName);
      assertNotNull(up);
      assertEquals(userName, up.getUserName());
      assertEquals("value1", up.getAttribute("key1"));
      assertEquals("value2", up.getAttribute("key2"));

      //Try to remove user
      PreventDeleteUserListener preventDeleteUserListener = new PreventDeleteUserListener();
      uHandler.addUserEventListener(preventDeleteUserListener);
      try
      {
         uHandler.removeUser(userName, true);
         fail("Exception should be thrown");
      }
      catch (Exception ex)
      {
         //Expect exception will be thrown
      }
      finally
      {
         uHandler.removeUserEventListener(preventDeleteUserListener);
      }

      // Make sure that the user has not been removed
      assertNotNull(uHandler.findUserByName(userName));

      // Make sure that the membership has not been removed
      assertEquals("Only one membership is expected for the user " + userName, 1,
         mHandler.findMembershipsByUser(userName).size());

      // Make sure that the UserProfile has not been removed
      up = upHandler.findUserProfileByName(userName);
      assertNotNull(up);
      assertEquals(userName, up.getUserName());
      assertEquals("value1", up.getAttribute("key1"));
      assertEquals("value2", up.getAttribute("key2"));
   }

    public void testPreDeleteNewUserEventListener() throws Exception
   {
        createMembership(userName, groupName2, membershipType);
        createUserProfile(userName);

        assertEquals("Only one membership is expected for the user " + userName, 1,
                mHandler.findMembershipsByUser(userName).size());

        // We ensure that the UserProfile has been created properly
        UserProfile up = upHandler.findUserProfileByName(userName);
        assertNotNull(up);
        assertEquals(userName, up.getUserName());
        assertEquals("value1", up.getAttribute("key1"));
        assertEquals("value2", up.getAttribute("key2"));

        //Try to remove user
        ObjectParameter param = new ObjectParameter();
        param.setObject(new NewUserConfig());
        InitParams params = new InitParams();
        params.addParam(param);
        NewUserEventListener newUserEventListener = new NewUserEventListener(params);
        uHandler.addUserEventListener(newUserEventListener);
        try
        {
            uHandler.removeUser(userName, true);
        }
        catch (Exception ex)
        {
            fail("Exception should not be thrown");
        }
        finally
        {
            uHandler.removeUserEventListener(newUserEventListener);
        }

        // Make sure that the user has been removed
        assertNull(uHandler.findUserByName(userName));

        // Make sure that the membership has been removed
        assertEquals("the membership should be removed for the user " + userName, 0,
                mHandler.findMembershipsByUser(userName).size());

        // Make sure that the UserProfile has been removed
        up = upHandler.findUserProfileByName(userName);
        assertNull(up);
   }

    public void testPreventRemoveUser() throws Exception
   {
        createMembership(userName, groupName2, membershipType);
        createUserProfile(userName);

        assertEquals("Only one membership is expected for the user " + userName, 1,
                mHandler.findMembershipsByUser(userName).size());

        // We ensure that the UserProfile has been created properly
        UserProfile up = upHandler.findUserProfileByName(userName);
        assertNotNull(up);
        assertEquals(userName, up.getUserName());
        assertEquals("value1", up.getAttribute("key1"));
        assertEquals("value2", up.getAttribute("key2"));

        //Try to remove user
        ObjectParameter param = new ObjectParameter();
        param.setObject(new NewUserConfig());
        InitParams params = new InitParams();
        params.addParam(param);
        NewUserEventListener newUserEventListener = new NewUserEventListener(params);
        PreventDeleteUserListener preventDeleteUserListener = new PreventDeleteUserListener();
        uHandler.addUserEventListener(newUserEventListener);
        uHandler.addUserEventListener(preventDeleteUserListener);
        try
        {
            uHandler.removeUser(userName, true);
            fail("Exception should be thrown");
        }
        catch (Exception ex)
        {
            //Expect exception will be thrown
        }
        finally
        {
            uHandler.removeUserEventListener(preventDeleteUserListener);
            uHandler.removeUserEventListener(newUserEventListener);
        }

        // Make sure that the user has not been removed
        assertNotNull(uHandler.findUserByName(userName));

        // Make sure that the membership has not been removed
        assertEquals("Only one membership is expected for the user " + userName, 1,
                mHandler.findMembershipsByUser(userName).size());

        // Make sure that the UserProfile has not been removed
        up = upHandler.findUserProfileByName(userName);
        assertNotNull(up);
        assertEquals(userName, up.getUserName());
        assertEquals("value1", up.getAttribute("key1"));
        assertEquals("value2", up.getAttribute("key2"));
   }

    private static class PreventDeleteUserListener extends UserEventListener
   {
      @Override
      public void preDelete(User user) throws Exception
      {
         throw new Exception("You cannot to delete user");
      }

      @Override
      public void postDelete(User user) throws Exception
      {
         fail("This method should not be execute because preDelete Event prevent remove user");
      }
   }

   private static class MyUserEventListener extends UserEventListener
   {
      public int preSaveNew, postSaveNew;
      public int preSave, postSave;
      public int preDelete, postDelete;
      public int preSetEnabled, postSetEnabled;

      @Override
      public void preSave(User user, boolean isNew) throws Exception
      {
         if (user == null)
            return;
         if (isNew)
            preSaveNew++;
         else
            preSave++;
      }

      @Override
      public void postSave(User user, boolean isNew) throws Exception
      {
         if (user == null)
            return;
         if (isNew)
            postSaveNew++;
         else
            postSave++;
      }

      @Override
      public void preDelete(User user) throws Exception
      {
         if (user == null)
            return;
         preDelete++;
      }

      @Override
      public void postDelete(User user) throws Exception
      {
         if (user == null)
            return;
         postDelete++;
      }

      @Override
      public void preSetEnabled(User user) throws Exception
      {
         if (user == null)
            return;
         preSetEnabled++;
      }

      @Override
      public void postSetEnabled(User user) throws Exception
      {
         if (user == null)
            return;
         postSetEnabled++;
      }
   }
}
