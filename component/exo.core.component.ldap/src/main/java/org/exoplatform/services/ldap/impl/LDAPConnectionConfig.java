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

public class LDAPConnectionConfig
{

   private String providerURL = "ldap://127.0.0.1:389";

   private String rootdn;

   private String password; // = "exo";

   private String version; // =

   // LDAPConnection
   // .LDAP_V3;

   private String authenticationType = "simple";

   private String serverName = "default";

   private int minConnection;

   private int maxConnection;

   private String referralMode = "follow";

   public String getRootDN()
   {
      return this.rootdn;
   }

   public void setRootDN(String d)
   {
      this.rootdn = d;
   }

   public String getPassword()
   {
      return this.password;
   }

   public void setPassword(String p)
   {
      this.password = p;
   }

   public String getVerion()
   {
      return this.version;
   }

   public void setVersion(String v)
   {
      this.version = v;
   }

   public String getAuthenticationType()
   {
      return authenticationType;
   }

   public void setAuthenticationType(String s)
   {
      authenticationType = s;
   }

   public int getMinConnection()
   {
      return this.minConnection;
   }

   public void setMinConnection(int n)
   {
      this.minConnection = n;
   }

   public int getMaxConnection()
   {
      return this.maxConnection;
   }

   public void setMaxConnection(int n)
   {
      this.maxConnection = n;
   }

   public String getProviderURL()
   {
      return providerURL;
   }

   public void setProviderURL(String s)
   {
      providerURL = s;
   }

   public String getServerName()
   {
      return serverName;
   }

   public void setServerName(String n)
   {
      serverName = n;
   }

   public String getReferralMode()
   {
      return referralMode;
   }

   public void setReferralMode(String referral)
   {
      referralMode = referral;
   }
}
