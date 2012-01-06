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
package org.exoplatform.services.database.jdbc;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.database.utils.JDBCUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: DBSchemaCreator.java 13053 2007-03-01 06:44:00Z tuan08 $
 */

public class DBSchemaCreator
{
   static private String SQL_ALREADYEXISTS = ".*((already exist)|(duplicate key)| (already used)|(ORA-00955))+.*";

   private final Pattern pattern;

   private static Log log = ExoLogger.getLogger("exo.core.component.database.DBSchemaCreator");

   private List<CreateDBSchemaPlugin> createDBSchemaPlugins = new ArrayList<CreateDBSchemaPlugin>();

   @SuppressWarnings("unused")
   public DBSchemaCreator(InitialContextInitializer contextInit)
   {
      pattern = Pattern.compile(SQL_ALREADYEXISTS, Pattern.CASE_INSENSITIVE);
   }

   // for testing only
   private DBSchemaCreator(String dsName, String script) throws SQLException, NamingException
   {
      pattern = Pattern.compile(SQL_ALREADYEXISTS, Pattern.CASE_INSENSITIVE);
      createTables(dsName, script);
   }

   public void createTables(String dsName, String script) throws NamingException, SQLException
   {
      InitialContext context = new InitialContext();
      DataSource ds = (DataSource)context.lookup(dsName);
      Connection conn = ds.getConnection();
      String sql = "";
      try
      {
         String[] scripts = JDBCUtils.splitWithSQLDelimiter(script);

         for (String scr : scripts)
         {
            String s = JDBCUtils.cleanWhitespaces(scr.trim());

            if (s.length() < 1)
               continue;
            sql = s;
            if (log.isDebugEnabled())
               log.debug("Execute script: \n[" + sql + "]");

            try
            {
               conn.setAutoCommit(false);
               conn.createStatement().executeUpdate(sql);
               conn.commit();
            }
            catch (SQLException e)
            {
               conn.rollback();
               // already exists check
               Matcher aeMatcher = pattern.matcher(e.getMessage().trim());
               if (!aeMatcher.matches())
                  throw e;
               if (log.isDebugEnabled())
                  log.debug(e.getMessage());
            }

         }
         log.info("DB schema of DataSource: '" + dsName + "' created succesfully. context " + context);
      }
      catch (SQLException e)
      {
         log.error("Could not create db schema of DataSource: '" + dsName + "'. Reason: " + e.getMessage() + "; "
                  + JDBCUtils.getFullMessage(e) + ". Last command: " + sql, e);
      }
      finally
      {
         conn.close();
      }

   }

   public void addPlugin(ComponentPlugin plugin)
   {
      if (plugin instanceof CreateDBSchemaPlugin)
      {
         CreateDBSchemaPlugin csplugin = (CreateDBSchemaPlugin)plugin;
         try
         {
            createTables(csplugin.getDataSource(), csplugin.getScript());
            createDBSchemaPlugins.add(csplugin);
         }
         catch (NamingException e)
         {
            log.error(e.getLocalizedMessage(), e);
         }
         catch (SQLException e)
         {
            log.error(e.getLocalizedMessage(), e);
         }
      }
   }

   @SuppressWarnings("unused")
   public ComponentPlugin removePlugin(String name)
   {
      return null;
   }

   public Collection getPlugins()
   {
      return createDBSchemaPlugins;
   }

   // for testing
   public static DBSchemaCreator initialize(String dsName, String script) throws SQLException, NamingException
   {
      return new DBSchemaCreator(dsName, script);
   }
}
