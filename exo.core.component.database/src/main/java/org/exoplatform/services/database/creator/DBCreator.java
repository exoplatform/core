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

import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id$
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
    * Server url.
    */
   protected final String serverUrl;

   /**
    * User name with administrative rights for connection to server.
    */
   protected final String adminName;

   /**
    * User's password.
    */
   protected final String adminPwd;

   /**
    * Internal login connection property needed for Oracle.
    */
   protected final String internal_logon;

   /**
    * DDL script database creation.
    */
   protected final String dbScript;

   /**
    * User name for new database.
    */
   protected final String dbUserName;

   /**
    * User's password.
    */
   protected final String dbPassword;

   /**
    * Additional connection properties.
    */
   protected final Map<String, String> additionalProperties;

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
         if (driver == null)
         {
            throw new ConfigurationException("driverClassName expected in db-connection properties section");
         }

         this.serverUrl = prop.getProperty("url");
         if (serverUrl == null)
         {
            throw new ConfigurationException("url expected in db-connection properties section");
         }

         this.adminName = prop.getProperty("username");
         if (adminName == null)
         {
            throw new ConfigurationException("username expected in db-connection properties section");
         }

         this.adminPwd = prop.getProperty("password");
         if (adminPwd == null)
         {
            throw new ConfigurationException("password expected in db-connection properties section");
         }

         this.internal_logon = prop.getProperty("internal_logon");

         // Store additional properties into map          
         Iterator<Property> pit = prop.getPropertyIterator();

         additionalProperties = new HashMap<String, String>();

         while (pit.hasNext())
         {
            Property p = pit.next();
            String name = p.getName();
            if (name.equalsIgnoreCase("driverClassName") || name.equalsIgnoreCase("url")
               || name.equalsIgnoreCase("username") || name.equalsIgnoreCase("password")
               || name.equalsIgnoreCase("internal_logon"))
            {
               continue;
            }
            additionalProperties.put(name, p.getValue());
         }
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
            throw new ConfigurationException("scriptPath expected in db-creation properties section");
         }

         this.dbUserName = prop.getProperty("username");
         if (dbUserName == null)
         {
            throw new ConfigurationException("username expected in db-creation properties section");
         }

         this.dbPassword = prop.getProperty("password");
         if (dbPassword == null)
         {
            throw new ConfigurationException("password expected in db-creation properties section");
         }
      }
      else
      {
         throw new ConfigurationException("db-creation properties expected in initializations parameters");
      }
   }

   /**
    * Execute DDL script for new database creation. Database name are passed as parameter, 
    * user name and password are passed via configuration. In script database name, user name 
    * and password defined via templates as ${database}, ${username} and ${password} respectively.
    * At execution time method replaces templates by real values.
    * 
    * @param dbName
    *          new database name
    * @throws DBCreatorException
    *          if any error occurs 
    */
   public DBConnectionInfo createDatabase(String dbName) throws DBCreatorException
   {
      Connection conn = null;
      try
      {
         Class.forName(driver);

         conn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
         {
            public Connection run() throws Exception
            {
               return DriverManager.getConnection(serverUrl, adminName, adminPwd);
            }
         });
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't establish the JDBC connection to database " + serverUrl, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreatorException("Can't load the JDBC driver " + driver, e);
      }

      String dbProductName;
      try
      {
         final Connection connection = conn;
         dbProductName = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<String>()
         {
            public String run() throws Exception
            {
               return connection.getMetaData().getDatabaseProductName();
            }
         });

         if (dbProductName.startsWith("Microsoft SQL Server") || dbProductName.startsWith("Adaptive Server Anywhere")
            || dbProductName.equals("Sybase SQL Server") || dbProductName.equals("Adaptive Server Enterprise"))
         {
            executeAutoCommitMode(conn, dbName);
         }
         else
         {
            executeBatchMode(conn, dbName);
         }
      }
      catch (SQLException e)
      {
         String errorTrace = "";
         while (e != null)
         {
            errorTrace += e.getMessage() + "; ";
            e = e.getNextException();
         }

         throw new DBCreatorException("Can't execute SQL script " + errorTrace);
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

      return constructDBConnectionInfo(dbName, dbProductName);
   }

   /**
    * Get database connection info.
    * 
    * @param dbName
    *          new database name
    * @throws DBCreatorException
    *          if any error occurs 
    */
   public DBConnectionInfo getDBConnectionInfo(String dbName) throws DBCreatorException
   {
      Connection conn = null;
      try
      {
         Class.forName(driver);

         conn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
         {
            public Connection run() throws Exception
            {
               return DriverManager.getConnection(serverUrl, adminName, adminPwd);
            }
         });
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't establish the JDBC connection to database " + serverUrl, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreatorException("Can't load the JDBC driver " + driver, e);
      }

      String dbProductName;
      try
      {
         final Connection connection = conn;
         dbProductName = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<String>()
         {
            public String run() throws Exception
            {
               return connection.getMetaData().getDatabaseProductName();
            }
         });
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't resolve database product name ", e);
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

      return constructDBConnectionInfo(dbName, dbProductName);
   }

   /**
    * Executes DDL script in generic batch mode.
    * 
    * @param conn
    *          connection to server
    * @param dbName
    *          database name
    * @throws SQLException
    *          if any errors occurs
    */
   private void executeBatchMode(Connection conn, String dbName) throws SQLException
   {
      Statement statement = conn.createStatement();
      for (String scr : dbScript.split(";"))
      {
         scr = scr.replace(DATABASE_TEMPLATE, dbName);
         scr = scr.replace(USERNAME_TEMPLATE, dbUserName);
         scr = scr.replace(PASSWORD_TEMPLATE, dbPassword);

         String s = cleanWhitespaces(scr.trim());
         if (s.length() > 0)
         {
            statement.addBatch(s);
         }
      }
      statement.executeBatch();
   }

   /**
    * Construct database url connection depending on specific database.
    * 
    * @param dbName
    *          database name
    * @param dbProductName
    *          database product name
    * @return DBConnectionInfo
    */
   private DBConnectionInfo constructDBConnectionInfo(String dbName, String dbProductName)
   {
      String dbUrl = serverUrl;
      if (dbProductName.startsWith("Microsoft SQL Server"))
      {
         dbUrl = dbUrl + (dbUrl.endsWith(";") ? "" : ";") + "databaseName=" + dbName + ";";
      }
      else if (dbProductName.equals("Oracle"))
      {
         // do nothing
      }
      else
      {
         dbUrl = dbUrl + (dbUrl.endsWith("/") ? "" : "/") + dbName;
      }

      return new DBConnectionInfo(driver, dbUrl, dbUserName, dbPassword, additionalProperties);
   }

   /**
    * Executes DDL script with autocommit mode set true. Actually need for MSSQL and Sybase database servers.
    * After execution "create database" command newly created database not available for "use" command and
    * therefore you can't create user inside.  
    * 
    * @param conn
    *          connection to server
    * @param dbName
    *          database name
    * @throws SQLException
    *          if any errors occurs
    */
   private void executeAutoCommitMode(Connection conn, String dbName) throws SQLException
   {
      conn.setAutoCommit(true);
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
   }

   /**
    * Read SQL script from file resource.
    */
   protected String readScriptResource(String path) throws IOException
   {
      InputStream is = PrivilegedFileHelper.fileInputStream(path);
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
