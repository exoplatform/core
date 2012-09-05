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
package org.exoplatform.services.ldap.impl;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.ldap.CreateObjectCommand;
import org.exoplatform.services.ldap.DeleteObjectCommand;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS .
 * Author : James Chamberlain james@echamberlains.com
 * Date: 11/2/2005
 */
public class LDAPServiceImpl implements LDAPService, ComponentRequestLifecycle
{

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.ldap.LDAPServiceImpl");

   private Map<String, String> env = new HashMap<String, String>();

   private int serverType = DEFAULT_SERVER;

   /**
    * @param params See {@link InitParams}
    */
   public LDAPServiceImpl(InitParams params)
   {
      LDAPConnectionConfig config = (LDAPConnectionConfig)params.getObjectParam("ldap.config").getObject();

      String url = config.getProviderURL();
      serverType = toServerType(config.getServerName());

      boolean ssl = url.toLowerCase().startsWith("ldaps");
      if (serverType == ACTIVE_DIRECTORY_SERVER && ssl)
      {
         StringBuilder keystore = new StringBuilder(System.getProperty("java.home"));
         keystore.append(File.separator);
         keystore.append("lib");
         keystore.append(File.separator);
         keystore.append("security");
         keystore.append(File.separator);
         keystore.append("cacerts");
         PrivilegedSystemHelper.setProperty("javax.net.ssl.trustStore", keystore.toString());
      }

      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.SECURITY_AUTHENTICATION, config.getAuthenticationType());
      env.put(Context.SECURITY_PRINCIPAL, config.getRootDN());
      env.put(Context.SECURITY_CREDENTIALS, config.getPassword());

      if (config.getTimeout() > 0)
      {
         PrivilegedSystemHelper.setProperty("com.sun.jndi.ldap.connect.pool.timeout",
            Integer.toString(config.getTimeout()));
      }

      if (config.getMinConnection() > 0)
      {
         PrivilegedSystemHelper.setProperty("com.sun.jndi.ldap.connect.pool.initsize",
            Integer.toString(config.getMinConnection()));
         PrivilegedSystemHelper.setProperty("com.sun.jndi.ldap.connect.pool.prefsize",
            Integer.toString(config.getMinConnection()));
      }

      if (config.getMaxConnection() > 0)
      {
         PrivilegedSystemHelper.setProperty("com.sun.jndi.ldap.connect.pool.maxsize",
            Integer.toString(config.getMaxConnection()));
      }

      env.put("com.sun.jndi.ldap.connect.pool", "true");
      env.put("java.naming.ldap.version", config.getVerion());
      env.put("java.naming.ldap.attributes.binary", "tokenGroups");
      env.put(Context.REFERRAL, config.getReferralMode());

      Pattern pattern = Pattern.compile("\\p{Space}*,\\p{Space}*", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(url);
      if (ssl)
         url = matcher.replaceAll("/ ldaps://");
      else
         url = matcher.replaceAll("/ ldap://");
      url += "/"; //NOSONAR
      env.put(Context.PROVIDER_URL, url);

      if (serverType == ACTIVE_DIRECTORY_SERVER && ssl)
         env.put(Context.SECURITY_PROTOCOL, "ssl");
   }

   /**
    * {@inheritDoc}
    */
   public LdapContext getLdapContext() throws NamingException
   {
      // This method can be used for getting context from thread-local variables,
      // etc. instead create new instance of LdapContext. Currently just create
      // new one (use from pool if 'com.sun.jndi.ldap.connect.pool' is 'true').
      // Override this method if need other behavior.
      return getLdapContext(true);
   }

   /**
    * {@inheritDoc}
    */
   public LdapContext getLdapContext(boolean renew) throws NamingException
   {
      // Force create new context.  
      return new InitialLdapContext(new Hashtable<String, String>(env), null);
   }

   /**
    * {@inheritDoc}
    */
   public void release(LdapContext ctx)
   {
      // Just close since we are not pooling anything by self.
      // Override this method if need other behavior.
      closeContext(ctx);
   }

