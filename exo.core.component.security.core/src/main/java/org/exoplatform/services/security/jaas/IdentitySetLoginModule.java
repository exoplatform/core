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
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

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
public class IdentitySetLoginModule extends AbstractLoginModule
{

   /**
    * Login.
    */
   protected Log log = ExoLogger.getLogger("exo.core.component.security.core.IdentitySetLoginModule");

   /**
    * Is allowed for one user login again if he already login. If must set in LM
    * options.
    */
   protected boolean singleLogin;

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
         // TODO Remove subject from identity if nod need it in eXo environment.
         // Do not need implement logout by self if use tomcat 6.0.21 and later.
         // See deprecation comments in
         // org.exoplatform.services.security.web.JAASConversationStateListener
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
   public void afterInitialize()
   {
      if (log.isDebugEnabled())
      {
         log.debug("in initialize");
      }

      String sl = (String)options.get("singleLogin");
      this.singleLogin = (sl != null && (sl.equalsIgnoreCase("yes") || sl.equalsIgnoreCase("true")));
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
    * {@inheritDoc}
    */
   @Override
   protected Log getLogger()
   {
      return log;
   }
}
