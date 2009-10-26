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
package org.exoplatform.services.document.diff;

public interface ToString
{

   /**
    * Default implementation of the {@link java.lang.Object#toString toString() }
    * method that delegates work to a {@link java.lang.StringBuffer StringBuffer}
    * base version.
    */
   public abstract String toString();

   /**
    * Place a string image of the object in a StringBuffer.
    * 
    * @param s the string buffer.
    */
   public abstract void toString(StringBuffer s);

}
