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
 * @version $Id: $
 */
public class DBCreatorImpl implements DBCreator
{
   public static final String SQL_DELIMITER = ";";

   public static final String SQL_DELIMITER_COMMENT_PREFIX = "/*$DELIMITER:";

   private static final String DB_CONNECTION_PROPERTIES = "db-connection";

   private static final String DB_CREATION_PROPERTIES = "db-creation";

   private static final String DRIVER_PROPERTY = "driverClassName";

   private static final String URL_PROPERTY = "url";

   private static final String SCRIPT_PATH_PROPERTY = "scriptPath";

   private static final String USERNAME_PROPERTY = "username";

   private static final String PASSWORD_PROPERTY = "password";

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
    * DBCreatorImpl constructor.
    * 
    * @param contextInit
    *          Initial context initializer
    * @param params
    *          Initializations parameters
    */
   public DBCreatorImpl(InitParams params) throws ConfigurationException
   {
      if (params == null)
      {
         throw new ConfigurationException("Initializations parameters expected");
      }

      PropertiesParam prop = params.getPropertiesParam(DB_CONNECTION_PROPERTIES);

      if (prop != null)
      {
         this.driver = prop.getProperty(DRIVER_PROPERTY);
         this.url = prop.getProperty(URL_PROPERTY);
         this.userName = prop.getProperty(USERNAME_PROPERTY);
         this.password = prop.getProperty(PASSWORD_PROPERTY);
      }
      else
      {
         throw new ConfigurationException(DB_CONNECTION_PROPERTIES + " expected in initializations parameters");
      }

      prop = params.getPropertiesParam(DB_CREATION_PROPERTIES);

      if (prop != null)
      {
         String scriptPath = prop.getProperty(SCRIPT_PATH_PROPERTY);
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
            throw new ConfigurationException(SCRIPT_PATH_PROPERTY + " expected in initializations parameters");
         }

         this.dbUserName = prop.getProperty(USERNAME_PROPERTY);
         this.dbPassword = prop.getProperty(PASSWORD_PROPERTY);
      }
      else
      {
         throw new ConfigurationException(DB_CREATION_PROPERTIES + " expected in initializations parameters");
      }

   }

   /**
    * {@inheritDoc}
    */
   public void create(String dbName) throws DBCreationException
   {
      Connection conn = null;
      try
      {
         Class.forName(driver);
         conn = DriverManager.getConnection(url, userName, password);
      }
      catch (SQLException e)
      {
         throw new DBCreationException("Can't establish the JDBC connection to database " + url, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreationException("Can't load the JDBC driver " + driver, e);
      }

      try
      {
         conn.setAutoCommit(false);

         for (String scr : dbScript.split(SQL_DELIMITER))
         {
            scr = scr.replace(DBCreator.DATABASE_TEMPLATE, dbName);
            scr = scr.replace(DBCreator.USERNAME_TEMPLATE, dbUserName);
            scr = scr.replace(DBCreator.PASSWORD_TEMPLATE, dbPassword);

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
            throw new DBCreationException("Can't perform rollback", e1);
         }
         throw new DBCreationException("Can't execute SQL script", e);
      }
      finally
      {
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
            throw new DBCreationException("Can't close connection", e);
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
