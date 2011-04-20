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
package org.exoplatform.services.security.j2ee;

import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.services.security.jaas.DefaultLoginModule;
import org.exoplatform.services.security.jaas.JAASGroup;
import org.exoplatform.services.security.jaas.RolePrincipal;
import org.exoplatform.services.security.jaas.UserPrincipal;
import org.jboss.security.auth.callback.MapCallback;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: JbossLoginModule.java 34415 2009-07-23 14:33:34Z dkatayev $
 */

public class JbossLoginModule extends DefaultLoginModule
{
   /** . */
   private static Log log = ExoLogger.getLogger("exo.core.component.security.core.JbossLoginModule.class");

   /**
    * To retrieve password context during Digest Authentication.
    */
   private MapCallback[] mapCallback = {new MapCallback()};

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean commit() throws LoginException
   {

      if (super.commit())
      {

         Set<Principal> principals = subject.getPrincipals();

         Group roleGroup = new JAASGroup(JAASGroup.ROLES);
         for (String role : identity.getRoles())
            roleGroup.addMember(new RolePrincipal(role));

         // group principal
         principals.add(roleGroup);

         // username principal
         principals.add(new UserPrincipal(identity.getUserId()));

         return true;
      }
      else
      {
         return false;
      }

   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public boolean login() throws LoginException
   {
      if (log.isDebugEnabled())
      {
         log.debug("In login of JbossLoginModule.");
      }
      try
      {
         if (sharedState.containsKey("exo.security.identity"))
         {
            if (log.isDebugEnabled())
            {
               log.debug("Use Identity from previous LoginModule");
            }
            identity = (Identity)sharedState.get("exo.security.identity");
         }
         else
         {
            if (!digestAuthenticationIsUsed())
            {
               return super.login();
            }

            if (log.isDebugEnabled())
            {
               log.debug("Try create identity");
            }

            Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);

            if (authenticator == null)
            {
               throw new LoginException("No Authenticator component found, check your configuration");
            }

            String userId = authenticator.validateUser(getCredentials());

            identity = authenticator.createIdentity(userId);
            sharedState.put("javax.security.auth.login.name", userId);
            subject.getPrivateCredentials().add(getPassword());
            subject.getPublicCredentials().add(getUsername());
         }
         return true;

      }
      catch (final Throwable e)
      {
         if (log.isDebugEnabled())
         {
            log.debug(e.getMessage(), e);
         }

         throw new LoginException(e.getMessage());
      }
   }

   /**
    * An utility method handles mapCallback and also checks if digest authentication is used.
    * @return true if digest authentication is used, otherwise - false
    * @throws IOException
    */
   private boolean digestAuthenticationIsUsed() throws IOException
   {
      try
      {
         // here we're trying to handle mapCallback
         // if it is handled successfully than digest
         // authentication is used
         callbackHandler.handle(mapCallback);
         return true;
      }
      catch (UnsupportedCallbackException uce)
      {
         // otherwise UnsupportedCallbackException is thrown
         return false;
      }
   }

   /**
    * An utility method to retrieve credentials. All needed for password hashing information 
    * is retrieved from MapCallback. NameCallback and PasswordCallback are used to correspondingly  
    * retrieve username and password.
    * @return Credential
    * @throws IOException 
    * @throws Exception
    */
   private Credential[] getCredentials() throws IOException
   {
      String username = null;
      String password = null;
      Map<String, String> passwordContext = new HashMap<String, String>();

      passwordContext.put("qop", (String)mapCallback[0].getInfo("qop"));
      passwordContext.put("nonce", (String)mapCallback[0].getInfo("nonce"));
      passwordContext.put("cnonce", (String)mapCallback[0].getInfo("cnonce"));
      passwordContext.put("a2hash", (String)mapCallback[0].getInfo("a2hash"));
      passwordContext.put("nc", (String)mapCallback[0].getInfo("nc"));
      passwordContext.put("realm", (String)mapCallback[0].getInfo("realm"));

      try
      {
         Callback[] nameCallback = {new NameCallback("Username")};
         callbackHandler.handle(nameCallback);
         username = ((NameCallback)nameCallback[0]).getName();
      }
      catch (UnsupportedCallbackException e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Error on retrieving username from callback handler! ", e);
         }
      }

