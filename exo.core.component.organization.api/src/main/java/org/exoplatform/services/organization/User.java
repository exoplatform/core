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

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Date: Aug 21, 2003 Time: 3:22:54 PM
 * This is the interface for a User data model. The OrganizationService implementor
 * can use the different strategy to implement this class, he can use the native
 * field for each get method or use a Map to hold the user data. Note that after
 * each set method is called. The developer need to call
 * 
 * @see UserHandler saveUser(..) to persist the change
 * @LevelAPI Platform
 */
public interface User
{
   /**
    * This method should return the username of the user. The username should be
    * unique and the user database should not have 2 user record with the same
    * username
    * 
    * @return the name of the user account
    */
   String getUserName();

   /**
    * This method is used to change the username
    * 
    * @param s the name of the user account
    * @deprecated The third party developer should not used this method
    */
   void setUserName(String s);

   /**
    * @return This method return the password of the user account
    */
   String getPassword();

   /**
    * This method is used to change the user account password.
    * 
    * @param s  the password of the user account
    */
   void setPassword(String s);

   /**
    * @return This method return the first name of the user
    */
   String getFirstName();

   /**
    * @param s the new first name
    */
   void setFirstName(String s);

   /**
    * @return The last name of the user
    */
   String getLastName();

   /**
    * @param s The new last name of the user
    */
   void setLastName(String s);

   /**
    * @return return the full name of the user. The full name should have the
    *         format: first name, last name by default
    * @deprecated This method call getDiplayName
    */
   String getFullName();

   /**
    * @param s The name that should show in the full name
    * @deprecated This method call setDiplayName
    */
   void setFullName(String s);

   /**
    * @return The email address of the user
    */
   String getEmail();

   /**
    * @param s The new user email address
    */
   void setEmail(String s);

   /**
    * @return the creation date of the user account
    */
   Date getCreatedDate();

   /**
    * @param t the creation date of the user account
    * @deprecated The third party should not used this method.
    */
   void setCreatedDate(Date t);

   /**
    * @return Returns the date corresponding to the last time that the user signed in to the platform
    */
   Date getLastLoginTime();

   /**
    * @param t the date corresponding to the last time that the user signed in to the platform
    * @deprecated The third party developer should not aware of this method
    */
   void setLastLoginTime(Date t);

   /**
    * @return return the display name
    */
   String getDisplayName();

   /**
    * @param displayName The name that should show in the display name
    */
   void setDisplayName(String displayName);

   /**
    * @return returns the id of organization to which the user belong or null if not
    *         applicable
    */
   String getOrganizationId();

   /**
    * sets the organizationId
    * @param organizationId the id of organization to which the user belong
    */
   void setOrganizationId(String organizationId);

   /**
    * Indicates whether the user is enabled or not
    * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
    */
   boolean isEnabled();
}
