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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class TestIdentity extends TestCase
{

   Identity identity;

   Collection<MembershipEntry> memberships;

   protected void setUp()
   {
      // common setup for testIsInGroup and testHasMembership

      memberships = new ArrayList<MembershipEntry>();
      memberships.add(new MembershipEntry("/group1", "member"));
      memberships.add(new MembershipEntry("/group1", "*"));
      memberships.add(new MembershipEntry("/group2", "member"));
      memberships.add(new MembershipEntry("/group3", "member"));
      memberships.add(new MembershipEntry("/group3", "validator"));
      memberships.add(new MembershipEntry("/group3", "*"));
      memberships.add(new MembershipEntry("/group4", "member"));
      memberships.add(new MembershipEntry("/group4", "*"));
      memberships.add(new MembershipEntry("/group4", "validator"));
      memberships.add(new MembershipEntry("/group5", "*"));
      memberships.add(new MembershipEntry("/group5", "member"));
      memberships.add(new MembershipEntry("/group5", "validator"));

      identity = new Identity("user", memberships);
   }

   public void testIsInGroup()
   {
      assertTrue("user in group /group1", identity.isMemberOf("/group1"));
      assertTrue("user in group /group2", identity.isMemberOf("/group2"));
      assertFalse("user in group /non/existing/group", identity.isMemberOf("non/existing/group"));
   }

   public void testHasMembership()
   {
      assertTrue("membership * in group /group1", identity.isMemberOf("/group1", "*"));
      assertTrue("membership manager in group /group1", identity.isMemberOf("/group1", "manager"));
      assertTrue("membership member in group /group1", identity.isMemberOf("/group1", "member"));
      assertTrue("membership member in group /group2", identity.isMemberOf("/group2", "member"));
      assertTrue("membership * in group /group1", identity.isMemberOf("/group2", "*"));
      assertTrue("membership member in group /group3", identity.isMemberOf("/group3", "member"));
      assertTrue("membership validator in group /group3", identity.isMemberOf("/group3", "validator"));
      assertTrue("membership manager in group /group3", identity.isMemberOf("/group3", "manager"));
      assertTrue("membership * in group /group3", identity.isMemberOf("/group3", "*"));
      assertTrue("membership member in group /group4", identity.isMemberOf("/group4", "member"));
      assertTrue("membership validator in group /group4", identity.isMemberOf("/group4", "validator"));
      assertTrue("membership manager in group /group4", identity.isMemberOf("/group4", "manager"));
      assertTrue("membership * in group /group4", identity.isMemberOf("/group4", "*"));
      assertTrue("membership member in group /group5", identity.isMemberOf("/group5", "member"));
      assertTrue("membership validator in group /group5", identity.isMemberOf("/group5", "validator"));
      assertTrue("membership manager in group /group5", identity.isMemberOf("/group5", "manager"));
      assertTrue("membership * in group /group5", identity.isMemberOf("/group5", "*"));
      // any membership of /group1
      assertFalse("membership null in group group1", identity.isMemberOf("group1", null));
      assertFalse("membership null in group group1", identity.isMemberOf("group1"));
   }
}
