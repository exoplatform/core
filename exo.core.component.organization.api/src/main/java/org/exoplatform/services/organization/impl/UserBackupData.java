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
package org.exoplatform.services.organization.impl;

import java.util.Collection;

/**
 * May 28, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $ID$
 **/
public class UserBackupData
{
   private UserImpl user;

   private UserProfileImpl userProfile;

   private Collection<?> memberships;

   public UserBackupData(UserImpl u, UserProfileImpl up, Collection<?> mbs)
   {
      user = u;
      userProfile = up;
      memberships = mbs;
   }

   public UserImpl getUser()
   {
      return user;
   }

   public UserProfileImpl getUserProfile()
   {
      return userProfile;
   }

   public Collection<?> getMemberships()
   {
      return memberships;
   }
}
