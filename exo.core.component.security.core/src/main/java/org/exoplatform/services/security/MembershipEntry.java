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

/**
 * Created by The eXo Platform SAS .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 * @LevelAPI Platform
 */

public final class MembershipEntry
{

   public final static String ANY_TYPE = "*";

   private String membershipType;

   private String group;

   /**
    * Constructor with undefined membership type
    * 
    * @param group the group name
    */
   public MembershipEntry(String group)
   {
      this(group, null);
   }

   /**
    * @param group the group name
    * @param membershipType the membership type
    */
   public MembershipEntry(String group, String membershipType)
   {
      this.membershipType = membershipType != null ? membershipType : ANY_TYPE;
      if (group == null)
      {
         throw new IllegalArgumentException("Group is null");
      }
      this.group = group;
   }

   /**
    * @return the real membership type or "*" if not defined
    */
   public String getMembershipType()
   {
      return membershipType;
   }

   /**
    * @return the group name
    */
   public String getGroup()
   {
      return group;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!(obj instanceof MembershipEntry))
      {
         return false;
      }
      MembershipEntry me = (MembershipEntry)obj;
      if (membershipType.equals(ANY_TYPE) || me.membershipType.equals(ANY_TYPE))
      {
         return this.group.equals(me.group);
      }
      return this.group.equals(me.group) && this.membershipType.equals(me.membershipType);
   }

   
   /**
    * {@inheritDoc}
    */   
   @Override
   public int hashCode()
   {
      return group.hashCode();
   }

   public static MembershipEntry parse(String identityStr)
   {

      final int index = identityStr.indexOf(":");
      if (index != -1)
      {
         String membershipName = identityStr.substring(0, index);
         String groupName = identityStr.substring(index + 1);
         return new MembershipEntry(groupName, membershipName);
      }

      return null;

   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return getMembershipType() + ":" + getGroup();
   }
}
