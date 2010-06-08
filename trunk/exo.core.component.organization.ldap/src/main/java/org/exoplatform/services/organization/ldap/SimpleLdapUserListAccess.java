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
package org.exoplatform.services.organization.ldap;

import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SimpleLdapUserListAccess extends LdapUserListAccess
{

   private int size = -1;

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.SimpleLdapUserListAccess");

   /**
    * @param ldapAttrMapping LDAP attribute to organization service essences 
    * @param ldapService LDAP service
    * @param searchBase base search DN
    * @param filter search filter
    */
   public SimpleLdapUserListAccess(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, String searchBase,
      String filter)
   {
      super(ldapAttrMapping, ldapService, searchBase, filter);
   }

   /**
    * {@inheritDoc}
    */
   protected User[] load(LdapContext ctx, int index, int length) throws Exception
   {
      User[] users = new User[length];
      NamingEnumeration<SearchResult> results = null;

      try
      {
         SortControl sctl = new SortControl(new String[]{ldapAttrMapping.userUsernameAttr}, Control.NONCRITICAL);
         ctx.setRequestControls(new Control[]{sctl});

         SearchControls constraints = new SearchControls();
         constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

         results = ctx.search(searchBase, filter, constraints);

         for (int p = 0, counter = 0; results.hasMoreElements() && counter < length; p++)
         {
            SearchResult result = results.next();

            if (p >= index)
            { // start point for getting results
               User user = ldapAttrMapping.attributesToUser(result.getAttributes());
               users[counter++] = user;
            }

         }

      }
      finally
      {
         if (results != null)
            results.close();
      }

      if (LOG.isDebugEnabled())
         LOG.debug("range of users from " + index + " to " + (index + length));
      return users;
   }

   /**
    * {@inheritDoc}
    */
   protected int getSize(LdapContext ctx) throws Exception
   {
      if (size < 0)
      {
         NamingEnumeration<SearchResult> results = null;

         try
         {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search(searchBase, filter, constraints);
            size = 0;
            while (results.hasMoreElements())
            {
               results.next();
               size++;
            }

         }
         finally
         {
            if (results != null)
               results.close();
         }
      }
      if (LOG.isDebugEnabled())
         LOG.debug("size : " + size);
      return size;
   }

}
