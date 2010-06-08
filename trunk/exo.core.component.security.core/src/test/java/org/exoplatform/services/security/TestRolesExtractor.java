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

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.security.impl.DefaultRolesExtractorImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:alexey.zavizionov@exoplatform.com.ua">Alexey
 *         Zavizionov</a>
 * @version $Id: $ Mar 27, 2008
 */
public class TestRolesExtractor extends TestCase
{

   //  private static Log       log = ExoLogger.getLogger("exo.core.component.security.core.TestRolesExtractor");

   protected RolesExtractor rolesExtractor;

   public TestRolesExtractor(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      if (rolesExtractor == null)
      {
         String containerConf =
            TestRolesExtractor.class.getResource("/conf/standalone/test-configuration.xml").toString();
         StandaloneContainer.addConfigurationURL(containerConf);

         StandaloneContainer manager = StandaloneContainer.getInstance();

         rolesExtractor = (DefaultRolesExtractorImpl)manager.getComponentInstanceOfType(RolesExtractor.class);
         assertNotNull(rolesExtractor);
      }
   }

   public void testExtractRoles1() throws Exception
   {

      Set<MembershipEntry> groups = getGroups1();

      Set<String> extractRoles = rolesExtractor.extractRoles("exo", groups);
      assertNotNull(extractRoles);
      assertFalse(extractRoles.isEmpty());
      assertEquals(2, extractRoles.size());
      assertTrue(extractRoles.contains("admin"));
      assertTrue(extractRoles.contains("exo"));

      ((DefaultRolesExtractorImpl)rolesExtractor).setUserRoleParentGroup("platform");
   }

   public void testExtractRoles2() throws Exception
   {

      Set<MembershipEntry> groups = getGroups2();

      ((DefaultRolesExtractorImpl)rolesExtractor).setUserRoleParentGroup("platform");
      Set<String> extractRoles = rolesExtractor.extractRoles("exo", groups);
      assertNotNull(extractRoles);
      assertFalse(extractRoles.isEmpty());
      assertEquals(5, extractRoles.size());
      assertTrue(extractRoles.contains("organization"));
      assertTrue(extractRoles.contains("administrators"));
      assertTrue(extractRoles.contains("users"));

   }

   /**
    * @return set of groups to which this user belongs to
    */
   private Set<MembershipEntry> getGroups1()
   {
      Set<MembershipEntry> groups = new HashSet<MembershipEntry>();
      groups.add(new MembershipEntry("/admin"));
      groups.add(new MembershipEntry("/exo"));
      return groups;
   }

   /**
    * @return set of groups to which this user belongs to
    */
   private Set<MembershipEntry> getGroups2()
   {
      Set<MembershipEntry> groups = new HashSet<MembershipEntry>();
      groups.add(new MembershipEntry("/admin"));
      groups.add(new MembershipEntry("/exo"));
      groups.add(new MembershipEntry("/organization/management/executive-board"));
      groups.add(new MembershipEntry("/platform/administrators"));
      groups.add(new MembershipEntry("/platform/users"));
      return groups;
   }

}
