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
import org.exoplatform.services.organization.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "EXO_USER")
public class UserImpl implements User, ExtendedCloneable
{

   @Id
   private String id = null;

   @Column
   private String userName = null;

   @Column
   private String password = null;

   @Column
   private String firstName = null;

   @Column
   private String lastName = null;

   @Column
   private String email = null;

   @Column
   private Date createdDate;

   @Column
   private Date lastLoginTime;

   @Column
   private String organizationId = null;

   @Column
   private String displayName = null;

   public UserImpl()
   {
   }

   public UserImpl(String username)
   {
      this.userName = username;
   }

   public String getDisplayName()
   {
      return displayName != null ? displayName : getFirstName() + " " + getLastName();
   }

   public void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String name)
   {
      this.userName = name;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   // wrapper method
   public String getFullName()
   {
      return getDisplayName();
   }

   public void setFullName(String fullName)
   {
      setDisplayName(fullName);
   }

   public Date getCreatedDate()
   {
      return createdDate;
   }

   public void setCreatedDate(Date t)
   {
      createdDate = t;
   }

   public Date getLastLoginTime()
   {
      return lastLoginTime;
   }

   public void setLastLoginTime(Date t)
   {
      lastLoginTime = t;
   }

   // toString
   public String toString()
   {
      return "User[" + id + "|" + userName + "]" + (organizationId == null ? "" : ("@" + organizationId));
   }

   public String getOrganizationId()
   {
      return organizationId;
   }

   public void setOrganizationId(String organizationId)
   {
      this.organizationId = organizationId;
   }

   /**
    * {@inheritDoc}
    */
   public UserImpl clone()
   {
      UserImpl ui;
      try
      {
         ui = (UserImpl)super.clone();
         if (createdDate != null)
         {
            ui.createdDate = (Date)createdDate.clone();
         }
         if (lastLoginTime != null)
         {
            ui.lastLoginTime = (Date)lastLoginTime.clone();
         }
      }
      catch (CloneNotSupportedException e)
      {
         return this;
      }

      return ui;
   }
}
