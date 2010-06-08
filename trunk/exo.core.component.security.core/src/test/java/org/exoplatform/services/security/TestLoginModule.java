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
package org.exoplatform.services.security;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.security.jaas.BasicCallbackHandler;

import java.net.URL;

import javax.security.auth.login.LoginContext;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 28 avr. 2004
 */
public class TestLoginModule extends TestCase
{

   protected ConversationRegistry conversationRegistry;

   protected IdentityRegistry identityRegistry;

   protected Authenticator authenticator;

   public TestLoginModule(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {

      if (conversationRegistry == null)
      {
         URL containerConfURL = TestLoginModule.class.getResource("/conf/standalone/test-configuration.xml");
         assertNotNull(containerConfURL);
         String containerConf = containerConfURL.toString();
         URL loginConfURL = TestLoginModule.class.getResource("/login.conf");
         assertNotNull(loginConfURL);
         String loginConf = loginConfURL.toString();
         StandaloneContainer.addConfigurationURL(containerConf);
         if (System.getProperty("java.security.auth.login.config") == null)
            System.setProperty("java.security.auth.login.config", loginConf);

         StandaloneContainer manager = StandaloneContainer.getInstance();

         authenticator = (DummyAuthenticatorImpl)manager.getComponentInstanceOfType(DummyAuthenticatorImpl.class);
         assertNotNull(authenticator);
         conversationRegistry = (ConversationRegistry)manager.getComponentInstanceOfType(ConversationRegistry.class);
         assertNotNull(conversationRegistry);
         identityRegistry = (IdentityRegistry)manager.getComponentInstanceOfType(IdentityRegistry.class);
         assertNotNull(identityRegistry);

      }
      identityRegistry.clear();
      conversationRegistry.clear();
   }

   public void testLogin() throws Exception
   {
      BasicCallbackHandler handler = new BasicCallbackHandler("exo", "exo".toCharArray());
      LoginContext loginContext = new LoginContext("exo", handler);
      loginContext.login();

      assertNotNull(identityRegistry.getIdentity("exo"));
      assertEquals("exo", identityRegistry.getIdentity("exo").getUserId());

      assertEquals(1, identityRegistry.getIdentity("exo").getGroups().size());

      StateKey key = new SimpleStateKey("exo");
      conversationRegistry.register(key, new ConversationState(identityRegistry.getIdentity("exo")));
      assertNotNull(conversationRegistry.getState(key));

   }

}
