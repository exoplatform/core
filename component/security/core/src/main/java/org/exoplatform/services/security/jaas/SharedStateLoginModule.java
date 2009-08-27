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
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class SharedStateLoginModule implements LoginModule
{

   /**
    * The name of the option to use in order to specify the name of the portal
    * container
    */
   private static final String OPTION_PORTAL_CONTAINER_NAME = "portalContainerName";

   /**
    * The default name of the portal container
    */
   private static final String DEFAULT_PORTAL_CONTAINER_NAME = "portal";

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(SharedStateLoginModule.class.getName());

   /**
    * The name of the portal container.
    */
   private String portalContainerName;

   /**
    * Shared state.
    */
   @SuppressWarnings("unchecked")
   private Map sharedState;

   /**
    * Subject.
    */
   private Subject subject;

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public boolean login() throws LoginException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("in login");
      }
      try
      {
         String username = (String)sharedState.get("javax.security.auth.login.name");
         String password = (String)sharedState.get("javax.security.auth.login.password");
         if (username == null || password == null)
            return false;

         Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);

         if (authenticator == null)
            throw new LoginException("No Authenticator component found, check your configuration");

         Credential[] credentials =
            new Credential[]{new UsernameCredential(username), new PasswordCredential(password)};

         String userId = authenticator.validateUser(credentials);
         Identity identity = authenticator.createIdentity(userId);

         sharedState.put("exo.security.identity", identity);
         sharedState.put("javax.security.auth.login.name", userId);
         // TODO use PasswordCredential wrapper
         subject.getPrivateCredentials().add(password);
         subject.getPublicCredentials().add(new UsernameCredential(username));
         return true;
      }
      catch (final Throwable e)
      {
         throw new LoginException(e.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean abort() throws LoginException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("in abort");
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean commit() throws LoginException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("in commit");
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("in initialize");
      }
      this.sharedState = sharedState;
      this.portalContainerName = getPortalContainerName(options);
      this.subject = subject;
   }

   /**
    * {@inheritDoc}
    */
   public boolean logout() throws LoginException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("in logout");
      }
      return true;
   }

   /**
    * @return actual ExoContainer instance.
    */
   protected ExoContainer getContainer() throws Exception
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (container instanceof RootContainer)
      {
         container = RootContainer.getInstance().getPortalContainer(portalContainerName);
      }
      return container;
   }

   /**
    * Return portal container name if it provide with options,
    * DEFAULT_PORTAL_CONTAINER_NAME otherwise.
    * 
    * @param options
    * @return
    */
   @SuppressWarnings("unchecked")
   private String getPortalContainerName(Map options)
   {
      if (options != null)
      {
         String optionValue = (String)options.get(OPTION_PORTAL_CONTAINER_NAME);
         if (optionValue != null && optionValue.length() > 0)
         {
            if (LOG.isDebugEnabled())
               LOG.debug("The IdentitySetLoginModule will use the portal container " + optionValue);
            return optionValue;
         }
      }
      return DEFAULT_PORTAL_CONTAINER_NAME;
   }
}
