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
package org.exoplatform.services.security.jaas;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * This LoginModule should be used after customer LoginModule, which makes
 * authentication. This one registers Identity for user in IdentityRegistry.
 * Required name of user MUST be passed to LM via sharedState (see method
 * {@link #initialize(Subject, CallbackHandler, Map, Map)}), with name
 * javax.security.auth.login.name.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class IdentitySetLoginModule implements LoginModule
{

   /**
    * The name of the option to use in order to specify the name of the portal container
    */
   private static final String OPTION_PORTAL_CONTAINER_NAME = "portalContainerName";

   /**
    * The default name of the portal container
    */
   private static final String DEFAULT_PORTAL_CONTAINER_NAME = "portal";

   /**
    * Login.
    */
   protected Log log = ExoLogger.getLogger("core.IdentitySetLoginModule");

   /**
    * @see {@link Subject} .
    */
   protected Subject subject;

   /**
    * Shared state.
    */
   @SuppressWarnings("unchecked")
   protected Map sharedState;

   /**
    * Is allowed for one user login again if he already login. If must set in LM
    * options.
    */
   protected boolean singleLogin = false;

   /**
    * The name of the portal container.
    */
   private String portalContainerName;

   /**
    * {@inheritDoc}
    */
   public boolean abort() throws LoginException
   {
      if (log.isDebugEnabled())
      {
         log.debug("in abort");
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean commit() throws LoginException
   {
      if (log.isDebugEnabled())
      {
         log.debug("in commit");
      }

      String userId = (String)sharedState.get("javax.security.auth.login.name");
      try
      {
         Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);

         if (authenticator == null)
            throw new LoginException("No Authenticator component found, check your configuration.");

         IdentityRegistry identityRegistry =
            (IdentityRegistry)getContainer().getComponentInstanceOfType(IdentityRegistry.class);

         if (singleLogin && identityRegistry.getIdentity(userId) != null)
            throw new LoginException("User " + userId + " already logined.");

         Identity identity = authenticator.createIdentity(userId);
         identity.setSubject(subject);

         identityRegistry.register(identity);

      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new LoginException(e.getMessage());
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
   {
      if (log.isDebugEnabled())
      {
         log.debug("in initialize");
      }

      this.subject = subject;
      this.sharedState = sharedState;
      this.portalContainerName = getPortalContainerName(options);

      String sl = (String)options.get("singleLogin");
      if (sl != null && (sl.equalsIgnoreCase("yes") || sl.equalsIgnoreCase("true")))
      {
         this.singleLogin = true;
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean login() throws LoginException
   {
      if (log.isDebugEnabled())
      {
         log.debug("in login");
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean logout() throws LoginException
   {
      if (log.isDebugEnabled())
      {
         log.debug("in logout");
      }
      return true;
   }

   /**
    * @return actual ExoContainer instance.
    */
   protected ExoContainer getContainer() throws Exception
   {
      // TODO set correct current container
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (container instanceof RootContainer)
      {
         container = RootContainer.getInstance().getPortalContainer(portalContainerName);
      }
      return container;
   }

   @SuppressWarnings("unchecked")
   private String getPortalContainerName(Map options)
   {
      if (options != null)
      {
         String optionValue = (String)options.get(OPTION_PORTAL_CONTAINER_NAME);
         if (optionValue != null && optionValue.length() > 0)
         {
            if (log.isDebugEnabled())
               log.debug("The IdentitySetLoginModule will use the portal container " + optionValue);
            return optionValue;
         }
      }
      return DEFAULT_PORTAL_CONTAINER_NAME;
   }
}