      try
      {
         Callback[] passwordCallback = {new PasswordCallback("Password", false)};
         callbackHandler.handle(passwordCallback);
         password = new String(((PasswordCallback)passwordCallback[0]).getPassword());
         ((PasswordCallback)passwordCallback[0]).clearPassword();
      }
      catch (UnsupportedCallbackException e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Error on retrieving password from callback handler! ", e);
         }
      }

      if (username == null || password == null)
      {
         return null;
      }

      return new Credential[]{new UsernameCredential(username), new PasswordCredential(password, passwordContext)};
   }

   private UsernameCredential getUsername() throws IOException
   {
      String username = null;

      try
      {
         Callback[] nameCallback = {new NameCallback("Username")};
         callbackHandler.handle(nameCallback);
         username = ((NameCallback)nameCallback[0]).getName();
      }
      catch (UnsupportedCallbackException e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Error on retrieving username from callback handler! ", e);
         }
      }

      return new UsernameCredential(username);
   }

   private String getPassword() throws IOException
   {
      String password = null;

      try
      {
         Callback[] passwordCallback = {new PasswordCallback("Password", false)};
         callbackHandler.handle(passwordCallback);
         password = new String(((PasswordCallback)passwordCallback[0]).getPassword());
         ((PasswordCallback)passwordCallback[0]).clearPassword();
      }
      catch (UnsupportedCallbackException e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Error on retrieving password from callback handler! ", e);
         }
      }

      return password;
   }

   /**
    * Attempts eviction of the subject in the JBoss security manager cache.
    *
    * @return a boolean
    * @throws LoginException any login exception
    */
   @Override
   public boolean logout() throws LoginException
   {
      org.exoplatform.container.monitor.jvm.J2EEServerInfo info = new J2EEServerInfo();
      MBeanServer jbossServer = info.getMBeanServer();

      //
      if (jbossServer != null)
      {
         try
         {

            log.debug("Performing JBoss security manager cache eviction");

            ObjectName securityManagerName = new ObjectName("jboss.security:service=JaasSecurityManager");

            // Obtain user name
            String userName = null;
            Set<UserPrincipal> userPrincipals = subject.getPrincipals(UserPrincipal.class);
            if (!userPrincipals.isEmpty())
            {
               // There should be one
               userName = userPrincipals.iterator().next().getName();
            }

            //
            if (userName != null)
            {
               log.debug("Going to perform JBoss security manager cache eviction for user " + userName);

               //
               List allPrincipals =
                  (List)jbossServer.invoke(securityManagerName, "getAuthenticationCachePrincipals",
                     new Object[]{realmName}, new String[]{String.class.getName()});

               // Make a copy to avoid some concurrent mods
               allPrincipals = new ArrayList(allPrincipals);

               // Lookup for invalidation key, it must be the same principal!
               Principal key = null;
               for (Iterator i = allPrincipals.iterator(); i.hasNext();)
               {
                  Principal principal = (Principal)i.next();
                  if (principal.getName().equals(userName))
                  {
                     key = principal;
                     break;
                  }
               }

               // Perform invalidation
               if (key != null)
               {
                  jbossServer.invoke(securityManagerName, "flushAuthenticationCache", new Object[]{realmName, key},
                     new String[]{String.class.getName(), Principal.class.getName()});
                  log.debug("Performed JBoss security manager cache eviction for user " + userName + " with principal "
                     + key);
               }
               else
               {
                  log.warn("No principal found when performing JBoss security manager cache eviction for user "
                     + userName);
               }
            }
            else
            {
               log.warn("No user name found when performing JBoss security manager cache eviction");
            }
         }
         catch (Exception e)
         {
            log.error("Could not perform JBoss security manager cache eviction", e);
         }
      }
      else
      {
         log.debug("Could not find mbean server for performing JBoss security manager cache eviction");
      }

      //
      return true;
   }
}
