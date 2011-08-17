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
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.exoplatform.services.organization.ldap.CacheHandler.CacheType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 14, 2005. @version andrew00x $
 */
public class MembershipTypeDAOImpl extends BaseDAO implements MembershipTypeHandler
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.MembershipTypeDAOImpl");

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items (users, groups, etc)
    * @param ldapService {@link LDAPService}
    * @param cacheHandler
    *          The Cache Handler
    * @throws Exception if any errors occurs
    */
   public MembershipTypeDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, CacheHandler cacheHandler)
      throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
   }

   /**
    * {@inheritDoc}
    */
   public final MembershipType createMembershipTypeInstance()
   {
      return new MembershipTypeImpl();
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      String membershipTypeDN =
         ldapAttrMapping.membershipTypeNameAttr + "=" + mt.getName() + "," + ldapAttrMapping.membershipTypeURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               try
               {
                  ctx.lookup(membershipTypeDN);
               }
               catch (NameNotFoundException e)
               {
                  Date now = new Date();
                  mt.setCreatedDate(now);
                  mt.setModifiedDate(now);
                  ctx.createSubcontext(membershipTypeDN, ldapAttrMapping.membershipTypeToAttributes(mt));

                  cacheHandler.put(mt.getName(), mt, CacheType.MEMBERSHIPTYPE);
               }
               return mt;
            }
            catch (NamingException e1)
            {
               ctx = reloadCtx(ctx, err, e1);
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
   public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      String membershipTypeDN =
         ldapAttrMapping.membershipTypeNameAttr + "=" + mt.getName() + "," + ldapAttrMapping.membershipTypeURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               Attributes attrs = ctx.getAttributes(membershipTypeDN);

               if (/*attrs == null || */attrs.size() == 0)
                  return mt;
               ModificationItem[] mods = new ModificationItem[1];
               String desc = mt.getDescription();
               if (desc != null && desc.length() > 0)
               {
                  mods[0] =
                     new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                        ldapAttrMapping.ldapDescriptionAttr, mt.getDescription()));
               }
               else
               {
                  mods[0] =
                     new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(
                        ldapAttrMapping.ldapDescriptionAttr, mt.getDescription()));
               }
               ctx.modifyAttributes(membershipTypeDN, mods);

               cacheHandler.put(mt.getName(), mt, CacheType.MEMBERSHIPTYPE);
               return mt;
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
   public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception
   {
      String membershipTypeDN =
         ldapAttrMapping.membershipTypeNameAttr + "=" + name + "," + ldapAttrMapping.membershipTypeURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               Attributes attrs = ctx.getAttributes(membershipTypeDN);
               MembershipType m = ldapAttrMapping.attributesToMembershipType(attrs);
               removeMembership(ctx, name);
               ctx.destroySubcontext(membershipTypeDN);

               cacheHandler.remove(name, CacheType.MEMBERSHIPTYPE);
               return m;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      catch (NameNotFoundException e)
      {
         if (LOG.isDebugEnabled())
            e.printStackTrace();
         return null;
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public MembershipType findMembershipType(String name) throws Exception
   {
      MembershipType mt = (MembershipType)cacheHandler.get(name, CacheType.MEMBERSHIPTYPE);
      if (mt != null)
      {
         return mt;
      }

      String membershipTypeDN =
         ldapAttrMapping.membershipTypeNameAttr + "=" + name + "," + ldapAttrMapping.membershipTypeURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               Attributes attrs = ctx.getAttributes(membershipTypeDN);
               mt = ldapAttrMapping.attributesToMembershipType(attrs);
               if (mt != null)
               {
                  cacheHandler.put(name, mt, CacheType.MEMBERSHIPTYPE);
               }
               return mt;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      catch (NameNotFoundException e)
      {
         if (LOG.isDebugEnabled())
            e.printStackTrace();
         return null;
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
   public Collection findMembershipTypes() throws Exception
   {
      Collection<MembershipType> memberships = new ArrayList<MembershipType>();
      String filter = ldapAttrMapping.membershipTypeNameAttr + "=*";
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            // clear if something was added in previous iteration
            memberships.clear();
            try
            {
               results = ctx.search(ldapAttrMapping.membershipTypeURL, filter, constraints);
               while (results.hasMore())
               {
                  SearchResult sr = results.next();
                  Attributes attrs = sr.getAttributes();
                  memberships.add(ldapAttrMapping.attributesToMembershipType(attrs));
               }
               return memberships;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
            finally
            {
               if (results != null)
                  results.close();
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   // helpers

   private void removeMembership(LdapContext ctx, String name) throws NamingException
   {
      NamingEnumeration<SearchResult> results = null;
      try
      {
         SearchControls constraints = new SearchControls();
         constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
         String filter = ldapAttrMapping.membershipTypeNameAttr + "=" + name;
         results = ctx.search(ldapAttrMapping.groupsURL, filter, constraints);
         while (results.hasMore())
         {
            SearchResult sr = results.next();
            ctx.destroySubcontext(sr.getNameInNamespace());

            cacheHandler.remove(CacheHandler.MEMBERSHIPTYPE_PREFIX + name, CacheType.MEMBERSHIP);
         }
      }
      finally
      {
         if (results != null)
            results.close();
      }
   }

}
