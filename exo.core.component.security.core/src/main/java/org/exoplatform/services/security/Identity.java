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
package org.exoplatform.services.security;

import org.exoplatform.commons.utils.secure.SecureCollections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

/**
 * Created by The eXo Platform SAS .<br/>
 * User Session encapsulates user's principals such as name, groups along with
 * JAAS subject (useful in J2EE environment) as well as other optional
 * attributes
 *
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class Identity
{

   /**
    * User's identifier.
    */
   private String userId;

   /**
    * Memberships.
    */
   private final Set<MembershipEntry> memberships;

   /**
    * javax.security.auth.Subject can be used for logout process. <code>
    * LoginContext ctx = new LoginContext("exo-domain", subject);
    * ctx.logout();
    * </code>
    */
   private Subject subject;

   /**
    * User's roles.
    */
   private final Set<String> roles;

   /**
    * @param userId the iser's identifier.
    */
   public Identity(String userId)
   {
      this(userId, new HashSet<MembershipEntry>(), new HashSet<String>());
   }

   /**
    * @param userId the user's identifier.
    * @param memberships the memberships.
    */
   public Identity(String userId, Collection<MembershipEntry> memberships)
   {
      this(userId, memberships, new HashSet<String>());
   }

   /**
    * @param userId the user's identifier.
    * @param memberships the memberships.
    * @param roles the user's roles.
    */
   public Identity(String userId, Collection<MembershipEntry> memberships, Collection<String> roles)
   {
      this.userId = userId;
      this.memberships =
         SecureCollections.secureSet(new HashSet<MembershipEntry>(memberships),
            PermissionConstants.MODIFY_IDENTITY_PERMISSION);
      this.roles =
         SecureCollections.secureSet(new HashSet<String>(roles), PermissionConstants.MODIFY_IDENTITY_PERMISSION);;
   }

   /**
    * @return user name.
    */
   public String getUserId()
   {
      return userId;
   }

   /**
    * @param group the group.
    * @param membershipType the MembershipType.
    * @return true if user has given membershipType for given group, false
    *         otherwise
    */
   public boolean isMemberOf(String group, String membershipType)
   {
      return containsMembership(new MembershipEntry(group, membershipType));
   }

   /**
    * @param me the MembershipEntry.
    * @return true if user has given MembershipEntry, false otherwise.
    */
   public boolean isMemberOf(MembershipEntry me)
   {
      return containsMembership(me);
   }

   /**
    * Check is user member of group.
    *
    * @param group the group.
    * @return true if user has any membershipType for given group, false
    *         otherwise.
    */
   public boolean isMemberOf(String group)
   {
      return containsMembership(new MembershipEntry(group));
   }

   /**
    * @return set of groups to which this user belongs to.
    */
   public Set<String> getGroups()
   {
      // TODO : Need to protect group's set ??
      Set<String> groups = new HashSet<String>();
      for (MembershipEntry m : memberships)
      {
         groups.add(m.getGroup());
      }
      return groups;
   }

   /**
    * @deprecated for back compatibility.
    */
   @Deprecated
   public void setMemberships(Collection<MembershipEntry> memberships)
   {
      this.memberships.clear();
      this.memberships.addAll(memberships);
   }

   /**
    * @return user's memberships.
    */
   public Collection<MembershipEntry> getMemberships()
   {
      return memberships;
   }

   /**
    * Sets the roles for J2EE environment using.
    *
    * @param roles the roles.
    */
   public void setRoles(Collection<String> roles)
   {
      this.roles.clear();
      this.roles.addAll(roles);
   }

   /**
    * @return roles users'roles.
    */
   public Collection<String> getRoles()
   {
      return roles;
   }

   /**
    * @return @see {@link Subject} .
    */
   public Subject getSubject()
   {
      return subject;
   }

   /**
    * @param subject @see {@link Subject} .
    */
   public void setSubject(Subject subject)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(PermissionConstants.SET_SUBJECT_PERMISSION);
      }
      this.subject = subject;
   }

   /**
    * Check is given {@link MembershipEntry} presents in user's memberships.
    *
    * @param checkMe the MembershipEntry.
    * @return true if presents false otherwise.
    */
   private boolean containsMembership(MembershipEntry checkMe)
   {
      return memberships.contains(checkMe);
   }
}