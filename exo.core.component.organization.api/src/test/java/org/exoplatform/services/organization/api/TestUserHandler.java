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
package org.exoplatform.services.organization.api;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.auth.TestOrganizationAuthenticator;
import org.exoplatform.services.security.ConversationRegistry;

import java.net.URL;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestUserHandler.java 111 2008-11-11 11:11:11Z $
 */
public class TestUserHandler extends TestCase
{

   protected ConversationRegistry registry;

   private BaseOrganizationService organizationService;

   private UserHandler uHandler;

   protected void setUp() throws Exception
   {
      super.setUp();

      if (registry == null)
      {
         URL containerConfURL =
            TestOrganizationAuthenticator.class.getResource("/conf/standalone/test-configuration.xml");
         assertNotNull(containerConfURL);

         String containerConf = containerConfURL.toString();
         StandaloneContainer.addConfigurationURL(containerConf);
         StandaloneContainer container = StandaloneContainer.getInstance();

         organizationService =
            (BaseOrganizationService)container
               .getComponentInstance(org.exoplatform.services.organization.OrganizationService.class);
         assertNotNull(organizationService);

         uHandler = organizationService.getUserHandler();

         registry = (ConversationRegistry)container.getComponentInstanceOfType(ConversationRegistry.class);
         assertNotNull(registry);
      }
   }

   /**
    * Authenticate users.
    */
   public void testAuthenticate()
   {
      try
      {
         assertTrue(uHandler.authenticate("demo", "exo"));
         assertFalse(uHandler.authenticate("demo", "exo_"));
         assertFalse(uHandler.authenticate("_demo_", "exo"));

      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Exception should not be thrown.");
      }
   }


   public void testUserEnabling() throws Exception
   {
      ListAccess<User> users = uHandler.findAllUsers();
      assertTrue(contains(users, "demo"));
      users = uHandler.findAllUsers(false);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByGroupId("/platform/users");
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByGroupId("/platform/users", false);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByQuery(null);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByQuery(null, false);
      assertTrue(contains(users, "demo"));
      assertNotNull(uHandler.findUserByName("demo"));
      assertNotNull(uHandler.findUserByName("demo", false));
      assertTrue(uHandler.authenticate("demo", "exo"));

      uHandler.setEnabled("demo", false, true);
      users = uHandler.findAllUsers();
      assertFalse(contains(users, "demo"));
      users = uHandler.findAllUsers(false);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByGroupId("/platform/users");
      assertFalse(contains(users, "demo"));
      users = uHandler.findUsersByGroupId("/platform/users", false);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByQuery(null);
      assertFalse(contains(users, "demo"));
      users = uHandler.findUsersByQuery(null, false);
      assertTrue(contains(users, "demo"));
      assertNull(uHandler.findUserByName("demo"));
      assertNotNull(uHandler.findUserByName("demo", false));
      try
      {
         uHandler.authenticate("demo", "exo");
         fail("A DisabledUserException was epected");
      }
      catch (DisabledUserException e)
      {
         // expected
      }

      uHandler.setEnabled("demo", true, true);
      users = uHandler.findAllUsers();
      assertTrue(contains(users, "demo"));
      users = uHandler.findAllUsers(false);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByGroupId("/platform/users");
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByGroupId("/platform/users", false);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByQuery(null);
      assertTrue(contains(users, "demo"));
      users = uHandler.findUsersByQuery(null, false);
      assertTrue(contains(users, "demo"));
      assertNotNull(uHandler.findUserByName("demo"));
      assertNotNull(uHandler.findUserByName("demo", false));
      assertTrue(uHandler.authenticate("demo", "exo"));
   }

   private static boolean contains(ListAccess<User> users, String username) throws IllegalArgumentException, Exception
   {
      User[] aUsers = users.load(0, users.getSize());
      for (User user : aUsers)
      {
         if (user.getUserName().equals(username))
            return true;
      }
      return false;
   }
}
