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

import java.util.Date;

/**
 * Created by The eXo Platform SAS . Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Jun 14, 2003 Time: 1:12:22 PM
 */
public class Query
{

   private String userName_;

   private String fname_;

   private String lname_;

   private String email_;

   private Date from_;

   private Date to_;

   public Query()
   {
   }

   public String getUserName()
   {
      return userName_;
   }

   public void setUserName(String s)
   {
      userName_ = s;
   }

   public String getFirstName()
   {
      return fname_;
   }

   public void setFirstName(String s)
   {
      fname_ = s;
   }

   public String getLastName()
   {
      return lname_;
   }

   public void setLastName(String s)
   {
      lname_ = s;
   }

   public String getEmail()
   {
      return email_;
   }

   public void setEmail(String s)
   {
      email_ = s;
   }

   public Date getFromLoginDate()
   {
      return from_;
   }

   public void setFromLoginDate(Date d)
   {
      from_ = d;
   }

   public Date getToLoginDate()
   {
      return to_;
   }

   public void setToLoginDate(Date d)
   {
      to_ = d;
   }
}
