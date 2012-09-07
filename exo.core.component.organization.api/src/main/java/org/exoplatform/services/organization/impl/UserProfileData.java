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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

import org.exoplatform.commons.utils.SecurityHelper;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import java.security.PrivilegedAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by The eXo Platform SAS .
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Jun 14, 2003 Time: 1:12:22 PM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "EXO_USER_PROFILE")
public class UserProfileData
{
   static transient private XStream xstream_;

   @Id
   private String userName;

   @Column(length = 65536)
   @Type(type = "org.exoplatform.services.database.impl.TextClobType")
   private String profile;

   public UserProfileData()
   {
   }

   public UserProfileData(String userName)
   {
      StringBuffer b = new StringBuffer();
      b.append("<user-profile>\n").append("  <userName>").append(userName).append("</userName>\n");
      b.append("</user-profile>\n");
      this.userName = userName;
      this.profile = b.toString();
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String s)
   {
      this.userName = s;
   }

   public String getProfile()
   {
      return profile;
   }

   public void setProfile(String s)
   {
      profile = s;
   }

   public org.exoplatform.services.organization.UserProfile getUserProfile()
   {
      final XStream xstream = getXStream();
      UserProfileImpl up = SecurityHelper.doPrivilegedAction(new PrivilegedAction<UserProfileImpl>()
      {
         public UserProfileImpl run()
         {
            return (UserProfileImpl)xstream.fromXML(profile);
         }
      });
      return up;
   }

   public void setUserProfile(org.exoplatform.services.organization.UserProfile up)
   {
      if (up == null)
      {
         profile = "";
         return;
      }
      final UserProfileImpl impl = (UserProfileImpl)up;
      userName = up.getUserName();
      final XStream xstream = getXStream();
      profile = SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return xstream.toXML(impl);
         }
      });
   }

   static private XStream getXStream()
   {
      if (xstream_ == null)
      {
         xstream_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<XStream>()
         {
            public XStream run()
            {
               return new XStream(new XppDriver());
            }
         });
         xstream_.alias("user-profile", UserProfileImpl.class);
      }
      return xstream_;
   }
}
