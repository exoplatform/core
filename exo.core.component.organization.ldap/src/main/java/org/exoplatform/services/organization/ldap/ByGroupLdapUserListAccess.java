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
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ByGroupLdapUserListAccess extends LdapListAccess<User>
{

   /**
    * Base search DN.
    */
   protected final String searchBase;

   /**
    * Search filter.
    */
   protected final String filter;

   /**
    * LDAP attribute to organization service essences.
    */
   protected final LDAPAttributeMapping ldapAttrMapping;

   /**
    * Indicates the expected status of the user
    */
   protected final UserStatus status;

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.ByGroupLdapUserListAccess");

   /**
    * List's size.
    */
   private int size = -1;

   /**
    * @param ldapAttrMapping LDAP attribute to organization service essences 
    * @param ldapService LDAP service
    * @param searchBase base search DN
    * @param filter search filter
    */
   public ByGroupLdapUserListAccess(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, String searchBase,
      String filter, UserStatus status)
   {
      super(ldapService);
      this.ldapAttrMapping = ldapAttrMapping;
      this.searchBase = searchBase;
      this.filter = filter;
      this.status = status;
   }

   /**
    * {@inheritDoc}
    */
   protected User[] load(LdapContext ctx, int index, int length) throws Exception
   {

      User[] users = new User[length];

      if (length == 0)
         return users;
      NamingEnumeration<SearchResult> results = null;
      try
      {
         SearchControls constraints = new SearchControls();
         constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

         // get all memberships for group
         results = ctx.search(searchBase, filter, constraints);

         int counter = 0;
         int p = 0;
         // Retrieval before get requested range.
         // Range can be filed from one or few memberships
         while (counter < length)
         {
            if (!results.hasMoreElements())
            {
               throw new IllegalArgumentException(
                  "Illegal index or length: sum of the index and the length cannot be greater than the list size");
            }

            SearchResult result = results.next();
            Attributes attrs = result.getAttributes();

            if (attrs.size() == 0)
               continue; // object has not attributes at all, must never be true
            Attribute attr = attrs.get(ldapAttrMapping.membershipTypeMemberValue);
            if (attr == null || attr.size() == 0)
               continue; // object has not any attribute 'member', must never be true

            NamingEnumeration<?> members = attr.getAll();

            try
            {
               // again check range if one membership contains enough attributes
               while (members.hasMoreElements() && counter < length)
               {
                  String member = (String)members.next();

                  if (p >= index)
                  { // start point for getting users
                     Attributes uattr = ctx.getAttributes(member);
                     User user = ldapAttrMapping.attributesToUser(uattr);
                     if (user != null && status.matches(user.isEnabled()))
                     {
                        user.setFullName(user.getFirstName() + " " + user.getLastName());
                        users[counter++] = user;
                     }
                  }

                  p++;
               }

            }
            finally
            {
               if (members != null)
                  members.close();
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
         size = 0;
         NamingEnumeration<SearchResult> results = null;
         try
         {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

            // get all memberships for group
            results = ctx.search(searchBase, filter, constraints);

            while (results.hasMoreElements())
            {

               SearchResult result = results.next();
               Attributes attrs = result.getAttributes();

               if (attrs.size() == 0)
                  continue; // object has not attributes at all, must never be true
               Attribute attr = attrs.get(ldapAttrMapping.membershipTypeMemberValue);
               if (attr == null || attr.size() == 0)
                  continue; // object has not any attribute 'member', must never be true

               // retrieval all 'member' attribute
               NamingEnumeration<?> members = attr.getAll();

               try
               {
                  String[] attrIds;
                  if (ldapAttrMapping.hasUserAccountControl())
                  {
                     attrIds = new String[]{ldapAttrMapping.userAccountControlAttr};
                  }
                  else
                  {
                     attrIds = new String[]{};
                  }
                  while (members.hasMoreElements())
                  {
                     String member = (String)members.next();
                     if (status != UserStatus.BOTH)
                     {
                        Attributes atts =
                           ctx.getAttributes(member, attrIds);
                        try
                        {
                           if (status.matches(ldapAttrMapping.isEnabled(member, atts)))
                           {
                              size++;
                           }
                        }
                        catch (DisabledUserException e)
                        {
                           if (LOG.isDebugEnabled())
                              LOG.debug("Could not know if the member '" + member + "' is disabled or not.", e);
                        }
                        continue;
                     }
                     size++;
                  }

               }
               finally
               {
                  if (members != null)
                     members.close();
               }
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
