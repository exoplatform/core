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
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.services.organization.ldap.CacheHandler.CacheType;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : James Chamberlain.
 * james.chamberlain@gmail.com Feb 22, 2006
 */
public class ADMembershipDAOImpl extends MembershipDAOImpl
{

   private ADSearchBySID adSearch;

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items
    * @param ldapService {@link LDAPService}
    * @param ad See {@link ADSearchBySID}
    * @param cacheHandler
    *          The Cache Handler 
    * @throws Exception if any errors occurs
    */
   public ADMembershipDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, ADSearchBySID ad,
      CacheHandler cacheHandler) throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
      adSearch = ad;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {
      MembershipImpl membership =
         (MembershipImpl)cacheHandler.get(cacheHandler.getMembershipKey(userName, groupId, type), CacheType.MEMBERSHIP);
      if (membership != null)
      {
         return membership;
      }

      LdapContext ctx = ldapService.getLdapContext(true);
      String groupDN = getGroupDNFromGroupId(groupId);
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               Collection memberships = findMemberships(ctx, userName, groupDN, type);
               if (memberships.size() > 0)
               {
                  membership = (MembershipImpl)memberships.iterator().next();
                  cacheHandler.put(cacheHandler.getMembershipKey(membership), membership, CacheType.MEMBERSHIP);
                  return membership;
               }
               return null;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public Collection findMembershipsByUser(String userName) throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext(true);
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               return findMemberships(ctx, userName, ldapAttrMapping.groupsURL, null);
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      String groupDN = getGroupDNFromGroupId(groupId);
      LdapContext ctx = ldapService.getLdapContext(true);
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               return findMemberships(ctx, userName, groupDN, null);
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * @param ctx {@link LdapContext}
    * @param userName user name
    * @param groupId group id
    * @param type membership type
    * @return collection of {@link Membership} if nothing found collection will
    *         be empty
    * @throws Exception if any errors occurs
    */
   @SuppressWarnings("unchecked")
   private Collection findMemberships(LdapContext ctx, String userName, String groupId, String type) throws Exception
   {
      Collection<Membership> list = new ArrayList<Membership>();
      String userDN = getDNFromUsername(ctx, userName);
      if (userDN == null)
         return list;

      String filter = ldapAttrMapping.userObjectClassFilter;
      String[] retAttrs = {"tokenGroups"};
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.OBJECT_SCOPE);
      constraints.setReturningAttributes(retAttrs);

      NamingEnumeration<SearchResult> results = null;
      try
      {
         results = ctx.search(userDN, filter, constraints);
         while (results.hasMore())
         {
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();
            Attribute attr = attrs.get("tokenGroups");
            for (int x = 0; x < attr.size(); x++)
            {
               byte[] SID = (byte[])attr.get(x);
               // String membershipDN = adSearch.findMembershipDNBySID(SID, groupId,
               // type);
               String membershipDN = adSearch.findMembershipDNBySID(ctx, SID, groupId, type);
               if (membershipDN != null)
                  list.add(createMembershipObject(ctx, membershipDN, userName, type));
            }
         }
         return list;
      }
      finally
      {
         if (results != null)
            results.close();
      }
   }

   /**
    * Create {@link Membership} instance.
    * 
    * @param ctx {@link LdapContext}
    * @param dn Distinguished Name
    * @param user user name
    * @param type membership type
    * @return newly created instance of {@link Membership}
    * @throws Exception if any errors occurs
    */
   private Membership createMembershipObject(LdapContext ctx, String dn, String user, String type) throws Exception
   {
      Group group = getGroupFromMembershipDN(ctx, dn);
      if (type == null)
         type = explodeDN(dn, true)[0];
      MembershipImpl membership = new MembershipImpl();
      membership.setId(user + "," + type + "," + group.getId());
      membership.setUserName(user);
      membership.setMembershipType(type);
      membership.setGroupId(group.getId());
      return membership;
   }
}
