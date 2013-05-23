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
package org.exoplatform.services.organization.auth;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

import java.net.URL;

import javax.security.auth.login.LoginException;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 28 avr. 2004
 */
public class TestOrganizationAuthenticator extends TestCase
{

   protected ConversationRegistry registry;

   protected Authenticator authenticator;

   protected OrganizationService orgService;

   public TestOrganizationAuthenticator(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {

      if (registry == null)
      {
         URL containerConfURL =
            TestOrganizationAuthenticator.class.getResource("/conf/standalone/test-configuration.xml");
         assertNotNull(containerConfURL);
         String containerConf = containerConfURL.toString();
         URL loginConfURL = TestOrganizationAuthenticator.class.getResource("/login.conf");
         assertNotNull(loginConfURL);
         String loginConf = loginConfURL.toString();
         StandaloneContainer.addConfigurationURL(containerConf);
         if (System.getProperty("java.security.auth.login.config") == null)
            System.setProperty("java.security.auth.login.config", loginConf);

         StandaloneContainer container = StandaloneContainer.getInstance();

         authenticator = (Authenticator)container.getComponentInstanceOfType(OrganizationAuthenticatorImpl.class);
         assertNotNull(authenticator);

         registry = (ConversationRegistry)container.getComponentInstanceOfType(ConversationRegistry.class);
         assertNotNull(registry);

         orgService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
         assertNotNull(orgService);

      }

   }

   public void testAuthenticator() throws Exception
   {
      assertNotNull(authenticator);
      assertTrue(authenticator instanceof OrganizationAuthenticatorImpl);
      Credential[] cred = new Credential[]{new UsernameCredential("admin"), new PasswordCredential("admin")};
      String userId = authenticator.validateUser(cred);
      assertEquals("admin", userId);
      Identity identity = authenticator.createIdentity(userId);
      assertTrue(identity.isMemberOf("/platform/administrators", "manager"));
      assertTrue(identity.getGroups().size() > 0);
   }
   

   public void testGetLastExceptionOnValidateUser() throws Exception
   {
      assertNotNull(orgService);
      UserHandler uh = orgService.getUserHandler();
      User user = uh.createUserInstance("testGetLastExceptionOnValidateUser");
      user.setPassword("foo");
      assertNotNull(authenticator);
      assertTrue(authenticator instanceof OrganizationAuthenticatorImpl);
      Credential[] cred = new Credential[]{new UsernameCredential("testGetLastExceptionOnValidateUser"), new PasswordCredential("foo")};
      String userId = authenticator.validateUser(cred);
      assertEquals("testGetLastExceptionOnValidateUser", userId);
      assertNull(authenticator.getLastExceptionOnValidateUser());
      uh.setEnabled("testGetLastExceptionOnValidateUser", false, false);
      try
      {
         authenticator.validateUser(cred);
         fail("a LoginException was expected");
      }
      catch (LoginException e)
      {
         // expected
      }
      assertNotNull(authenticator.getLastExceptionOnValidateUser());
      assertTrue(authenticator.getLastExceptionOnValidateUser() instanceof DisabledUserException);
      uh.setEnabled("testGetLastExceptionOnValidateUser", true, false);
      userId = authenticator.validateUser(cred);
      assertEquals("testGetLastExceptionOnValidateUser", userId);
      assertNull(authenticator.getLastExceptionOnValidateUser());
   }
}
