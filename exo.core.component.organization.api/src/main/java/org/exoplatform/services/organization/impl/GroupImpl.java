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
package org.exoplatform.services.organization.impl;

import org.exoplatform.services.organization.Group;

/**
 * @hibernate.class table="EXO_GROUP"
 */
public class GroupImpl implements Group, Cloneable
{

   private String id;

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

   /**
    * @hibernate.id generator-class="assigned" unsaved-value="null"
    ***/
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * @hibernate.property
    **/
   public String getParentId()
   {
      return parentId;
   }

   public void setParentId(String parentId)
   {
      this.parentId = parentId;
   }

   /**
    * @hibernate.property
    **/
   public String getGroupName()
   {
      return groupName;
   }

   public void setGroupName(String name)
   {
      this.groupName = name;
   }

   /**
    * @hibernate.property
    **/
   public String getLabel()
   {
      return label;
   }

   public void setLabel(String s)
   {
      label = s;
   }

   /**
    * @hibernate.property
    **/
   public String getDescription()
   {
      return desc;
   }

   public void setDescription(String s)
   {
      desc = s;
   }

   /**
    * @hibernate.many-to-one 
    *                        class="org.exoplatform.services.organization.impl.GroupImpl"
    *                        column="parent" name="parent"
    */
   /*
    * public Group getParent() { return parent; } public void setParent(Group
    * parent) { this.parent = parent; }
    */

   /**
    * @hibernate.set name="children" cascade="all" lazy="true"
    * @hibernate.collection-key column="parent"
    * @hibernate.collection-one-to-many 
    *                                   class="org.exoplatform.services.organization.impl.GroupImpl"
    */
   /*
    * public Collection getChildren() { return children; } public void
    * setChildren(Collection children) { this.children = children; } public void
    * addChild(GroupImpl c) { c.setParent(this); children.add(c); }
    */

   public String toString()
   {
      return "Group[" + id + "|" + groupName + "]";
   }

   /**
    * {@inheritDoc}
    */
   public Object clone()
   {
      GroupImpl gi = new GroupImpl(groupName);

      gi.setDescription(desc);
      gi.setId(id);
      gi.setLabel(label);
      gi.setParentId(parentId);

      return gi;
   }
}
