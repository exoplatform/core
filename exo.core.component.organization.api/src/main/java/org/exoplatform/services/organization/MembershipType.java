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
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Aug 21, 2003 Time: 3:22:54 PM This
 * is the interface for the membership type data model. Note that after each set
 * method is called. The developer need to call @see
 * MembershipTypeHandler.saveMembershipType(..) or
 * 
 * @see MembershipTypeHandler.createMembershipType(..) to persist the change
 * @LevelAPI Platform
 */
public interface MembershipType
{
   /**
    * @return the name name of the membership type. The name of the membershipt
    *         type should be unique in the membership type database.
    */
   public String getName();

   /**
    * @param s The name of the membership type
    */
   public void setName(String s);

   /**
    * @return The description of the membership type
    */
   public String getDescription();

   /**
    * @param s The new description of the membership type
    */
   public void setDescription(String s);

   /**
    * @return The owner of the membership
    */
   public String getOwner();

   /**
    * @param s The new owner of the membership
    */
   public void setOwner(String s);

   /**
    * @return The date that the membership type is saved to the database
    */
   public Date getCreatedDate();

   /**
    * @param d The date of creation the membership type
    */
   public void setCreatedDate(Date d);

   /**
    * @return The last time that an user modify the data of the membership type.
    */
   public Date getModifiedDate();

   /**
    * @param d The date of modification the membership type.
    */
   public void setModifiedDate(Date d);
}
