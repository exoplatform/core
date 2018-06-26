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

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS<br>
 * This is the interface for the group data model.
 * The developer needs to call {@link GroupHandler#saveGroup(Group, boolean)} to persist the change.
 * @author <a href="mailto:benjmestrallet@users.sourceforge.net">Mestrallet Benjamin</a>
 * @LevelAPI Platform
 */
public interface Group extends Serializable
{
   /**
    * @return the id of the group. The id should have the form
    *         /ancestor/parent/groupname
    */
   public String getId();

   /**
    * Set the id of the group. The id should have the form
    *         /ancestor/parent/groupname
    * 
    * @param id
    */
   default public void setId(String id) {
     throw new UnsupportedOperationException("setId operation is not supported by this object class " + getClass().getName());
   }

   /**
    * @return the id of the parent group. if the parent id is null , it mean that
    *         the group is at the first level. the child of root group.
    */
   public String getParentId();

   /**
    * Set the id of the parent group. if the parent id is null , it mean that
    *         the group is at the first level. the child of root group.
    * 
    * @param parentId
    */
   default public void setParentId(String parentId) {
     throw new UnsupportedOperationException("setParentId operation is not supported by this object class " + getClass().getName());
   }

   /**
    * @return the name of the group
    */
   public String getGroupName();

   /**
    * @param name the name for the group
    */
   public void setGroupName(String name);

   /**
    * @return The display label of the group.
    */
   public String getLabel();

   /**
    * @param name The new label of the group
    */
   public void setLabel(String name);

   /**
    * @return The group description
    */
   public String getDescription();

   /**
    * @param desc The new description of the group
    */
   public void setDescription(String desc);

   /**
    * Set originating store name (internal or external)
    * 
    * @param originatingStore
    */
   default public void setOriginatingStore(String originatingStore) {
    throw new UnsupportedOperationException("originatingStore is not supported by this object class " + getClass().getName());
   }

   /**
    * @return originating store name (internal or external)
    */
   default public String getOriginatingStore() {
     throw new UnsupportedOperationException("originatingStore is not supported by this object class " + getClass().getName());
   }

   /**
    * @return true if the group was initially added to internal store
    */
   default public boolean isInternalStore() {
     throw new UnsupportedOperationException("originatingStore is not supported by this object class " + getClass().getName());
   }

}
