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

import org.exoplatform.services.organization.UserProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Aug 21, 2003 Time: 3:22:54 PM
 */
public class UserProfileImpl implements UserProfile
{
   private String userName;

   private Map<String, String> attributes;

   public UserProfileImpl()
   {
      attributes = new HashMap<String, String>();
   }

   public UserProfileImpl(String userName)
   {
      this.userName = userName;
      attributes = new HashMap<String, String>();
   }

   public UserProfileImpl(String userName, Map<String, String> map)
   {
      this.userName = userName;
      attributes = map;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String s)
   {
      userName = s;
   }

   public Map<String, String> getUserInfoMap()
   {
      if (attributes == null)
         attributes = new HashMap<String, String>();
      return this.attributes;
   }

   public void setUserInfoMap(Map<String, String> map)
   {
      this.attributes = map;
   }

   public String getAttribute(String attName)
   {
      return attributes.get(attName);
   }

   public void setAttribute(String key, String value)
   {
      attributes.put(key, value);
   }

   public Map getAttributeMap()
   {
      return attributes;
   }
}
