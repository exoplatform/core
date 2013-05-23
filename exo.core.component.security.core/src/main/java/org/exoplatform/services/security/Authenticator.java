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
package org.exoplatform.services.security;

import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS . Component responsible for user
 * authentication (session creation) In JAAS LoginModule typically called in
 * login() method
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 * @LevelAPI Platform
 */

public interface Authenticator
{
   /**
    * Authenticate user and return userId which can be different to username.
    * 
    * @param credentials - list of users credentials (such as name/password, X509
    *          certificate etc)
    * @return userId the user's identifier.
    * @throws LoginException in case the authentication fails
    * @throws Exception if any exception occurs
    */
   String validateUser(Credential[] credentials) throws LoginException, Exception;

   /**
    * @param userId the user's identifier
    * @return returns the Identity representing the user
    * @throws Exception if any exception occurs
    */
   Identity createIdentity(String userId) throws Exception;

   /**
    * Gives the last exception that occurs while calling {@link #validateUser(Credential[])}. This
    * allows applications outside JAAS like UI to be able to know which exception occurs
    * while calling {@link #validateUser(Credential[])}.
    * @return the original Exception that occurs while calling {@link #validateUser(Credential[])} 
    * for the very last time if an exception occurred, <code>null</code> otherwise.
    */
   Exception getLastExceptionOnValidateUser();
}
