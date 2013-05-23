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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Thuannd nhudinhthuan@yahoo.com Feb
 * 22, 2006. @version andrew00x $
 */
public class ADSearchBySID
{

   /**
    * Logger. 
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.ADSearchBySID");

   /**
    * Mapping LDAP attributes to eXo organization service items.
    */
   protected LDAPAttributeMapping ldapAttrMapping;

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items
    */
   public ADSearchBySID(LDAPAttributeMapping ldapAttrMapping)
   {
      this.ldapAttrMapping = ldapAttrMapping;
   }

   public String findMembershipDNBySID(LdapContext ctx, byte[] sid, String baseDN, String scopedRole)
      throws NamingException
   {
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
      constraints.setReturningAttributes(new String[]{""});
      constraints.setDerefLinkFlag(true);

      NamingEnumeration<SearchResult> answer = null;
      try
      {
         if (scopedRole == null)
         {
            answer = ctx.search(baseDN, "objectSid={0}", new Object[]{sid}, constraints);
         }
         else
         {
            answer =
               ctx.search(baseDN, "(& (objectSid={0}) (" + ldapAttrMapping.membershipTypeRoleNameAttr + "={1}))",
                  new Object[]{sid, scopedRole}, constraints);
         }
         while (answer.hasMoreElements())
         {
            SearchResult sr = answer.next();
            NameParser parser = ctx.getNameParser("");
            Name entryName = parser.parse(new CompositeName(sr.getName()).get(0));
            return entryName + "," + baseDN;
         }
         return null;
      }
      catch (NameNotFoundException e)
      {
         if (LOG.isDebugEnabled())
            LOG.debug(e.getLocalizedMessage(), e);
         return null;
      }
      finally
      {
         if (answer != null)
            answer.close();
      }
   }

}
