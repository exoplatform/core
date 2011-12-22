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
package org.exoplatform.services.database;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.database.table.IDGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Apr 4, 2006 This class is a wrapper class for
 * the java.sql.Datasource class. In additional to the java.sql.Datasource
 * method such getConnection().. The ExoDatasource provice 2 other methods:
 * DBTableManager getDBTableManager and IDGenerator getIDGenerator()
 */
public class ExoDatasource
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.database.ExoDatasource");

   final public static int STANDARD_DB_TYPE = 0;

   final public static int HSQL_DB_TYPE = 1;

   final public static int MYSQL_DB_TYPE = 2;

   final public static int DB2_DB_TYPE = 3;

   final public static int DERBY_DB_TYPE = 4;

   final public static int ORACLE_DB_TYPE = 5;

   final public static int SQL_SERVER_TYPE = 6;

   static int totalGetConnect = 0;

   // static int totalCommit = 0;
   // static int totalCloseConnect = 0;
   final public static int MSSQL_DB_TYPE = 6;

   final public static int SYSBASE_DB_TYPE = 7;

   final public static int POSTGRES_DB_TYPE = 8;

   private DataSource xaDatasource_;

   private DBTableManager tableManager_;

   private IDGenerator idGenerator_;

   private QueryBuilder queryManager_;

   private String databaseName_;

   private String databaseVersion_;

   private int dbType_ = STANDARD_DB_TYPE;

   Connection conn;

   /**
    * The constructor should: 1. Keep track of the datasource object 2. Create
    * the DBTableManager object base on the datasource information such database
    * type , version 3. Create an IDGenerator for the datasource
    * 
    * @param ds
    * @throws Exception
    */
   public ExoDatasource(final DataSource ds) throws Exception
   {
      xaDatasource_ = ds;
      DatabaseMetaData metaData =
         SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<DatabaseMetaData>()
         {
            public DatabaseMetaData run() throws SQLException
            {
               return ds.getConnection().getMetaData();
            }
         });
      
      databaseName_ = metaData.getDatabaseProductName();
      databaseVersion_ = metaData.getDatabaseProductVersion();

      String dbname = databaseName_.toLowerCase();
      LOG.info("\n\n\n\n------->DB Name: " + dbname + "\n\n\n\n");
      if (dbname.indexOf("oracle") >= 0)
      {
         dbType_ = ORACLE_DB_TYPE;
      }
      else if (dbname.indexOf("hsql") >= 0)
      {
         dbType_ = HSQL_DB_TYPE;
      }
      else if (dbname.indexOf("mysql") >= 0)
      {
         dbType_ = MYSQL_DB_TYPE;
      }
      else if (dbname.indexOf("derby") >= 0)
      {
         dbType_ = DERBY_DB_TYPE;
      }
      else if (dbname.indexOf("db2") >= 0)
      {
         dbType_ = DB2_DB_TYPE;
      }
      else if (dbname.indexOf("server") >= 0)
      {
         dbType_ = SQL_SERVER_TYPE;
      }
      else
      {
         dbType_ = STANDARD_DB_TYPE;
      }

      tableManager_ = DBTableManager.createDBTableManager(this);
      idGenerator_ = new IDGenerator(this);
      queryManager_ = new QueryBuilder(dbType_);
   }

   /**
    * This method should return the real Datasource object
    * 
    * @return
    */
   public DataSource getDatasource()
   {
      return xaDatasource_;
   }

   /**
    * This method should call the datasource getConnection method and return the
    * Connection object. The developer can add some debug code or broadcast an
    * event here.
    * 
    * @return
    * @throws Exception
    */
   public Connection getConnection() throws Exception
   {
      return xaDatasource_.getConnection();
   }

   /**
    * This method should delegate to the method close of the Connection object.
    * The developer can add debug or broadcast an event here.
    * 
    * @param conn
    * @throws Exception
    */
   public void closeConnection(Connection conn) throws Exception
   {
      // long startGet = System.currentTimeMillis();
      conn.close();
      // totalCloseConnect += System.currentTimeMillis() - startGet;
      // System.out.println(" \n\n\n == > total time to Close connection "+
      // totalCloseConnect+"\n\n");
   }

   /**
    * This method should delegate to the commit() method of the Connection
    * object. The developer can add the debug code here
    * 
    * @param conn
    * @throws Exception
    */
   public void commit(Connection conn) throws Exception
   {
      // long startGet = System.currentTimeMillis();
      conn.setAutoCommit(false);
      conn.commit();
      // totalCommit += System.currentTimeMillis() - startGet;
      //System.out.println(" \n\n\n == > total time to Commit "+totalCommit+"\n\n"
      // );
   }

   /**
    * This method should return the DBTableManager object. The DBTableManager
    * object should be initialized in the constructor according to the database
    * type and version
    * 
    * @return
    */
   public DBTableManager getDBTableManager()
   {
      return tableManager_;
   }

   /**
    * This mthod should return the IDGenerator object, the developer can use the
    * id generator to generate an unique long id for an db object
    * 
    * @return
    */
   public IDGenerator getIDGenerator()
   {
      return idGenerator_;
   }

   public int getDatabaseType()
   {
      return dbType_;
   }

   public String getDatabaseName()
   {
      return databaseName_;
   }

   public String getDatabaseVersion()
   {
      return databaseVersion_;
   }

   public QueryBuilder getQueryBuilder()
   {
      return queryManager_;
   }
}
