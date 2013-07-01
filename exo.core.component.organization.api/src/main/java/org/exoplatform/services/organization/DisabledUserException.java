/*
 * Copyright (C) 2013 eXo Platform SAS.
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

/**
 * This exception is used when we try to interact illegally with a disabled user account.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DisabledUserException extends Exception
{

   /**
    * The serial version id
    */
   private static final long serialVersionUID = 1751536769113302305L;

   /**
    * The user name of the disabled account with which we try to interact illegally
    */
   private final String username;

   /**
    * Constructs the exception with only the user name
    * @param username the user name of the disabled account
    */
   public DisabledUserException(String username)
   {
      this.username = username;
   }

   /**
    * This constructor is used when we could not check if the user account
    * is disabled or not due to another exception. By default we consider
    * the user account as disabled so we raise a {@link DisabledUserException}
    * that we created with this constructor.
    * @param username the user name of the potentially disabled account
    * @param cause The exception that prevents to check whether the user
    *              account is disabled or not.
    */
   public DisabledUserException(String username, Throwable cause)
   {
      super(cause);
      this.username = username;
   }

   /**
    * @return the username of the disabled account
    */
   public String getUsername()
   {
      return username;
   }
}
