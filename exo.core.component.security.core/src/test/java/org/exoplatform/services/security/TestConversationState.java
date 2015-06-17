/*
 * Copyright (C) 2015 eXo Platform SAS.
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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by The eXo Platform SAS<br>
 *
 * @author Aymen Boughzela
 */

public class TestConversationState extends TestCase
{
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.security.TestConversationState");

   protected ConversationRegistry registry;

   protected Authenticator authenticator;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      String containerConf = TestLoginModule.class.getResource("/conf/standalone/test-configuration.xml").toString();
      String loginConf = TestLoginModule.class.getResource("/login.conf").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      if (System.getProperty("java.security.auth.login.config") == null)
      {
         System.setProperty("java.security.auth.login.config", loginConf);
      }

      StandaloneContainer manager = StandaloneContainer.getInstance();
      authenticator = (DummyAuthenticatorImpl)manager.getComponentInstanceOfType(DummyAuthenticatorImpl.class);
      registry = (ConversationRegistry)manager.getComponentInstanceOfType(ConversationRegistry.class);
      assertNotNull(registry);
   }

   public void testConcurrentConversationState() throws Exception
   {
      Identity id = authenticator.createIdentity("exo");
      final ConversationState state = new ConversationState(id);
      final StateKey key = new SimpleStateKey("key");
      registry.register(key, state);

      final AtomicInteger count = new AtomicInteger(0);

      List<Thread> threads = new LinkedList<Thread>();
      final CountDownLatch latch = new CountDownLatch(1);
      long start = System.currentTimeMillis();
      for (int i = 0; i < 100; i++)
      {
         final int idx = i;

         Thread t = new Thread(new Runnable()
         {
            public void run()
            {
               try
               {
                  latch.await();
               }
               catch (InterruptedException e)
               {
                  LOG.error(e);
               }
               ConversationState current = registry.getState(key);

               try
               {
                  for (int j = 0; j < 100; j++)
                  {
                     current.setAttribute("test" + idx + ":" + j, "value" + j);
                  }

                  for (int j = 0; j < 90; j++)
                  {
                     current.removeAttribute("test" + idx + ":" + j);
                  }
               }
               catch (Throwable e)//NOSONAR
               {
                  LOG.error(e);
               }
               finally
               {
                  count.addAndGet(1);
               }

            }
         });
         threads.add(t);
      }

      for (Thread t : threads)
      {
         t.start();
      }
      latch.countDown();

      for (Thread t : threads) {
         t.join();
      }

      while (count.get() < 50) {
         Thread.sleep(1000);
      }
      for (int i = 0; i < 20; i++)
      {
         for (int j = 90; j < 100; j++)
         {
            assertEquals("value" + j, state.getAttribute("test" + i + ":" + j));
         }
      }
      LOG.info("Time execution : " + (System.currentTimeMillis() - start));
   }

}
