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
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.services.database.DBObject;
import org.exoplatform.services.database.annotation.Table;
import org.exoplatform.services.database.annotation.TableField;
import org.exoplatform.services.organization.Membership;

@Table(name = "EX0_MEMBERSHIP", field = {
   @TableField(name = "MEMBERSHIP_ID", type = "string", length = 100, unique = true, nullable = false),
   @TableField(name = "MEMBERSHIP_TYPE", type = "string", length = 100),
   @TableField(name = "GROUP_ID", type = "string", length = 100),
   @TableField(name = "USER_NAME", type = "string", length = 500)})
public class MembershipImpl extends DBObject implements Membership
{

   private String id = null;

   private String membershipType = "member";

   private String groupId = null;

   private String userName = null;

   public MembershipImpl()
   {
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getMembershipType()
   {
      return membershipType;
   }

   public void setMembershipType(String type)
   {
      this.membershipType = type;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String user)
   {
      this.userName = user;
   }

   public String getGroupId()
   {
      return groupId;
   }

   public void setGroupId(String group)
   {
      this.groupId = group;
   }

   public String toString()
   {
      return "Membership[" + id + "]";
   }
}
