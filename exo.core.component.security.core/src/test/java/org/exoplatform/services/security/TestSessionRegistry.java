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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.impl.DefaultRolesExtractorImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 28 avr. 2004
 */
public class TestSessionRegistry extends TestCase
{

   protected ConversationRegistry registry;

   protected Authenticator authenticator;

   protected ListenerService listenerService;

   public TestSessionRegistry(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {

      if (registry == null)
      {
         String containerConf = TestLoginModule.class.getResource("/conf/standalone/test-configuration.xml").toString();
         String loginConf = TestLoginModule.class.getResource("/login.conf").toString();
         StandaloneContainer.addConfigurationURL(containerConf);
         if (System.getProperty("java.security.auth.login.config") == null)
            System.setProperty("java.security.auth.login.config", loginConf);

         StandaloneContainer manager = StandaloneContainer.getInstance();

         authenticator = (DummyAuthenticatorImpl)manager.getComponentInstanceOfType(DummyAuthenticatorImpl.class);
         registry = (ConversationRegistry)manager.getComponentInstanceOfType(ConversationRegistry.class);
         assertNotNull(registry);

         listenerService = (ListenerService)manager.getComponentInstanceOfType(ListenerService.class);

      }

      registry.clear();

   }

   public void testConversationStateListener()
   {
   }

   AssertionFailedError assertionError = null;

   @SuppressWarnings("unchecked")
   public void testRegistry() throws Exception
   {
      Credential[] cred = new Credential[]{new UsernameCredential("exo")};

      String userId = authenticator.validateUser(cred);
      assertEquals("exo", userId);

      try
      {
         cred[0] = new UsernameCredential("enemy");
         authenticator.validateUser(cred);
         fail("login exception have been thrown");
      }
      catch (LoginException e)
      {
      }

      //
      Identity id = authenticator.createIdentity(userId);
      final ConversationState s = new ConversationState(id);

      //
      final Object payload = new Object();
      listenerService.addListener(new Listener()
      {
         @Override
         public void onEvent(Event event) throws Exception
         {
            try
            {
               assertNotNull(event);
               assertEquals("exo.core.security.ConversationRegistry.register", event.getEventName());
               assertTrue(event.getData() instanceof ConversationState);
               ConversationState cs = (ConversationState)event.getData();
               assertSame(s, cs);
               cs.setAttribute("payload", payload);
            }
            catch (AssertionFailedError error)
            {
               assertionError = error;
            }
         }

         @Override
         public String getName()
         {
            return "exo.core.security.ConversationRegistry.register";
         }
      });
      listenerService.addListener(new Listener()
      {
         @Override
         public void onEvent(Event event) throws Exception
         {
            try
            {
               assertNotNull(event);
               assertEquals("exo.core.security.ConversationRegistry.unregister", event.getEventName());
               assertTrue(event.getData() instanceof ConversationState);
               ConversationState cs = (ConversationState)event.getData();
               assertSame(s, cs);
            }
            catch (AssertionFailedError error)
            {
               if (assertionError == null)
               {
                  assertionError = error;
               }
            }
         }

         @Override
         public String getName()
         {
            return "exo.core.security.ConversationRegistry.unregister";
         }
      });

      //
      ConversationState.setCurrent(s);
      assertEquals(s, ConversationState.getCurrent());

      StateKey key = new SimpleStateKey("key");
      //
      registry.register(key, s);
      assertNotNull(registry.getState(key));
      assertEquals(id, registry.getState(key).getIdentity());
      assertSame(payload, s.getAttribute("payload"));

      //
      registry.unregister(key);

      // Rethrow any junit error that could have been thrown in the listener
      if (assertionError != null)
      {
         throw assertionError;
      }

      //
      assertNull(registry.getState(key));
   }

   public void testUnregisterByUserId() throws Exception
   {
      Credential[] cred = new Credential[]{new UsernameCredential("exo")};
      String userId = authenticator.validateUser(cred);
      Identity id = authenticator.createIdentity(userId);
      ConversationState state = new ConversationState(id);
      StateKey key1 = new SimpleStateKey("key1");
      StateKey key2 = new SimpleStateKey("key2");
      StateKey key3 = new SimpleStateKey("key3");
      registry.register(key1, state);
      registry.register(key2, state);
      registry.register(key3, state);
      assertEquals(3, registry.getStateKeys(userId).size());
      List<ConversationState> unregistered = registry.unregisterByUserId(userId);
      assertEquals(3, unregistered.size());
      assertEquals(0, registry.getStateKeys(userId).size());
   }

   public void testMemberships() throws Exception
   {
      MembershipEntry me = new MembershipEntry("exo");
      assertEquals("*", me.getMembershipType());

      Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
      memberships.add(new MembershipEntry("exogroup"));
      memberships.add(new MembershipEntry("exogroup1", "member"));

      Identity session = new Identity("exo", memberships);
      assertTrue(session.getGroups().size() > 1);

      assertTrue(session.isMemberOf("exogroup"));
      assertTrue(session.isMemberOf("exogroup1"));
      assertTrue(session.isMemberOf("exogroup", "member"));
      assertTrue(session.isMemberOf("exogroup1", "member"));
      assertFalse(session.isMemberOf("exogroup1", "validator"));
   }

   public void testDefaultRolesExtractor() throws Exception
   {
      Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
      memberships.add(new MembershipEntry("exogroup"));
      memberships.add(new MembershipEntry("exogroup/exogroup1/exogroup2", "member"));
      DefaultRolesExtractorImpl extractor = new DefaultRolesExtractorImpl();
      extractor.setUserRoleParentGroup("exogroup");
      Set<String> roles = extractor.extractRoles("exo", memberships);
      assertEquals(2, roles.size());
      assertTrue(roles.contains("exogroup"));
      assertTrue(roles.contains("exogroup2"));
      Identity session = new Identity("exo", memberships, roles);
      Collection<String> roles2 = session.getRoles();
      assertEquals(2, roles2.size());
   }

}
