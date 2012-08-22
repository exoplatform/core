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
package org.exoplatform.services.security.web;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.StateKey;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * @deprecated Since use tomcat as default web-container do need use this
 *             listener any more. In tomcat 6.0.21 and later logout already
 *             implemented in
 *             <strong>org.apache.catalina.realm.GenericPrincipal</strong>.
 *             Detains described <a
 *             href="https://issues.apache.org/bugzilla/show_bug.cgi?id=39231"
 *             >here</a> . Should use {@link ConversationStateListener} instead.
 */
public class JAASConversationStateListener extends ConversationStateListener
{

   /**
    * {@inheritDoc}
    */
   @Override
   public void sessionDestroyed(HttpSessionEvent event)
   {
      HttpSession httpSession = event.getSession();
      StateKey stateKey = new HttpSessionStateKey(httpSession);
      try
      {
         ExoContainer container = getContainerIfPresent(httpSession.getServletContext());
         if (container != null)
         {
            ConversationRegistry conversationRegistry =
               (ConversationRegistry)container.getComponentInstanceOfType(ConversationRegistry.class);

            ConversationState conversationState = conversationRegistry.unregister(stateKey);

            if (conversationState != null)
            {
               if (LOG.isDebugEnabled())
                  LOG.debug("Remove conversation state " + httpSession.getId());
               if (conversationState.getAttribute(ConversationState.SUBJECT) != null)
               {
                  Subject subject = (Subject)conversationState.getAttribute(ConversationState.SUBJECT);
                  String realmName =
                     container instanceof PortalContainer ? ((PortalContainer)container).getRealmName()
                        : PortalContainer.DEFAULT_REALM_NAME;
                  LoginContext ctx = new LoginContext(realmName, subject);
                  ctx.logout();
               }
               else
               {
                  if (LOG.isDebugEnabled())
                  {
                     LOG.warn("Subject was not found in ConversationState attributes.");
                  }
               }
            }
         }
      }
      catch (LoginException e)
      {
         LOG.error("Can't remove conversation state " + httpSession.getId());
      }
      catch (SecurityException e)
      {
         LOG.error("Can't remove conversation state " + httpSession.getId());
      }
   }

}
