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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.StateKey;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ConversationStateListener implements HttpSessionListener
{

   /**
    * Logger.
    */
   protected Log log = ExoLogger.getLogger("core.security.ConversationStateListener");

   /**
    * {@inheritDoc}
    */
   public void sessionCreated(HttpSessionEvent event)
   {
      // nothing to do here
   }

   /**
    * Remove {@link ConversationState}. {@inheritDoc}
    */
   public void sessionDestroyed(HttpSessionEvent event)
   {
      HttpSession httpSession = event.getSession();
      StateKey stateKey = new HttpSessionStateKey(httpSession);

      ConversationRegistry conversationRegistry =
         (ConversationRegistry)getContainer(httpSession.getServletContext()).getComponentInstanceOfType(
            ConversationRegistry.class);

      ConversationState conversationState = conversationRegistry.unregister(stateKey);

      if (conversationState != null)
         if (log.isDebugEnabled())
            log.debug("Remove conversation state " + httpSession.getId());

   }

   /**
    * @param sctx {@link ServletContext}
    * @return actual ExoContainer instance
    */
   protected ExoContainer getContainer(ServletContext sctx)
   {
      return PortalContainer.getInstance(sctx);
   }

}
