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
   private static final String DB_CREATOR_PROPERTIES = "db-creator-properties";

   private static final String DRIVER_PROPERTY = "driverClassName";

   private static final String URL_PROPERTY = "url";

   private static final String USERNAME_PROPERTY = "username";

   private static final String PASSWORD_PROPERTY = "password";

   private static final String SCRIPT_PROPERTY = "script";

   // TODO javaDoc
   private final String driver;

   private final String url;

   private final String userName;

   private final String password;

   private final String script;

   /**
    * DBCreatorImpl constructor.
    * 
    * @param contextInit
    *          Initial context initializer
    * @param params
    *          Initializations params
    */
   public DBCreatorImpl(InitParams params)
   {
      // TODO null checks
      PropertiesParam prop = params.getPropertiesParam(DB_CREATOR_PROPERTIES);

      this.driver = prop.getProperty(DRIVER_PROPERTY);
      this.url = prop.getProperty(URL_PROPERTY);
      this.userName = prop.getProperty(USERNAME_PROPERTY);
      this.password = prop.getProperty(PASSWORD_PROPERTY);
      this.script = prop.getProperty(SCRIPT_PROPERTY);
   }

   /**
    * {@inheritDoc}
    */
   public void create(String dbName, String _userName, String _password) throws DBCreationException
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

      String sql;
      try
      {
         sql = readScriptResource(script);
      }
      catch (IOException e)
      {
         throw new DBCreationException("Can't read SQL script resource " + script, e);
      }

      sql = sql.replace(DBCreator.DATABASE_TEMPLATE, dbName);
      sql = sql.replace(DBCreator.USERNAME_TEMPLATE, _userName);
      sql = sql.replace(DBCreator.PASSWORD_TEMPLATE, _password);

      try
      {
         conn.setAutoCommit(false);
         conn.createStatement().executeUpdate(sql);
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
            throw new DBCreationException("Can't perform rollback", e);
         }
         throw new DBCreationException("Can't execute SQL script " + sql, e);
      }
   }

   /**
    * Read SQL script from file resource.
    */
   protected String readScriptResource(String path) throws IOException
   {
      //      InputStream is = this.getClass().getResourceAsStream(path);
      // TODO
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
         try
         {
            is.close();
         }
         catch (IOException e)
         {
         }
      }
   }
}
