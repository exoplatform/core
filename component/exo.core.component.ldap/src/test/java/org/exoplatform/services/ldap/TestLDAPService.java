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
package org.exoplatform.services.ldap;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.net.NetService;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Nhu Thuan thuannd@exoplatform.com
 * Oct 3, 2005
 */
public class TestLDAPService extends TestCase
{
   final static public String ROOT_DN = "cn=Manager,dc=exoplatform,dc=org";

   final static public String ROOT_PASSWORD = "secret";

   final static public String DEVELOPER_UNIT_DN = "ou=developer,o=company,c=vietnam,dc=exoplatform,dc=org";

   final static public String EXO_DEVELOPER_DN = "cn=exo, " + DEVELOPER_UNIT_DN;

   // static private String LDAP_HOST = "192.168.0.10" ;
   static private String LDAP_HOST = "localhost";

   static private int LDAP_PORT = 389;

   private LDAPService service_;

   private NetService nservice_;

   private boolean test = true;

   public void setUp() throws Exception
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      nservice_ = (NetService)pcontainer.getComponentInstanceOfType(NetService.class);
      if (nservice_.ping(LDAP_HOST, LDAP_PORT) < 0)
      {
         test = false;
         return;
      }
      service_ = (LDAPService)pcontainer.getComponentInstanceOfType(LDAPService.class);

   }

   public void testCreate() throws Exception
   {
      if (!test)
      {
         System.out.println("===LDAP Server is not started on host:" + LDAP_HOST + " port:" + LDAP_PORT);
         return;
      }
      LdapContext ctx = service_.getLdapContext();

      String BASE = "dc=exoplatform,dc=org";

      Attributes attrs = new BasicAttributes(true); // case-ignore
      Attribute objclass = new BasicAttribute("objectClass");
      objclass.add("top");
      objclass.add("organizationalUnit");
      attrs.put(objclass);
      // attrs.put(new BasicAttribute("ou", "clients"));
      ctx.createSubcontext("ou=dummy," + BASE, attrs);

      attrs = new BasicAttributes(true); // case-ignore
      objclass = new BasicAttribute("objectClass");
      objclass.add("top");
      objclass.add("organization");
      attrs.put(objclass);
      ctx.createSubcontext("o=company.com,ou=dummy," + BASE, attrs);

      attrs = new BasicAttributes(true); // case-ignore
      objclass = new BasicAttribute("objectClass");
      objclass.add("top");
      objclass.add("organizationalUnit");
      attrs.put(objclass);
      ctx.createSubcontext("ou=users,o=company.com,ou=dummy," + BASE, attrs);

      attrs = new BasicAttributes(true); // case-ignore
      objclass = new BasicAttribute("objectClass");
      objclass.add("inetOrgPerson");
      objclass.add("organizationalPerson");
      objclass.add("person");
      objclass.add("top");
      attrs.put(objclass);
      attrs.put("cn", "g");
      attrs.put("sn", "a");
      ctx.createSubcontext("uid=gena,ou=users,o=company.com,ou=dummy," + BASE, attrs);
      ctx.destroySubcontext("uid=gena,ou=users,o=company.com,ou=dummy,dc=exoplatform,dc=org");
      ctx.destroySubcontext("ou=users,o=company.com,ou=dummy,dc=exoplatform,dc=org");
      ctx.destroySubcontext("o=company.com,ou=dummy,dc=exoplatform,dc=org");
      ctx.destroySubcontext("ou=dummy,dc=exoplatform,dc=org");
   }

}
