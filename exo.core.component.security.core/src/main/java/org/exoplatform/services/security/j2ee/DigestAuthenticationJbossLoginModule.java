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
package org.exoplatform.services.security.j2ee;

import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.jboss.security.auth.callback.MapCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Dmitry Kuleshov
 * @version $Id: $
 */

public class DigestAuthenticationJbossLoginModule extends JbossLoginModule
{
   /**
     * To retrieve password context during Digest Authentication.
     */
   private MapCallback[] mapCallback = {new MapCallback()};
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public boolean login() throws LoginException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("In login of JbossLoginModule.");
      }
      try
      {
         if (sharedState.containsKey("exo.security.identity"))
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Use Identity from previous LoginModule");
            }
            identity = (Identity)sharedState.get("exo.security.identity");
         }
         else
         {
            if (!digestAuthenticationIsUsed())
            {
               return super.login();
            }

            if (LOG.isDebugEnabled())
            {
               LOG.debug("Try create identity");
            }

            Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);

            if (authenticator == null)
            {
               throw new LoginException("No Authenticator component found, check your configuration");
            }

            String userId = authenticator.validateUser(getCredentials());

            identity = authenticator.createIdentity(userId);
            sharedState.put("javax.security.auth.login.name", userId);
            subject.getPrivateCredentials().add(getPassword());
            subject.getPublicCredentials().add(getUsername());
         }
         return true;

      }
      catch (final Exception e)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug(e.getMessage(), e);
         }

         throw new LoginException(e.getMessage());
      }
   }

   /**
    * An utility method handles mapCallback and also checks if digest authentication is used.
    * @return true if digest authentication is used, otherwise - false
    * @throws IOException
    */
   private boolean digestAuthenticationIsUsed() throws IOException
   {
      try
      {
         // here we're trying to handle mapCallback
         // if it is handled successfully than digest
         // authentication is used
         callbackHandler.handle(mapCallback);
         return true;
      }
      catch (UnsupportedCallbackException uce)
      {
         // otherwise UnsupportedCallbackException is thrown
         return false;
      }
   }

   /**
    * An utility method to retrieve credentials. All needed for password hashing information 
    * is retrieved from MapCallback. NameCallback and PasswordCallback are used to correspondingly  
    * retrieve username and password.
    * @return Credential
    * @throws IOException 
    * @throws Exception
    */
   private Credential[] getCredentials() throws IOException
   {
      String username = null;
      String password = null;
      Map<String, String> passwordContext = new HashMap<String, String>();

      passwordContext.put("qop", (String)mapCallback[0].getInfo("qop"));
      passwordContext.put("nonce", (String)mapCallback[0].getInfo("nonce"));
      passwordContext.put("cnonce", (String)mapCallback[0].getInfo("cnonce"));
      passwordContext.put("a2hash", (String)mapCallback[0].getInfo("a2hash"));
      passwordContext.put("nc", (String)mapCallback[0].getInfo("nc"));
      passwordContext.put("realm", (String)mapCallback[0].getInfo("realm"));

      try
      {
         Callback[] nameCallback = {new NameCallback("Username")};
         callbackHandler.handle(nameCallback);
         username = ((NameCallback)nameCallback[0]).getName();
      }
      catch (UnsupportedCallbackException e)
      {
         if (LOG.isErrorEnabled())
         {
            LOG.error("Error on retrieving username from callback handler! ", e);
         }
      }

      try
      {
         Callback[] passwordCallback = {new PasswordCallback("Password", false)};
         callbackHandler.handle(passwordCallback);
         password = new String(((PasswordCallback)passwordCallback[0]).getPassword());
         ((PasswordCallback)passwordCallback[0]).clearPassword();
      }
      catch (UnsupportedCallbackException e)
      {
         if (LOG.isErrorEnabled())
         {
            LOG.error("Error on retrieving password from callback handler! ", e);
         }
      }

      if (username == null || password == null)
      {
         return null;
      }

      return new Credential[]{new UsernameCredential(username), new PasswordCredential(password, passwordContext)};
   }

   private UsernameCredential getUsername() throws IOException
   {
      String username = null;

      try
      {
         Callback[] nameCallback = {new NameCallback("Username")};
         callbackHandler.handle(nameCallback);
         username = ((NameCallback)nameCallback[0]).getName();
      }
      catch (UnsupportedCallbackException e)
      {
         if (LOG.isErrorEnabled())
         {
            LOG.error("Error on retrieving username from callback handler! ", e);
         }
      }

      return new UsernameCredential(username);
   }

   private String getPassword() throws IOException
   {
      String password = null;

      try
      {
         Callback[] passwordCallback = {new PasswordCallback("Password", false)};
         callbackHandler.handle(passwordCallback);
         password = new String(((PasswordCallback)passwordCallback[0]).getPassword());
         ((PasswordCallback)passwordCallback[0]).clearPassword();
      }
      catch (UnsupportedCallbackException e)
      {
         if (LOG.isErrorEnabled())
         {
            LOG.error("Error on retrieving password from callback handler! ", e);
         }
      }

      return password;
   }
}
