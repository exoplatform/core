/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.organization.cache;

import org.exoplatform.services.organization.Membership;

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 2009
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a> 
 * @version $Id$
 */
/**
 * Will be used as key for cacheMembership.
 */
public class MembershipCacheKey implements Serializable
{
   /**
    * serialVersionUID.
    */
   private static final long serialVersionUID = -188435911917156440L;

   private final String userName;

   private final String groupId;

   private final String type;

   private final int hashcode;

   public MembershipCacheKey(String userName, String groupId, String type)
   {
      this.userName = userName;
      this.groupId = groupId;
      this.type = type;

      final int prime = 31;
      int result = 1;
      result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      hashcode = prime * result + ((userName == null) ? 0 : userName.hashCode());
   }

   public MembershipCacheKey(Membership m)
   {
      this.userName = m.getUserName();
      this.groupId = m.getGroupId();
      this.type = m.getMembershipType();

      final int prime = 31;
      int result = 1;
      result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      hashcode = prime * result + ((userName == null) ? 0 : userName.hashCode());
   }

   @Override
   public int hashCode()
   {
      return hashcode;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MembershipCacheKey other = (MembershipCacheKey)obj;
      if (groupId == null)
      {
         if (other.groupId != null)
            return false;
      }
      else if (!groupId.equals(other.groupId))
         return false;
      if (type == null)
      {
         if (other.type != null)
            return false;
      }
      else if (!type.equals(other.type))
         return false;
      if (userName == null)
      {
         if (other.userName != null)
            return false;
      }
      else if (!userName.equals(other.userName))
         return false;
      return true;
   }

}