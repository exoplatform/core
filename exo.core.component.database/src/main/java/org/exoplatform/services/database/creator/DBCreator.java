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

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.services.database.utils.DialectConstants;
import org.exoplatform.services.database.utils.DialectDetecter;
import org.exoplatform.services.database.utils.JDBCUtils;

import java.io.IOException;
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

   private final static String CONNECTION_PROPERTIES = "db-connection";

   private final static String DRIVER_NAME = "driverClassName";

   private final static String SERVER_URL = "url";

   private final static String USERNAME = "username";

   private final static String PASSWORD = "password";

   private final static String DB_CREATION_PROPERTIES = "db-creation";

   private final static String DB_SCRIPT_PATH = "scriptPath";

   private final static String DB_USERNAME = "username";

   private final static String DB_PASSWORD = "password";

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
    * @configurationManager 
    *          configuration manager instance          
    */
   public DBCreator(String serverUrl, Map<String, String> connectionProperties, String scriptPath, String dbUserName,
      String dbPassword, ConfigurationManager cm) throws ConfigurationException
   {
      this.serverUrl = serverUrl;
      this.connectionProperties = connectionProperties;
      this.dbUserName = dbUserName;
      this.dbPassword = dbPassword;
      this.dbScript = readScript(scriptPath, cm);
   }

   /**
    * DBCreator constructor.
    * 
    * @param params
    *          Initializations parameters
    * @configurationManager 
    *          configuration manager instance          
    */
   public DBCreator(InitParams params, ConfigurationManager cm) throws ConfigurationException
   {
      if (params == null)
      {
         throw new ConfigurationException("Initializations parameters expected");
      }

      PropertiesParam prop = params.getPropertiesParam(CONNECTION_PROPERTIES);

      if (prop != null)
      {
         if (prop.getProperty(DRIVER_NAME) == null)
         {
            throw new ConfigurationException("driverClassName expected in db-connection properties section");
         }

         serverUrl = prop.getProperty(SERVER_URL);
         if (serverUrl == null)
         {
            throw new ConfigurationException("url expected in db-connection properties section");
         }

         if (prop.getProperty(USERNAME) == null)
         {
            throw new ConfigurationException("username expected in db-connection properties section");
         }

         if (prop.getProperty(PASSWORD) == null)
         {
            throw new ConfigurationException("password expected in db-connection properties section");
         }

         // Store all connection properties into single map          
         Iterator<Property> pit = prop.getPropertyIterator();
         connectionProperties = new HashMap<String, String>();
         while (pit.hasNext())
         {
            Property p = pit.next();
            if (!p.getName().equalsIgnoreCase(SERVER_URL))
            {
               connectionProperties.put(p.getName(), p.getValue());
            }
         }
      }
      else
      {
         throw new ConfigurationException("db-connection properties expected in initializations parameters");
      }

      prop = params.getPropertiesParam(DB_CREATION_PROPERTIES);
      if (prop != null)
      {
         String scriptPath = prop.getProperty(DB_SCRIPT_PATH);
         if (scriptPath != null)
         {
            this.dbScript = readScript(scriptPath, cm);
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
   public DBConnectionInfo createDatabase(final String dbName) throws DBCreatorException
   {
      Connection conn = openConnection();
      try
      {
         String dialect = DialectDetecter.detect(conn.getMetaData());

         if (dialect.equalsIgnoreCase(DialectConstants.DB_DIALECT_MSSQL)
            || dialect.equalsIgnoreCase(DialectConstants.DB_DIALECT_SYBASE))
         {
            executeInAutoCommitMode(conn, dbName);
         }
         else
         {
            executeInBatchMode(conn, dbName);
         }

         return constructDBConnectionInfo(dbName, dialect);
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't execute SQL script : " + JDBCUtils.getFullMessage(e), e);
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
    * Get database connection info.
    * 
    * @param dbName
    *          new database name
    * @throws DBCreatorException
    *          if any error occurs or database is not available
    */
   public DBConnectionInfo getDBConnectionInfo(String dbName) throws DBCreatorException
   {
      Connection conn = openConnection();
      try
      {
         return constructDBConnectionInfo(dbName, DialectDetecter.detect(conn.getMetaData()));
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can not get database connection information", e);
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
    * Executes DDL script in generic batch mode.
    * 
    * @param conn
    *          connection to server
    * @param dbName
    *          database name
    * @throws SQLException
    *          if any errors occurs
    */
   private void executeInBatchMode(Connection conn, String dbName) throws SQLException
   {
      Statement statement = conn.createStatement();
      for (String scr : dbScript.split(";"))
      {
         scr = scr.replace(DATABASE_TEMPLATE, dbName);
         scr = scr.replace(USERNAME_TEMPLATE, dbUserName);
         scr = scr.replace(PASSWORD_TEMPLATE, dbPassword);

         String s = JDBCUtils.cleanWhitespaces(scr.trim());
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
    * @param dialect
    *          dialect
    * @param serverUrl
    *          url to DB server
    * @param connectionProperties
    *          connection properties         
    * @return DBConnectionInfo
    */
   private DBConnectionInfo constructDBConnectionInfo(String dbName, String dialect)
   {
      StringBuilder dbUrl = new StringBuilder(serverUrl);

      if (dialect.equalsIgnoreCase(DialectConstants.DB_DIALECT_MSSQL))
      {
         dbUrl.append(serverUrl.endsWith(";") ? "" : ";");
         dbUrl.append("databaseName=");
         dbUrl.append(dbName);
         dbUrl.append(";");
      }
      else if (dialect.equalsIgnoreCase(DialectConstants.DB_DIALECT_ORACLE)
         || dialect.equalsIgnoreCase(DialectConstants.DB_DIALECT_ORACLEOCI))
      {
         // do nothing
      }
      else
      {
         dbUrl.append(serverUrl.endsWith("/") ? "" : "/");
         dbUrl.append(dbName);
      }

      // clone connection properties
      Map<String, String> connProperties = new HashMap<String, String>();

      for (Entry<String, String> entry : connectionProperties.entrySet())
      {
         connProperties.put(entry.getKey(), entry.getValue());
      }

      // add url to database
      connProperties.put(SERVER_URL, dbUrl.toString());

      return new DBConnectionInfo(dbName, connProperties);
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
   private void executeInAutoCommitMode(Connection conn, String dbName) throws SQLException
   {
      conn.setAutoCommit(true);
      for (String scr : dbScript.split(";"))
      {
         scr = scr.replace(DATABASE_TEMPLATE, dbName);
         scr = scr.replace(USERNAME_TEMPLATE, dbUserName);
         scr = scr.replace(PASSWORD_TEMPLATE, dbPassword);

         String s = JDBCUtils.cleanWhitespaces(scr.trim());
         if (s.length() > 0)
         {
            conn.createStatement().executeUpdate(s);
         }
      }
   }

   /**
    * Read script resource.
    * 
    * @param scriptPath
    *          path to the script
    * @param cm
    *          the configuration manager will help to find script in jars          
    * @return
    *       script content
    * @throws ConfigurationException 
    *          if script not found
    */
   private String readScript(String scriptPath, ConfigurationManager cm) throws ConfigurationException
   {
      try
      {
         return IOUtil.getStreamContentAsString(cm.getInputStream(scriptPath));
      }
      catch (Exception e)
      {
         try
         {
            return IOUtil.getFileContentAsString(scriptPath);
         }
         catch (IOException ioe)
         {
            throw new ConfigurationException("Can't read script at " + scriptPath, e);
         }
      }
   }

   /**
    * Open connection to the DB.
    * 
    * @param connectionProperties
    *          connection properties
    * @return connection
    * @throws DBCreatorException
    *          if can't establish connection to DB
    */
   private Connection openConnection() throws DBCreatorException
   {
      Connection conn = null;
      try
      {
         ClassLoading.forName(connectionProperties.get(DRIVER_NAME), this);

         conn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
         {
            public Connection run() throws Exception
            {
               return DriverManager.getConnection(serverUrl, connectionProperties.get(USERNAME),
                  connectionProperties.get(PASSWORD));
            }
         });

         return conn;
      }
      catch (SQLException e)
      {
         throw new DBCreatorException("Can't establish the JDBC connection to database " + serverUrl, e);
      }
      catch (ClassNotFoundException e)
      {
         throw new DBCreatorException("Can't load the JDBC driver " + connectionProperties.get(DRIVER_NAME), e);
      }
   }
}
