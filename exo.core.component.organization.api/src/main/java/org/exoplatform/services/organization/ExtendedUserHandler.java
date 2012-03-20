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

package org.exoplatform.services.organization;

import org.exoplatform.services.security.PasswordEncrypter;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Dmitry Kuleshov
 * @version $Id: $
 */

public interface ExtendedUserHandler
{
   /**
    * Checks if user's credentials are valid.
    * It is more flexible because Credential may contain password context
    * or some other useful data.
    * @param credentials
    * @return return true if the username and the password matches 
    * the database record, else return false.
    * @throws Exception throw an exception if cannot access the database
    */
   public boolean authenticate(String username, String password, PasswordEncrypter pe) throws Exception;
}
