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
import org.exoplatform.services.organization.MembershipType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * Created by The eXo Platform SAS .
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Jun 14, 2003 Time: 1:12:22 PM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "EXO_MEMBERSHIP_TYPE")
public class MembershipTypeImpl implements MembershipType, ExtendedCloneable
{
  private static final long serialVersionUID = -8441178538275068955L;

  @Id
   private String name;

   @Column
   private String description;

   @Column
   private String owner;

   @Column
   private Date createdDate;

   @Column
   private Date modifiedDate;

   public MembershipTypeImpl()
   {
   }

   public MembershipTypeImpl(String name, String owner, String desc)
   {
      this.name = name;
      this.owner = owner;
      this.description = desc;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String s)
   {
      description = s;
   }

   public String getOwner()
   {
      return owner;
   }

   public void setOwner(String s)
   {
      owner = s;
   }

   public Date getCreatedDate()
   {
      return createdDate;
   }

   public void setCreatedDate(Date d)
   {
      createdDate = d;
   }

   public Date getModifiedDate()
   {
      return modifiedDate;
   }

   public void setModifiedDate(Date d)
   {
      modifiedDate = d;
   }

   /**
    * {@inheritDoc}
    **/
   public MembershipTypeImpl clone()
   {
      MembershipTypeImpl mti;
      try
      {
         mti = (MembershipTypeImpl)super.clone();
         if (createdDate != null)
         {
            mti.createdDate = (Date)createdDate.clone();
         }
         if (modifiedDate != null)
         {
            mti.modifiedDate = (Date)modifiedDate.clone();
         }
      }
      catch (CloneNotSupportedException e)
      {
         return this;
      }

      return mti;
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembershipTypeImpl that = (MembershipTypeImpl) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(modifiedDate, that.modifiedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, owner, createdDate, modifiedDate);
    }
}
