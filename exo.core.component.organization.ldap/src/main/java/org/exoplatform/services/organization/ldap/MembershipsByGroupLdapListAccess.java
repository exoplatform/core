/*
 * Copyright (C) 2011 eXo Platform SAS.
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
import org.exoplatform.services.organization.Membership;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id: MembershipByGroupListAccess.java 34360 2009-07-22 23:58:59Z tolusha $
 */
public class MembershipsByGroupLdapListAccess extends LdapListAccess<Membership>
{

   protected final String groupId;

   protected final LDAPAttributeMapping ldapAttrMapping;

   protected final MembershipDAOImpl dao;

   /**
    * Constructor MembershipByGroupLdapListAccess.
    */
   public MembershipsByGroupLdapListAccess(LDAPService ldapService, MembershipDAOImpl dao,
      LDAPAttributeMapping ldapAttrMapping, String groupId)
   {
      super(ldapService);

      this.groupId = groupId;
      this.ldapAttrMapping = ldapAttrMapping;
      this.dao = dao;
   }

   /**
    * {@inheritDoc}
    */
   protected Membership[] load(LdapContext ctx, int index, int length) throws Exception
   {
      if (index < 0)
      {
         throw new IllegalArgumentException("Illegal index: index must be a positive number");
      }

      if (length < 0)
      {
         throw new IllegalArgumentException("Illegal length: length must be a positive number");
      }
      
      Membership[] memberships = new Membership[length];

      if (length == 0)
         return memberships;
      int counter = 0;
      int p = 0;

      for (int err = 0;; err++)
      {
         String groupDN = dao.getGroupDNFromGroupId(groupId);

         SearchControls constraints = new SearchControls();
         constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

         NamingEnumeration<SearchResult> results =
            ctx.search(groupDN, ldapAttrMapping.membershipObjectClassFilter, constraints);
         try
         {
            while (results.hasMoreElements())
            {
               SearchResult sr = results.next();
               String membershipType = dao.explodeDN(sr.getNameInNamespace(), true)[0];
               Attributes attrs = sr.getAttributes();
               Attribute attr = attrs.get(ldapAttrMapping.membershipTypeMemberValue);
               if (attr == null)
                  continue;

               for (int i = 0; i < attr.size(); i++)
               {
                  if (p >= index)
                  {
                     String userDN = String.valueOf(attr.get(i));
                     String userName;
                     if (ldapAttrMapping.userDNKey.equals(ldapAttrMapping.userUsernameAttr))
                     {
                        userName = dao.explodeDN(userDN, true)[0];
                     }
                     else
                     {
                        userName = dao.findUserByDN(ctx, userDN).getUserName();
                     }
                     Membership membership = dao.createMembershipObject(userName, groupId, membershipType);

                     memberships[counter++] = membership;

                     if (counter == length)
                     {
                        return memberships;
                     }
                  }

                  p++;
               }
            }

            if (counter < length)
            {
               throw new IllegalArgumentException(
                  "Illegal index or length: sum of the index and the length cannot be greater than the list size");
            }
         }
         catch (NamingException e)
         {
            ctx = dao.reloadCtx(ctx, err, e);
         }
         finally
         {
            results.close();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   protected int getSize(LdapContext ctx) throws Exception
   {
      int size = 0;

      String groupDN = dao.getGroupDNFromGroupId(groupId);

      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

      NamingEnumeration<SearchResult> results =
         ctx.search(groupDN, ldapAttrMapping.membershipObjectClassFilter, constraints);
      try
      {
         while (results.hasMoreElements())
         {
            Attributes attrs = results.next().getAttributes();
            Attribute attr = attrs.get(ldapAttrMapping.membershipTypeMemberValue);
            if (attr == null)
               continue;

            size += attr.size();
         }
      }
      finally
      {
         results.close();
      }

      return size;
   }
}
