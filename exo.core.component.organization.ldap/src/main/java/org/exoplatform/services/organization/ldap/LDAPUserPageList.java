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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;

/**
 * Created by VietSpider Studio Author : Nhu Dinh Thuan nhudinhthuan@yahoo.com
 * Dec 7, 2005, @version andrew00x $
 * @deprecated
 */
public class LDAPUserPageList extends PageList
{

   private String searchBase;

   private String filter;

   private LDAPService ldapService;

   private LDAPAttributeMapping ldapAttrMapping;
   
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.LDAPUserPageList");

   static boolean SEARCH_CONTROL = Control.NONCRITICAL;

   public LDAPUserPageList(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, String searchBase,
      String filter, int pageSize) throws Exception
   {
      super(pageSize);
      this.ldapAttrMapping = ldapAttrMapping;
      this.ldapService = ldapService;
      this.searchBase = searchBase;
      this.filter = filter;
      try
      {
         int size = this.getResultSize();
         setAvailablePage(size);
      }
      catch (NameNotFoundException e)
      {
         LOG.warn("Cannot set the page size while creating a LDAPUserPageList, no page size will be used", e);
         setAvailablePage(0);
      }
      catch (OperationNotSupportedException e)
      {
         LOG.warn("Cannot set the page size while creating a LDAPUserPageList, no page size will be used", e);
         setAvailablePage(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
  protected void populateCurrentPage(int page) throws Exception
   {
      List<User> users = new ArrayList<User>();
      PagedResultsControl prc = new PagedResultsControl(getPageSize(), Control.NONCRITICAL);
      String[] keys = {ldapAttrMapping.userUsernameAttr};
      SortControl sctl = new SortControl(keys, SEARCH_CONTROL);

      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            users.clear();
            try
            {
               ctx.setRequestControls(new Control[]{sctl, prc});
               SearchControls constraints = new SearchControls();
               constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

               byte[] cookie = null;
               int counter = 0;

               do
               {
                  counter++;
                  results = ctx.search(searchBase, filter, constraints);

                  while (results != null && results.hasMore())
                  {
                     SearchResult result = results.next();
                     if (counter == page)
                        users.add(ldapAttrMapping.attributesToUser(result.getAttributes()));
                  }

                  Control[] responseControls = ctx.getResponseControls();
                  if (responseControls != null)
                  {
                     for (int z = 0; z < responseControls.length; z++)
                     {
                        if (responseControls[z] instanceof PagedResultsResponseControl)
                           cookie = ((PagedResultsResponseControl)responseControls[z]).getCookie();
                     }
                  }
                  ctx
                     .setRequestControls(new Control[]{new PagedResultsControl(getPageSize(), cookie, Control.CRITICAL)});
               }
               while (cookie != null);
               this.currentListPage_ = users;
               return;
            }
            catch (NamingException e)
            {
               if (BaseDAO.isConnectionError(e) && err < BaseDAO.getMaxConnectionError())
               {
                  ldapService.release(ctx);
                  ctx = ldapService.getLdapContext(true);
               }
               else
                  throw e;
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

   private int getResultSize() throws Exception
   {
      return getAll().size();
   }

   /**
    * {@inheritDoc}
    */
   @Override
  @SuppressWarnings("unchecked")
   public List getAll() throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      List<User> users = new ArrayList<User>();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            users.clear();
            try
            {
               SearchControls constraints = new SearchControls();
               String[] returnedAtts = {ldapAttrMapping.userUsernameAttr};
               constraints.setReturningAttributes(returnedAtts);
               constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

               results = ctx.search(searchBase, filter, constraints);

               while (results != null && results.hasMore())
               {
                  SearchResult result = results.next();
                  users.add(ldapAttrMapping.attributesToUser(result.getAttributes()));
               }

               return users;
            }
            catch (NamingException e)
            {
               if (BaseDAO.isConnectionError(e) && err < 1)
               {
                  ldapService.release(ctx);
                  ctx = ldapService.getLdapContext(true);
               }
               else
                  throw e;
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

}
