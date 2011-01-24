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
import java.util.Map.Entry;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id$
 */
public class DBCreator
{

   private final static String DB_CONNECTION = "db-connection";

   private final static String DB_DRIVER = "driverClassName";

   private final static String DB_URL = "url";

   private final static String DB_USERNAME = "username";

   private final static String DB_PASSWORD = "password";

   private final static String DB_ORCL_INTERNAL_LOGON = "internal_logon";

   private final static String DB_CREATION = "db-creation";

   private final static String DB_SCRIPT_PATH = "scriptPath";

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
    * Server url.
    */
   protected final String serverUrl;

   /**
    * Connection properties.
    */
   protected final Map<String, String> connectionProperties;

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

      PropertiesParam prop = params.getPropertiesParam(DB_CONNECTION);

      if (prop != null)
      {
         if (prop.getProperty(DB_DRIVER) == null)
         {
            throw new ConfigurationException("driverClassName expected in db-connection properties section");
         }

         serverUrl = prop.getProperty(DB_URL);
         if (serverUrl == null)
         {
            throw new ConfigurationException("url expected in db-connection properties section");
         }

         if (prop.getProperty(DB_USERNAME) == null)
         {
            throw new ConfigurationException("username expected in db-connection properties section");
         }

         if (prop.getProperty(DB_PASSWORD) == null)
         {
            throw new ConfigurationException("password expected in db-connection properties section");
         }

         // Store all connection properties into single map          
         Iterator<Property> pit = prop.getPropertyIterator();
         connectionProperties = new HashMap<String, String>();
         while (pit.hasNext())
         {
            Property p = pit.next();
            if (!p.getName().equalsIgnoreCase(DB_URL))
            {
               connectionProperties.put(p.getName(), p.getValue());
            }
         }
      }
      else
      {
         throw new ConfigurationException("db-connection properties expected in initializations parameters");
      }

      prop = params.getPropertiesParam(DB_CREATION);
      if (prop != null)
      {
         String scriptPath = prop.getProperty(DB_SCRIPT_PATH);
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

         this.dbUserName = prop.getProperty(DB_USERNAME);
         if (dbUserName == null)
         {
            throw new ConfigurationException("username expected in db-creation properties section");
         }

         this.dbPassword = prop.getProperty(DB_PASSWORD);
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
         Class.forName(connectionProperties.get(DB_DRIVER));

         conn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
         {
            public Connection run() throws Exception
            {
               return DriverManager.getConnection(serverUrl, connectionProperties.get(DB_USERNAME),
                  connectionProperties.get(DB_PASSWORD));
            }
         });
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't establish the JDBC connection to database " + serverUrl, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreatorException("Can't load the JDBC driver " + connectionProperties.get(DB_DRIVER), e);
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
         Class.forName(connectionProperties.get(DB_DRIVER));

         conn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
         {
            public Connection run() throws Exception
            {
               return DriverManager.getConnection(serverUrl, connectionProperties.get(DB_USERNAME),
                  connectionProperties.get(DB_PASSWORD));
            }
         });
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't establish the JDBC connection to database " + serverUrl, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreatorException("Can't load the JDBC driver " + connectionProperties.get(DB_DRIVER), e);
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

      // clone connection properties
      Map<String, String> connProperties = new HashMap<String, String>();

      for (Entry<String, String> entry : this.connectionProperties.entrySet())
      {
         connProperties.put(entry.getKey(), entry.getValue());
      }

      connProperties.put(DB_URL, dbUrl);

      return new DBConnectionInfo(connProperties);
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
