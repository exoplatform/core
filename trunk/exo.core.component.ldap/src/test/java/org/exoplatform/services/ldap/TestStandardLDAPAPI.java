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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.net.NetService;
import org.exoplatform.test.BasicTestCase;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;

/**
 * Created by The eXo Platform SAS Author : Nhu Thuan thuannd@exoplatform.com
 * Oct 3, 2005
 */
public class TestStandardLDAPAPI extends BasicTestCase
{
   final static public String ROOT_DN = "cn=Manager,dc=exoplatform,dc=org";

   final static public String ROOT_PASSWORD = "secret";

   final static public String DEVELOPER_UNIT_DN = "ou=developer,o=company,c=vietnam,dc=exoplatform,dc=org";

   final static public String EXO_DEVELOPER_DN = "cn=exo, " + DEVELOPER_UNIT_DN;

   static private String LDAP_HOST = "127.0.0.1";

   static private int LDAP_PORT = 389;

   private NetService nservice_;

   public TestStandardLDAPAPI(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      nservice_ = (NetService)manager.getComponentInstanceOfType(NetService.class);
   }

   public void testLDAPService() throws Exception
   {
      if (nservice_.ping(LDAP_HOST, LDAP_PORT) < 0)
      {
         System.out.println("===LDAP Server is not started on host:" + LDAP_HOST + " port:" + LDAP_PORT);
         return;
      }
      // --------------------------------------------------
      // Set up the environment for creating the initial context
      // --------------------------------------------------
      Hashtable<String, String> props = new Hashtable<String, String>();
      props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      props.put(Context.PROVIDER_URL, "ldap://" + LDAP_HOST + ":" + LDAP_PORT);
      props.put(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");

      props.put(Context.REFERRAL, "ignore");
      props.put(Context.SECURITY_AUTHENTICATION, "simple");

      // --------------------------------------------------
      // specify the root username
      // --------------------------------------------------
      props.put(Context.SECURITY_PRINCIPAL, ROOT_DN);

      // --------------------------------------------------
      // specify the root password
      // --------------------------------------------------
      props.put(Context.SECURITY_CREDENTIALS, ROOT_PASSWORD);

      // --------------------------------------------------
      // Get the environment properties (props) for creating initial
      // context and specifying LDAP service provider parameters.
      // --------------------------------------------------
      LdapContext ctx = new InitialLdapContext(props, null);
      // Create attributes to be associated with the new context
      System.out.println("##############################CREATE##############################");
      // Create the context
      for (int i = 0; i < 10; i++)
      {
         String cn = "exo" + i;
         String sn = "sn" + i;
         String dn = "cn=" + cn + ", " + DEVELOPER_UNIT_DN;
         Attributes attrs = new BasicAttributes(true); // case-ignore
         attrs.put(new BasicAttribute("objectClass", "person"));
         attrs.put(new BasicAttribute("cn", cn));
         attrs.put(new BasicAttribute("sn", sn));
         attrs.put(new BasicAttribute("telephonenumber", "0989654990"));
         ctx.createSubcontext(dn, attrs);
         System.out.println(ctx.getAttributes(dn));
      }

      // Search for objects that have those matching attributes
      System.out.println("#################SEARCH BY ATTRIBUTES#############################");
      Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name
      // case
      matchAttrs.put(new BasicAttribute("sn", "sn1"));
      matchAttrs.put(new BasicAttribute("telephonenumber", "0989654990"));
      NamingEnumeration results = ctx.search(DEVELOPER_UNIT_DN, matchAttrs);
      while (results.hasMore())
      {
         SearchResult sr = (SearchResult)results.next();
         printAttributes(sr.getName(), sr.getAttributes());
      }
      System.out.println("#################SEARCH BY QUERY FILTER##########################");
      SearchControls searchControls = new SearchControls();
      searchControls.setCountLimit(5);
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      Control[] requestControls =
         {new PagedResultsControl(3, Control.CRITICAL), new SortControl(new String[]{"cn"}, Control.NONCRITICAL)};
      ctx.setRequestControls(requestControls);
      results = ctx.search(DEVELOPER_UNIT_DN, "(objectclass=person)", searchControls);
      while (results.hasMoreElements())
      {
         SearchResult sr = (SearchResult)results.nextElement();
         printAttributes(sr.getName(), sr.getAttributes());
         if (sr instanceof HasControls)
         {
            Control[] controls = ((HasControls)sr).getControls();
            if (controls != null)
            {
               System.out.println("====================>response control is not null");
               for (int i = 0; i < controls.length; i++)
               {
                  if (controls[i] instanceof PagedResultsResponseControl)
                  {
                     PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[i];
                     System.out.println("page result size: " + prrc.getResultSize());
                     System.out.println("cookie: " + prrc.getCookie());
                  }
                  else
                  {
                     // Handle other response controls (if any)
                  }
               }
            }
         }
      }

      Control[] controls = ctx.getResponseControls();
      if (controls != null)
      {
         System.out.println("====================>response control is not null");
         for (int i = 0; i < controls.length; i++)
         {
            if (controls[i] instanceof PagedResultsResponseControl)
            {
               PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[i];
               System.out.println("page result size: " + prrc.getResultSize());
               System.out.println("cookie: " + prrc.getCookie());
            }
            else
            {
               // Handle other response controls (if any)
            }
         }
      }
      ctx.setRequestControls(null);
      System.out.println("##############################REMOVE##############################");
      for (int i = 0; i < 10; i++)
      {
         String cn = "exo" + i;
         String dn = "cn=" + cn + ", " + DEVELOPER_UNIT_DN;
         ctx.unbind(dn);
         System.out.println("remove " + dn + " successfully");
      }
   }

   private void printAttributes(String entry, Attributes attrs)
   {
      System.out.println("entry: " + entry);
      System.out.println("    " + attrs);
   }
}
