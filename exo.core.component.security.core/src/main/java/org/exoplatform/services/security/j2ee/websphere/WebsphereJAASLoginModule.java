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
package org.exoplatform.services.security.j2ee.websphere;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.jaas.DefaultLoginModule;
import org.exoplatform.services.security.jaas.RolePrincipal;
import org.exoplatform.services.security.jaas.UserPrincipal;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS.
 * 
 * WebSphere JAAS login module for provide security credential.
 * 
 * @author <a href="mailto:alexey.zavizionov@exoplatform.com.ua">Alexey
 *         Zavizionov</a>
 * @version $Id: WebsphereJAASLoginModule.java 8478 2007-12-03 10:45:34Z rainf0x
 *          $
 */
public class WebsphereJAASLoginModule extends DefaultLoginModule
{

   /**
    * Exo logger.
    */
   private Log log = ExoLogger.getLogger("core.ExoWebsphereJAASLoginModule");

   /**
    * Default constructor.
    */
   public WebsphereJAASLoginModule()
   {
   }

   /**
    * {@inheritDoc} 
    */
   @Override
   public boolean login() throws LoginException
   {
      if (log.isDebugEnabled())
         log.debug("In login of WebsphereJAASLoginModule");
      if (super.login())
      {
         ArrayList<String> roleGroupList = new ArrayList<String>();

         for (String role : identity.getRoles())
         {
            roleGroupList.add(role);
         }
         // username principal
         // Principal usernamePrincipal = new UserPrincipal(identity_.getUserId());
         websphereLogin(identity.getUserId(), roleGroupList);

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
   @Override
   public boolean commit() throws LoginException
   {

      if (super.commit())
      {

         Set<Principal> principals = subject.getPrincipals();

         for (String role : identity.getRoles())
            principals.add(new RolePrincipal(role));

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
   @Override
   public boolean abort() throws LoginException
   {
      if (log.isDebugEnabled())
         log.debug("In abort of WebsphereJAASLoginModule");
      return super.abort();
   }

   /**
    * {@inheritDoc} 
    */
   @Override
   public boolean logout() throws LoginException
   {
      if (log.isDebugEnabled())
         log.debug("In logout of WebsphereJAASLoginModule");
      // getSubject().getPrincipals().remove(usernamePrincipal);
      return super.logout();
   }

   /**
    * WebSphere security credential constant name for propertiesObject.
    */
   final public static String WSCREDENTIAL_PROPERTIES_KEY = "com.ibm.wsspi.security.cred.propertiesObject";

   /**
    * WebSphere security credential constant name for uniqueId.
    */
   final public static String WSCREDENTIAL_UNIQUEID = "com.ibm.wsspi.security.cred.uniqueId";

   /**
    * WebSphere security credential constant name for securityName.
    */
   final public static String WSCREDENTIAL_SECURITYNAME = "com.ibm.wsspi.security.cred.securityName";

   /**
    * WebSphere security credential constant name for groups.
    */
   final public static String WSCREDENTIAL_GROUPS = "com.ibm.wsspi.security.cred.groups";

   /**
    * WebSphere security credential constant name for cacheKey.
    */
   final public static String WSCREDENTIAL_CACHE_KEY = "com.ibm.wsspi.security.cred.cacheKey";

   /**
    * Create and set map of public credentials into subject. 
    * 
    * @param user String user name
    * @param roleGroupList ArrayList<String> list of role groups
    */
   @SuppressWarnings("unchecked")
   private void websphereLogin(String user, ArrayList<String> roleGroupList)
   {
      Hashtable hashtable = new Hashtable();
      String uniqueid = user;
      hashtable.put(WSCREDENTIAL_UNIQUEID, uniqueid);
      hashtable.put(WSCREDENTIAL_SECURITYNAME, user);
      hashtable.put(WSCREDENTIAL_GROUPS, roleGroupList);
      hashtable.put(WSCREDENTIAL_CACHE_KEY, uniqueid + "WebsphereJAASLoginModule");
      // sharedState.put(WSCREDENTIAL_PROPERTIES_KEY, hashtable);
      subject.getPublicCredentials().add(hashtable);
   }
}
