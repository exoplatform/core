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
import org.exoplatform.services.organization.User;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS . Author : James Chamberlain
 * james.chamberlain@gmail.com
 */

public class ADUserDAOImpl extends UserDAOImpl
{

   /**
    * AD user's account controls attribute.
    */
   int UF_ACCOUNTDISABLE = 0x0002;

   /**
    * AD user's account controls attribute.
    */
   int UF_PASSWD_NOTREQD = 0x0020;

   /**
    * AD user's account controls attribute.
    */
   int UF_NORMAL_ACCOUNT = 0x0200;

   /**
    * AD user's account controls attribute.
    */
   int UF_PASSWORD_EXPIRED = 0x800000;

   /**
    * @param ldapAttrMapping {@link LDAPAttributeMapping}
    * @param ldapService {@link LDAPService}
    * @throws Exception if any errors occurs
    */
   public ADUserDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService) throws Exception
   {
      super(ldapAttrMapping, ldapService);
      LDAPUserPageList.SEARCH_CONTROL = Control.CRITICAL;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void createUser(User user, boolean broadcast) throws Exception
   {
      String userDN = ldapAttrMapping.userDNKey + "=" + user.getUserName() + "," + ldapAttrMapping.userURL;
      Attributes attrs = ldapAttrMapping.userToAttributes(user);
      attrs.put("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED
         + UF_ACCOUNTDISABLE));
      attrs.remove(ldapAttrMapping.userPassword);
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               if (broadcast)
                  preSave(user, true);
               // see comments about saving password below
               ctx.createSubcontext(userDN, attrs);
               if (broadcast)
                  postSave(user, true);
               break;
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
      // Really need do it separately ?
      // Do it in method with new LdapContext to avoid NameAlreadyBoundException,
      // if got connection error occurs when try to save password.
      saveUserPassword(user, userDN);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   void saveUserPassword(User user, String userDN) throws Exception
   {
      Object v = ldapService.getLdapContext().getEnvironment().get(Context.SECURITY_PROTOCOL);
      if (v == null)
         return;
      String security = String.valueOf(v);
      if (!security.equalsIgnoreCase("ssl"))
         return;
      String newQuotedPassword = "\"" + user.getPassword() + "\"";
      byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
      ModificationItem[] mods = new ModificationItem[2];
      mods[0] =
         new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userPassword,
            newUnicodePassword));
      mods[1] =
         new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer
            .toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               ctx.modifyAttributes(userDN, mods);
               break;
            }
            catch (NamingException e)
            {
               if (isConnectionError(e) && err < getMaxConnectionError())
                  ctx = ldapService.getLdapContext(true);
               else
                  throw e;
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }
}
