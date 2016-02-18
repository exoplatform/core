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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * Created by the eXo platform team User: Daniel Summer Date: 25/5/2004
 * interface abstracted from JSDK
 */
public interface LDAPService
{

   public static int DEFAULT_SERVER = 0;

   public static int ACTIVE_DIRECTORY_SERVER = 1;

   // public static int OPEN_LDAP_SERVER = 2;
   //
   // public static int NETSCAPE_SERVER = 3;
   //
   // public static int REDHAT_SERVER = 4;

   /**
    * Normal context for all directories.
    * 
    * @return {@link LdapContext}
    * @throws NamingException if errors occurs when try to get context
    */
   LdapContext getLdapContext() throws NamingException;

   /**
    * Get new LdapContext. Force create new context.
    * 
    * @param renew should be created new LdapContext
    * @return {@link LdapContext}
    * @throws NamingException if errors occurs when try to get context
    * @see #getLdapContext()
    */
   LdapContext getLdapContext(boolean renew) throws NamingException;

   /**
    * Release LdapContext, so that it can be recycled.
    * 
    * @param ctx {@link LdapContext}
    * @throws NamingException if errors occurs when release context
    */
   void release(LdapContext ctx) throws NamingException;

   /**
    * LDAP booster pack context for v3 directories (except Active Directory).
    * 
    * @return {@link InitialContext}
    * @throws NamingException if errors occurs when try to get context
    */
   InitialContext getInitialContext() throws NamingException;

   /**
    * LDAP bind authentication.
    * 
    * @param userDN userDN
    * @param password user's password
    * @return true is user authenticated false otherwise
    * @throws NamingException if errors occurs when try to authenticate user
    */
   boolean authenticate(String userDN, String password) throws NamingException;

   /**
    * @return LDAP server type
    */
   int getServerType();

}
