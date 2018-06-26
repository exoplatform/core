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
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Date: Oct 6, 2003
 * Time: 5:04:37 PM
 */
package org.exoplatform.services.organization.impl;

import java.util.Objects;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.exoplatform.services.organization.*;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "EXO_GROUP")
public class GroupImpl implements Group, ExtendedCloneable
{
  private static final long serialVersionUID = -5909516396351606340L;

  @Id
   private String id;

   @Column
   private String parentId;

   @Column
   private String groupName;

   @Column
   private String label;

   @Column(name = "description")
   private String desc;

   @Column(name = "store")
   private String originatingStore;

   public GroupImpl()
   {

   }

   public GroupImpl(String name)
   {
      groupName = name;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
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
      return "Group[" + id + "|" + groupName + "]";
   }

   /**
    * Set originating store name (internal or external)
    * 
    * @param originatingStore
    */
   public void setOriginatingStore(String originatingStore) {
     this.originatingStore = originatingStore;
   }

   /**
    * @return originating store name (internal or external)
    */
   public String getOriginatingStore() {
     return originatingStore;
   }

   /**
    * @return true if the group was initially added to internal store
    */
   public boolean isInternalStore() {
     return originatingStore == null || OrganizationService.INTERNAL_STORE.equals(originatingStore);
   }

   /**
    * {@inheritDoc}
    **/
   public GroupImpl clone()
   {
      try
      {
         return (GroupImpl)super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         return this;
      }
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupImpl group = (GroupImpl) o;
        return Objects.equals(id, group.id) &&
                Objects.equals(parentId, group.parentId) &&
                Objects.equals(groupName, group.groupName) &&
                Objects.equals(label, group.label) &&
                Objects.equals(desc, group.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, groupName, label, desc);
    }
}