   /**
    * {@inheritDoc}
    */
   public InitialContext getInitialContext() throws NamingException
   {
      Hashtable<String, String> props = new Hashtable<String, String>(env);
      props.put(Context.OBJECT_FACTORIES, "com.sun.jndi.ldap.obj.LdapGroupFactory");
      props.put(Context.STATE_FACTORIES, "com.sun.jndi.ldap.obj.LdapGroupFactory");
      return new InitialLdapContext(props, null);
   }

   /**
    * {@inheritDoc}
    */
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
         ctx = new InitialLdapContext(props, null);

         // anonymous user could be bind to AD but aren't able to pick up information
         return (ctx.lookup(userDN) != null);
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

   /**
    * {@inheritDoc}
    */
   public int getServerType()
   {
      return serverType;
   }

   /**
    * Delete objects from context.
    * 
    * @param plugin see {@link DeleteObjectCommand} {@link ComponentPlugin}
    * @throws NamingException if {@link NamingException} occurs
    */
   public void addDeleteObject(ComponentPlugin plugin) throws NamingException
   {
   }

   private void unbind(LdapContext ctx, String name) throws NamingException
   {
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      NamingEnumeration<SearchResult> results = ctx.search(name, "(objectclass=*)", constraints);
      try
      {
         while (results.hasMore())
         {
            SearchResult sr = results.next();
            unbind(ctx, sr.getNameInNamespace());
         }
         // close search results enumeration
      }
      finally
      {
         results.close();
      }
      ctx.unbind(name);
   }

   /**
    * Create objects in context.
    * 
    * @param plugin see {@link CreateObjectCommand} {@link ComponentPlugin}
    * @throws NamingException if {@link NamingException} occurs
    */
   public void addCreateObject(ComponentPlugin plugin) throws NamingException
   {
      if (plugin instanceof CreateObjectCommand)
      {
         CreateObjectCommand command = (CreateObjectCommand)plugin;
         Map<String, Attributes> objectsToCreate = command.getObjectsToCreate();
         if (objectsToCreate == null || objectsToCreate.size() == 0)
            return;
         LdapContext ctx = getLdapContext();
         for (Map.Entry<String, Attributes> e : objectsToCreate.entrySet())
         {
            String name = e.getKey();
            Attributes attrs = e.getValue();
            try
            {
               try
               {
                  ctx.createSubcontext(name, attrs);
               }
               catch (CommunicationException e1)
               {
                  // create new LDAP context
                  ctx = getLdapContext(true);
                  // try repeat operation where communication error occurs
                  ctx.createSubcontext(name, attrs);
               }
               catch (ServiceUnavailableException e2)
               {
                  // do the same as for CommunicationException
                  ctx = getLdapContext(true);
                  //
                  ctx.createSubcontext(name, attrs);
               }
            }
            catch (Exception e3)
            {
               // Catch all exceptions here.
               // just inform about exception if it is not connection problem.
               LOG.error("Create object (" + name + ") failed. ", e3);
            }
         }
         release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    * 
    * @deprecated Will be removed
    */
   public void startRequest(ExoContainer container)
   {
   }

   /**
    * {@inheritDoc}
    * 
    * @deprecated Will be removed
    */
   public void endRequest(ExoContainer container)
   {
      //     LdapContext context = tlocal_.get();
      //    if (context != null) {
      //      try {
      //        context.close();
      //        tlocal_.set(null);
      //      } catch (Exception ex) {
      //        ex.printStackTrace();
      //      }
      //    }
   }

   private int toServerType(String name)
   {
      name = name.trim();
      if (name == null || name.length() < 1)
         return DEFAULT_SERVER;
      if (name.equalsIgnoreCase("ACTIVE.DIRECTORY"))
         return ACTIVE_DIRECTORY_SERVER;
      // if(name.equalsIgnoreCase("OPEN.LDAP"))return OPEN_LDAP_SERVER;
      // if(name.equalsIgnoreCase("NETSCAPE.DIRECTORY")) return NETSCAPE_SERVER;
      // if(name.equalsIgnoreCase("REDHAT.DIRECTORY")) return REDHAT_SERVER;
      return DEFAULT_SERVER;
   }

   /**
    * Closes LDAP context and shows warning if exception occurred. 
    * 
    * @param ctx
    *          LDAP context
    */
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
