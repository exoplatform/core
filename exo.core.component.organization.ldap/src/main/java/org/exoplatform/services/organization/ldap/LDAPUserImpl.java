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
package org.exoplatform.services.organization.ldap;

import org.exoplatform.services.organization.impl.UserImpl;

/**
 * This sub class of {@link UserImpl} is used to store the value of the field <i>userAccountControl</i>
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
@Deprecated
public class LDAPUserImpl extends UserImpl
{

   /**
    * The value of the field <i>userAccountControl</i>
    */
   private int userAccountControl;

   /**
    * @return the userAccountControl
    */
   public int getUserAccountControl()
   {
      return userAccountControl;
   }

   /**
    * @param userAccountControl the userAccountControl to set
    */
   public void setUserAccountControl(int userAccountControl)
   {
      this.userAccountControl = userAccountControl;
   }
}
