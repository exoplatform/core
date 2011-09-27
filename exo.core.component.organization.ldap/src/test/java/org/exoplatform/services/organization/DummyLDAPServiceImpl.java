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

package org.exoplatform.services.organization;

import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * @author <a href="mailto:dmi3.kuleshov@gmail.com">Dmitry Kuleshov</a>
 */
public class DummyLDAPServiceImpl implements LDAPService
{
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.ldap.LDAPServiceImpl");

   private Map<String, String> env = new HashMap<String, String>();

   private int serverType = DEFAULT_SERVER;

   public DummyLDAPServiceImpl()
   {
      env.put(Context.PROVIDER_URL, "dc=exoplatform,dc=org");
      env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
      env.put(Context.SECURITY_CREDENTIALS, "secret");
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.directory.server.jndi.ServerContextFactory");
   }
   @Override
   public LdapContext getLdapContext() throws NamingException
   {
      return new DummyLdapContext(new InitialContext(new Hashtable<String, String>(env)));
   }

   @Override
   public LdapContext getLdapContext(boolean renew) throws NamingException
   {
      return getLdapContext();
   }

   @Override
   public void release(LdapContext ctx) throws NamingException
   {
      try
      {
         if (ctx != null)
         {
            ctx.close();
         }
      }
      catch (NamingException e)
      {
         LOG.warn("Exception occurred when tried to close context", e);
      }

   }

   @Override
   public InitialContext getInitialContext() throws NamingException
   {
      Hashtable<String, String> props = new Hashtable<String, String>(env);
      props.put(Context.OBJECT_FACTORIES, "com.sun.jndi.ldap.obj.LdapGroupFactory");
      props.put(Context.STATE_FACTORIES, "com.sun.jndi.ldap.obj.LdapGroupFactory");
      return new DummyLdapContext(new InitialContext(props));
   }

   @Override
   public boolean authenticate(String userDN, String password) throws NamingException
   {
      Hashtable<String, String> props = new Hashtable<String, String>(env);
      props.put(Context.SECURITY_AUTHENTICATION, "simple");
      props.put(Context.SECURITY_PRINCIPAL, userDN);
      props.put(Context.SECURITY_CREDENTIALS, password);
      props.put("com.sun.jndi.ldap.connect.pool", "false");

      InitialContext ctx = null;
      try
      {
         ctx = new DummyLdapContext(new InitialContext(new Hashtable<String, String>(env)));
         return true;
      }
      catch (NamingException e)
      {
         LOG.debug("Error during initialization LDAP Context", e);
         return false;
      }
      finally
      {
         closeContext(ctx);
      }
   }

   @Override
   public int getServerType()
   {
      return 0;
   }

   private int toServerType(String name)
   {
      name = name.trim();
      if (name == null || name.length() < 1)
         return DEFAULT_SERVER;
      if (name.equalsIgnoreCase("ACTIVE.DIRECTORY"))
         return ACTIVE_DIRECTORY_SERVER;
      return DEFAULT_SERVER;
   }

   private void closeContext(Context ctx)
   {
      try
      {
         if (ctx != null)
         {
            ctx.close();
         }
      }
      catch (NamingException e)
      {
         LOG.warn("Exception occurred when tried to close context", e);
      }
   }

}
