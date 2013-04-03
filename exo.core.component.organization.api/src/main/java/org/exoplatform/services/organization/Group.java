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

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Aug 21, 2003 Time: 3:22:54 PM This
 * is the interface for the group data model. Note that after each set method is
 * called. The developer need to call @see GroupHandler.saveGroup(..) to persist
 * the change
 * @LevelAPI Platform
 */
public interface Group
{
   /**
    * @return the id of the group. The id should have the form
    *         /ancestor/parent/groupname
    * @LevelAPI Platform
    */
   public String getId();

   /**
    * @return the id of the parent group. if the parent id is null , it mean that
    *         the group is at the first level. the child of root group.
    * @LevelAPI Platform
    */
   public String getParentId();

   /**
    * @return the local name of the group
    * @LevelAPI Platform
    */
   public String getGroupName();

   /**
    * @param name the local name for the group
    * @LevelAPI Platform
    */
   public void setGroupName(String name);

   /**
    * @return The display label of the group.
    * @LevelAPI Platform
    */
   public String getLabel();

   /**
    * @param name The new label of the group
    * @LevelAPI Platform
    */
   public void setLabel(String name);

   /**
    * @return The group description
    * @LevelAPI Platform
    */
   public String getDescription();

   /**
    * @param desc The new description of the group
    * @LevelAPI Platform
    */
   public void setDescription(String desc);
}
