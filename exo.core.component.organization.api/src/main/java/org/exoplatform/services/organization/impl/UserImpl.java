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

import org.exoplatform.services.organization.User;

import java.util.Date;

/**
 * @hibernate.class table="EXO_USER"
 */
public class UserImpl implements User, Cloneable
{

   private String id = null;

   private String userName = null;

   private transient String password = null;

   private String firstName = null;

   private String lastName = null;

   private String email = null;

   private Date createdDate;

   private Date lastLoginTime;

   private String organizationId = null;

   public UserImpl()
   {
   }

   public UserImpl(String username)
   {
      this.userName = username;
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
   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String name)
   {
      this.userName = name;
   }

   /**
    * @hibernate.property
    **/
   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * @hibernate.property
    **/
   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   /**
    * @hibernate.property
    **/
   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   /**
    * @hibernate.property
    **/
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
      return getFirstName() + " " + getLastName();
   }

   public void setFullName(String fullName)
   {

   }

   /**
    * @hibernate.property
    **/
   public Date getCreatedDate()
   {
      return createdDate;
   }

   public void setCreatedDate(Date t)
   {
      createdDate = t;
   }

   /**
    * @hibernate.property
    **/
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
   public Object clone()
   {
      UserImpl ui = new UserImpl(userName);

      ui.setId(id);
      ui.setCreatedDate(createdDate);
      ui.setEmail(email);
      ui.setFirstName(firstName);
      ui.setLastLoginTime(lastLoginTime);
      ui.setLastName(lastName);
      ui.setOrganizationId(organizationId);
      ui.setPassword(password);

      return ui;
   }
}
