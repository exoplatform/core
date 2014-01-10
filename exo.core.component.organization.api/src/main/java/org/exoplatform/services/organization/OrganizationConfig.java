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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 27, 2004
 * @version $Id: OrganizationConfig.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class OrganizationConfig
{
   private List<?> membershipType;

   private List<?> group;

   private List<?> user;

   public OrganizationConfig()
   {
      membershipType = new ArrayList<Object>(5);
      group = new ArrayList<Object>(5);
      user = new ArrayList<Object>(5);
   }

   public List<?> getGroup()
   {
      return group;
   }

   public void setGroup(List<?> group)
   {
      this.group = group;
   }

   public List<?> getMembershipType()
   {
      return membershipType;
   }

   public void setMembershipType(List<?> membershipType)
   {
      this.membershipType = membershipType;
   }

   public List<?> getUser()
   {
      return user;
   }

   public void setUser(List<?> user)
   {
      this.user = user;
   }

   static public class MembershipType
   {
      private String type;

      private String description;

      public String getDescription()
      {
         return description;
      }

      public void setDescription(String description)
      {
         this.description = description;
      }

      public String getType()
      {
         return type;
      }

      public void setType(String type)
      {
         this.type = type;
      }
   }

   static public class Group
   {
      private String name;

      private String description;

      private String parentId;

      private String label;

      // --------------------------------------

      public String getDescription()
      {
         return description;
      }

      public void setDescription(String description)
      {
         this.description = description;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getParentId()
      {
         return parentId;
      }

      public void setParentId(String parentId)
      {
         this.parentId = parentId;
      }

      public String getLabel()
      {
         return label;
      }

      public void setLabel(String label)
      {
         this.label = label;
      }
   }

   static public class User
   {
      private String userName;

      private String password;

      private String firstName;

      private String lastName;

      private String email;

      private String displayName;

      private String groups;
      
      private boolean enabled = true;

      public User()
      {
      }

      public String getEmail()
      {
         return email;
      }

      public void setEmail(String email)
      {
         this.email = email;
      }

      public String getDisplayName()
      {
         return displayName;
      }

      public void setDisplayName(String displayName)
      {
         this.displayName = displayName;
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

      public String getPassword()
      {
         return password;
      }

      public void setPassword(String password)
      {
         this.password = password;
      }

      public String getGroups()
      {
         return groups;
      }

      public void setGroups(String groups)
      {
         this.groups = groups;
      }

      public String getUserName()
      {
         return userName;
      }

      public void setUserName(String userName)
      {
         this.userName = userName;
      }
      
      public boolean isEnabled() 
      {
          return enabled;
      }
      
      public void setEnabled(boolean b) 
      {
          enabled = b;
      }
   }
}
