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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class DefaultLoginModule extends AbstractLoginModule
{

   /**
    * Logger.
    */
   protected Log log = ExoLogger.getLogger("exo.core.component.security.core.DefaultLoginModule");

   /**
    * encapsulates user's principals such as name, groups, etc .
    */
   protected Identity identity;

   /**
    * Is allowed for one user login again if he already login.
    * If must set in LM options.
    */
   protected boolean singleLogin;

   /**
    * Default constructor.
    */
   public DefaultLoginModule()
   {
   }

   /**
    * {@inheritDoc} 
    */
   public void afterInitialize()
   {
      String sl = (String)options.get("singleLogin");
      this.singleLogin = (sl != null && (sl.equalsIgnoreCase("yes") || sl.equalsIgnoreCase("true")));
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
    * {@inheritDoc}
    */
   @Override
   protected Log getLogger()
   {
      return log;
   }
}
