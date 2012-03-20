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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class BasicCallbackHandler implements CallbackHandler
{

   /**
    * Name. 
    */
   private String login;

   /**
    * Password.
    */
   private char[] password;

   /**
    * @param login name
    * @param password password
    */

   /**
    * Authentication method. 
    */
   private final String authMethod = "BASIC";

   public BasicCallbackHandler(String login, char[] password)
   {
      this.login = login;
      this.password = password;
   }

   /**
    * {@inheritDoc}
    */
   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         if (callbacks[i] instanceof NameCallback)
         {
            ((NameCallback)callbacks[i]).setName(login);
         }
         else if (callbacks[i] instanceof PasswordCallback)
         {
            ((PasswordCallback)callbacks[i]).setPassword(password);
         }
         else if (callbacks[i] instanceof TextInputCallback)
         {
            if ("authMethod".equals(((TextInputCallback)callbacks[i]).getPrompt()))
            {
               ((TextInputCallback)callbacks[i]).setText(authMethod);
            }
         }
         else
         {
            throw new UnsupportedCallbackException(callbacks[i], "Callback class not supported");
         }
      }
   }
}
