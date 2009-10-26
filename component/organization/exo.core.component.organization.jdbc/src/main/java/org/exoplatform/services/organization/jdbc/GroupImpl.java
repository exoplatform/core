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
/**
 * Created by The eXo Platform SAS        .
 * Author : Mestrallet Benjamin
 *          benjmestrallet@users.sourceforge.net
 * Date: Oct 6, 2003
 * Time: 5:04:37 PM
 */
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.services.database.DBObject;
import org.exoplatform.services.database.annotation.Table;
import org.exoplatform.services.database.annotation.TableField;
import org.exoplatform.services.organization.Group;

@Table(name = "EXO_GROUP", field = {
   @TableField(name = "GROUP_ID", type = "string", length = 100, unique = true, nullable = false),
   @TableField(name = "PARENT_ID", type = "string", length = 100),
   @TableField(name = "GROUP_NAME", type = "string", length = 500),
   @TableField(name = "LABEL", type = "string", length = 500),
   @TableField(name = "GROUP_DESC", type = "string", length = 1000)})
public class GroupImpl extends DBObject implements Group
{
   final static public String GROUP_ID_FIELD = "GROUP_ID";

   private String groupId;

   private String parentId;

   private String groupName;

   private String label;

   private String desc;

   public GroupImpl()
   {

   }

   public GroupImpl(String name)
   {
      groupName = name;
   }

   public String getId()
   {
      return groupId;
   }

   public void setId(String id)
   {
      this.groupId = id;
   }

   public String getParentId()
   {
      return parentId;
   }

   public void setParentId(String parentId)
   {
      this.parentId = parentId;
   }

   public String getGroupName()
   {
      return groupName;
   }

   public void setGroupName(String name)
   {
      this.groupName = name;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String s)
   {
      label = s;
   }

   public String getDescription()
   {
      return desc;
   }

   public void setDescription(String s)
   {
      desc = s;
   }

   public String toString()
   {
      return "Group[" + groupId + "|" + groupName + "]";
   }
}
