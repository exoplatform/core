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

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: DBCreator.java 111 2010-11-11 11:11:11Z tolusha $
 */
public class DBCreator
{

   /**
    * Database template.
    */
   public static final String DATABASE_TEMPLATE = "${database}";

   /**
    * User name template.
    */
   public static final String USERNAME_TEMPLATE = "${username}";

   /**
    * Password template.
    */
   public static final String PASSWORD_TEMPLATE = "${password}";

   /**
    * Driver class name.
    */
   protected final String driver;

   /**
    * Database url.
    */
   protected final String url;

   /**
    * SA user name.
    */
   protected final String userName;

   /**
    * SA user's password.
    */
   protected final String password;

   /**
    * DB script creation.
    */
   protected final String dbScript;

   /**
    * User name for new DB.
    */
   protected final String dbUserName;

   /**
    * User's password.
    */
   protected final String dbPassword;

   /**
    * DBCreator constructor.
    * 
    * @param params
    *          Initializations parameters
    */
   public DBCreator(InitParams params) throws ConfigurationException
   {
      if (params == null)
      {
         throw new ConfigurationException("Initializations parameters expected");
      }

      PropertiesParam prop = params.getPropertiesParam("db-connection");

      if (prop != null)
      {
         this.driver = prop.getProperty("driverClassName");
         this.url = prop.getProperty("url");
         this.userName = prop.getProperty("username");
         this.password = prop.getProperty("password");
      }
      else
      {
         throw new ConfigurationException("db-connection properties expected in initializations parameters");
      }

      prop = params.getPropertiesParam("db-creation");

      if (prop != null)
      {
         String scriptPath = prop.getProperty("scriptPath");
         if (scriptPath != null)
         {
            try
            {
               dbScript = readScriptResource(scriptPath);
            }
            catch (IOException e)
            {
               throw new ConfigurationException("Can't read script resource " + scriptPath, e);
            }
         }
         else
         {
            throw new ConfigurationException("scriptPath expected in initializations parameters");
         }

         this.dbUserName = prop.getProperty("username");
         this.dbPassword = prop.getProperty("password");
      }
      else
      {
         throw new ConfigurationException("db-creation properties  expected in initializations parameters");
      }

   }

   /**
    * Create database using predefined SQL script. Database name passed as parameter, 
    * user name and password passed via configuration. In SQL script database name, user name 
    * and password defined via templates as ${database}, ${username} and ${password} respectively.
    * 
    * @param dbName
    *          new database name
    * @throws DBCreatorException
    *          if any error occurs 
    */
   public void create(String dbName) throws DBCreatorException
   {
      Connection conn = null;
      try
      {
         Class.forName(driver);
         conn = DriverManager.getConnection(url, userName, password);
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't establish the JDBC connection to database " + url, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreatorException("Can't load the JDBC driver " + driver, e);
      }

      try
      {
         conn.setAutoCommit(false);

         for (String scr : dbScript.split(";"))
         {
            scr = scr.replace(DATABASE_TEMPLATE, dbName);
            scr = scr.replace(USERNAME_TEMPLATE, dbUserName);
            scr = scr.replace(PASSWORD_TEMPLATE, dbPassword);

            String s = cleanWhitespaces(scr.trim());
            if (s.length() > 0)
            {
               conn.createStatement().executeUpdate(s);
            }
         }
         conn.commit();
      }
      catch (SQLException e)
      {
         try
         {
            conn.rollback();
         }
         catch (SQLException e1)
         {
            throw new DBCreatorException("Can't perform rollback", e1);
         }
         throw new DBCreatorException("Can't execute SQL script", e);
      }
      finally
      {
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
            throw new DBCreatorException("Can't close connection", e);
         }
      }
   }

   /**
    * Read SQL script from file resource.
    */
   protected String readScriptResource(String path) throws IOException
   {
      InputStream is = new FileInputStream(path);
      InputStreamReader isr = new InputStreamReader(is);
      try
      {
         StringBuilder sbuff = new StringBuilder();
         char[] buff = new char[is.available()];
         int r = 0;
         while ((r = isr.read(buff)) > 0)
         {
            sbuff.append(buff, 0, r);
         }

         return sbuff.toString();
      }
      finally
      {
         is.close();
      }
   }

   /**
    * Clean whitespace.
    */
   private String cleanWhitespaces(String string)
   {
      if (string != null)
      {
         char[] cc = string.toCharArray();
         for (int ci = cc.length - 1; ci > 0; ci--)
         {
            if (Character.isWhitespace(cc[ci]))
            {
               cc[ci] = ' ';
            }
         }
         return new String(cc);
      }
      return string;
   }
}
