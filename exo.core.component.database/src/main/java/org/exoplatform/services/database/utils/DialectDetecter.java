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
package org.exoplatform.services.database.utils;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.security.PrivilegedExceptionAction;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * JDBC dialect detecter based on database metadata and vendor product name.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id:DialectDetecter.java 1111 2010-01-01 00:00:01Z pnedonosko $
 */
public class DialectDetecter
{

   /**
    * Logger.
    */
   private final static Log LOG = ExoLogger.getLogger("exo.core.component.database.DialectDetecter");

   /**
    * Detect databse dialect using JDBC metadata. Based on code of 
    * http://svn.jboss.org/repos/hibernate/core/trunk/core/src/main/java/org/hibernate/
    * dialect/resolver/StandardDialectResolver.java 
    * 
    * @param metaData {@link DatabaseMetaData} 
    * @return String
    * @throws SQLException if error occurs
    */
   public static String detect(final DatabaseMetaData metaData) throws SQLException
   {
      final String databaseName =
         SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<String>()
         {
            public String run() throws Exception
            {
               return metaData.getDatabaseProductName();
            }
         });

      if ("HSQL Database Engine".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_HSQLDB;
      }

      if ("H2".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_H2;
      }

      if ("MySQL".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_MYSQL;
      }

      if ("PostgreSQL".equals(databaseName))
      {
         int majorVersion = metaData.getDatabaseMajorVersion();
         int minorVersion = metaData.getDatabaseMinorVersion();

         return (majorVersion > 9 || (majorVersion == 9 && minorVersion >= 1)) ? DialectConstants.DB_DIALECT_PGSQL_SCS
            : DialectConstants.DB_DIALECT_PGSQL;
      }

      if ("EnterpriseDB".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_PGSQL_SCS;
      }

      if ("Apache Derby".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_DERBY;
      }

      if ("ingres".equalsIgnoreCase(databaseName))
      {
         return DialectConstants.DB_DIALECT_INGRES;
      }

      if (databaseName.startsWith("Microsoft SQL Server"))
      {
         return DialectConstants.DB_DIALECT_MSSQL;
      }

      if ("Sybase SQL Server".equals(databaseName) || "Adaptive Server Enterprise".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_SYBASE;
      }

      if (databaseName.startsWith("Adaptive Server Anywhere"))
      {
         return DialectConstants.DB_DIALECT_SYBASE;
      }

      if (databaseName.startsWith("DB2/"))
      {
         return detectDB2Dialect(metaData);
      }

      if ("Oracle".equals(databaseName))
      {
         return DialectConstants.DB_DIALECT_ORACLE;
      }

      return DialectConstants.DB_DIALECT_GENERIC;
   }

   /**
    * Detects DB2 dialect.
    */
   private static String detectDB2Dialect(final DatabaseMetaData metaData) throws SQLException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("DB Major version = " + metaData.getDatabaseMajorVersion() + ", DB Minor version = "
            + metaData.getDatabaseMinorVersion() + ", DB Product version = " + metaData.getDatabaseProductVersion());
      }

      int majorVersion = metaData.getDatabaseMajorVersion();
      if (majorVersion < 9)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("The dialect " + DialectConstants.DB_DIALECT_DB2V8
               + " will be used as the major version is lower than 9.");
         }

         return DialectConstants.DB_DIALECT_DB2V8;
      }
      else
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("The dialect " + DialectConstants.DB_DIALECT_DB2
               + " will be used as the major version is greater or equal to 9");
         }

         return DialectConstants.DB_DIALECT_DB2;
      }
   }

}
