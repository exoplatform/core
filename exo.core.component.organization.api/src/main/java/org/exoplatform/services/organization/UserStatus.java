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
 * This enumeration is used to be able to query users according to their status (enabled, disabled, both)
 *
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public enum UserStatus
{
   ENABLED
   {
      public boolean matches(boolean enabled)
      {
         return enabled;
      }

      public boolean acceptsEnabled()
      {
         return true;
      }
   },
   DISABLED
   {
      public boolean matches(boolean enabled)
      {
         return !enabled;
      }

      public boolean acceptsEnabled()
      {
         return false;
      }
   },
   BOTH
   {
      public boolean matches(boolean enabled)
      {
         return true;
      }

      public boolean acceptsEnabled()
      {
         return true;
      }
   };

   /**
    * Provides the corresponding {@link UserStatus}
    */
   public static UserStatus getStatus(boolean enabled)
   {
      return enabled ? ENABLED : DISABLED;
   }

   /**
    * Indicates whether the status matches with the provided flag
    * @return <code>true</code> if it matches, <code>false</code> otherwise
    */
   public abstract boolean matches(boolean enabled);

   /**
    * Indicates whether or not the status accepts enabled user
    * @return <code>true</code> if it accepts, <code>false</code> otherwise
    */
   public abstract boolean acceptsEnabled();
}

