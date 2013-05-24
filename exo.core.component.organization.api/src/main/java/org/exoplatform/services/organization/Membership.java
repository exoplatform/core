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

/**
 * Created by The eXo Platform SAS <br>
 * This is the interface for the membership data model.
 * @author <a href="mailto:benjmestrallet@users.sourceforge.net">Mestrallet Benjamin</a>
 * @LevelAPI Platform
 */
public interface Membership
{
   /**
    * the type of Membership allows distinction between 'hierarchical' and
    * 'supportive' Memberships.
    * @return the name of the MembershipType
    */
   public String getMembershipType();

   /**
    * @param type the name of the MembershipType.
    */
   public void setMembershipType(String type);

   /**
    * @return the id of the MembershipType.
    */
   public String getId();

   /**
    * @return the id of the Group.
    */
   public String getGroupId();

   /**
    * @return the name of the user.
    */
   public String getUserName();
}
