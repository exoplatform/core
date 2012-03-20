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

import org.exoplatform.services.organization.ExtendedCloneable;
import org.exoplatform.services.organization.Membership;

/**
 * @hibernate.class table="EXO_MEMBERSHIP"
 */
public class MembershipImpl implements Membership, ExtendedCloneable
{

   private String id = null;

   private String membershipType = "member";

   private String userName = null;

   private String groupId = null;

   public MembershipImpl()
   {
   }

   /**
    * @hibernate.id generator-class="assigned" unsaved-value="null"
    ***/
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      // new Exception("MODIFY MEMBERSHIP ID , old id: " + this.id + " new id : "
      // +id).printStackTrace() ;
      this.id = id;
   }

   /**
    * @hibernate.property
    **/
   public String getMembershipType()
   {
      return membershipType;
   }

   public void setMembershipType(String type)
   {
      this.membershipType = type;
   }

   /**
    * @hibernate.property
    **/
   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String user)
   {
      this.userName = user;
   }

   /**
    * @hibernate.property
    **/
   public String getGroupId()
   {
      return groupId;
   }

   public void setGroupId(String group)
   {
      this.groupId = group;
   }

   // toString
   public String toString()
   {
      return "Membership[" + id + "]";
   }

   /**
    * {@inheritDoc}
    **/
   public MembershipImpl clone()
   {
      try
      {
         return (MembershipImpl)super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         return this;
      }
   }
}
