/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.database.creator;

/**
 * Class contains needed database connection information. 
 * 
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: DBConnectionInfo.java 111 2010-11-11 11:11:11Z tolusha $
 */
public class DBConnectionInfo
{
   /**
    * Driver class name.
    */
   private final String driver;

   /**
    * DB connection url;
    */
   private final String url;

   /**
    * DB connection user name;
    */
   private final String username;

   /**
    * User's password.
    */
   private final String password;

   /**
    * DBConnectionInfo constructor.
    * 
    * @param driver
    *          driver class name
    * @param url
    *          db connection url
    * @param username
    *          db connection user name
    * @param password
    *          user's password      
    */
   public DBConnectionInfo(String driver, String url, String username, String password)
   {
      this.driver = driver;
      this.url = url;
      this.username = username;
      this.password = password;
   }

   /**
    * @return the driver
    */
   public String getDriver()
   {
      return driver;
   }

   /**
    * @return the url
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * @return the username
    */
   public String getUsername()
   {
      return username;
   }

   /**
    * @return the password
    */
   public String getPassword()
   {
      return password;
   }

}
