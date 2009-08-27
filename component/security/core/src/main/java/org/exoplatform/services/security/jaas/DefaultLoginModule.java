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
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class DefaultLoginModule implements LoginModule
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
   protected Log log = ExoLogger.getLogger("core.DefaultLoginModule");

   /**
    * @see {@link Subject} .
    */
   protected Subject subject;

   /**
    * @see {@link CallbackHandler}
    */
   private CallbackHandler callbackHandler;

   /**
    * encapsulates user's principals such as name, groups, etc .
    */
   protected Identity identity;

   /**
    * Shared state.
    */
   @SuppressWarnings("unchecked")
   protected Map sharedState;

   /**
    * The name of the portal container.
    */
   private String portalContainerName;

   /**
    * Is allowed for one user login again if he already login. If must set in LM
    * options.
    */
   protected boolean singleLogin = false;

   /**
    * Default constructor.
    */
   public DefaultLoginModule()
   {
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
   {
      this.subject = subject;
      this.callbackHandler = callbackHandler;
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
   @SuppressWarnings("unchecked")
   public boolean login() throws LoginException
   {
      if (log.isDebugEnabled())
         log.debug("In login of DefaultLoginModule.");

      try
      {
         if (sharedState.containsKey("exo.security.identity"))
         {
            if (log.isDebugEnabled())
               log.debug("Use Identity from previous LoginModule");
            identity = (Identity)sharedState.get("exo.security.identity");
         }
         else
         {
            if (log.isDebugEnabled())
               log.debug("Try create identity");
            Callback[] callbacks = new Callback[2];
            callbacks[0] = new NameCallback("Username");
            callbacks[1] = new PasswordCallback("Password", false);

            callbackHandler.handle(callbacks);
            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());
            ((PasswordCallback)callbacks[1]).clearPassword();
            if (username == null || password == null)
               return false;

            Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);

            if (authenticator == null)
               throw new LoginException("No Authenticator component found, check your configuration");

            Credential[] credentials =
               new Credential[]{new UsernameCredential(username), new PasswordCredential(password)};

            String userId = authenticator.validateUser(credentials);
            identity = authenticator.createIdentity(userId);
            sharedState.put("javax.security.auth.login.name", userId);
            // TODO use PasswordCredential wrapper
            subject.getPrivateCredentials().add(password);
            subject.getPublicCredentials().add(new UsernameCredential(username));
         }
         return true;

      }
      catch (final Throwable e)
      {
         log.error(e.getLocalizedMessage());
         throw new LoginException(e.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean commit() throws LoginException
   {
      try
      {

         IdentityRegistry identityRegistry =
            (IdentityRegistry)getContainer().getComponentInstanceOfType(IdentityRegistry.class);

         if (singleLogin && identityRegistry.getIdentity(identity.getUserId()) != null)
            throw new LoginException("User " + identity.getUserId() + " already logined.");

         identity.setSubject(subject);
         identityRegistry.register(identity);

      }
      catch (final Throwable e)
      {
         log.error(e.getLocalizedMessage());
         throw new LoginException(e.getMessage());
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean abort() throws LoginException
   {
      if (log.isDebugEnabled())
         log.debug("In abort of DefaultLoginModule.");
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean logout() throws LoginException
   {
      if (log.isDebugEnabled())
         log.debug("In logout of DefaultLoginModule.");

      return true;
   }

   /**
    * @return actual ExoContainer instance.
    */
   protected ExoContainer getContainer()
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
            if (log.isDebugEnabled())
               log.debug("The DefaultLoginModule will use the portal container " + optionValue);
            return optionValue;
         }
      }
      return DEFAULT_PORTAL_CONTAINER_NAME;
   }
}
