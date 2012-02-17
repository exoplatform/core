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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Apr 4, 2006
 */
public abstract class DAO<T extends DBObject>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.database.DAO");

   protected ExoDatasource eXoDS_;

   protected DBObjectMapper<T> mapper_;

   static int totalQueryTime = 0;

   static int totalBathTime = 0;

   static int totalCloseConnect = 0;

   public DAO(ExoDatasource datasource)
   {
      eXoDS_ = datasource;
      mapper_ = new ReflectionMapper<T>();
   }

   public DAO(ExoDatasource datasource, DBObjectMapper<T> mapper)
   {
      eXoDS_ = datasource;
      mapper_ = mapper;
   }

   public ExoDatasource getExoDatasource()
   {
      return eXoDS_;
   }

   abstract public T load(long id) throws Exception;

   abstract public PageList loadAll() throws Exception;

   abstract public void update(T bean) throws Exception;

   abstract public void update(List<T> beans) throws Exception;

   abstract public void save(T bean) throws Exception;

   abstract public void save(List<T> beans) throws Exception;

   abstract public void remove(T bean) throws Exception;

   abstract public T remove(long id) throws Exception;

   abstract public T createInstance() throws Exception;

   protected T loadUnique(String query) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         return loadUnique(connection, query);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected T loadUnique(Connection connection, String query) throws Exception
   {
      Statement statement = null;
      try
      {
         statement = connection.createStatement();
         // System.out.println(" Executed query: "+query) ;
         // long startGet = System.currentTimeMillis();
         ResultSet resultSet = statement.executeQuery(query);
         // totalQueryTime += System.currentTimeMillis() - startGet;
         // System.out.println(" \n\n\n == > total time to Query " +
         // totalQueryTime+"\n\n");
         if (!resultSet.next())
         {
            return null;
         }
         T bean = createInstance();
         mapper_.mapResultSet(resultSet, bean);
         resultSet.close();
         return bean;
      }
      finally
      {
         if (statement != null)
            statement.close();
      }
   }

   protected void loadInstances(String loadQuery, List<T> list) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         loadInstances(connection, loadQuery, list);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected void loadInstances(Connection connection, String loadQuery, List<T> list) throws Exception
   {
      Statement statement = connection.createStatement();
      // long startGet = System.currentTimeMillis();
      ResultSet resultSet = statement.executeQuery(loadQuery);
      // totalQueryTime += System.currentTimeMillis() - startGet;
      // System.out.println(" \n\n\n == > total time to Query " +
      // totalQueryTime+"\n\n");
      while (resultSet.next())
      {
         T bean = createInstance();
         mapper_.mapResultSet(resultSet, bean);
         list.add(bean);
      }
      resultSet.close();
      statement.close();
   }

   protected void execute(String query, T bean) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         execute(connection, query, bean);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected void execute(Connection connection, String query, T bean) throws Exception
   {
      PreparedStatement statement = connection.prepareStatement(query);
      if (bean != null)
         mapper_.mapUpdate(bean, statement);
      // System.out.println(" Executed query: "+query) ;
      // long startGet = System.currentTimeMillis();
      statement.executeUpdate();
      // totalQueryTime += System.currentTimeMillis() - startGet;
      // System.out.println(" \n\n\n == > total time to Query " +
      // totalQueryTime+"\n\n");
      eXoDS_.commit(connection);
      statement.close();
   }

   public <E> E loadDBField(String query) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         return this.<E> loadDBField(connection, query);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   @SuppressWarnings("unchecked")
   protected <E> E loadDBField(Connection connection, String query) throws Exception
   {
      Statement statement = connection.createStatement();
      long startGet = System.currentTimeMillis();
      ResultSet resultSet = statement.executeQuery(query);
      totalQueryTime += System.currentTimeMillis() - startGet;
      // System.out.println(" \n\n\n == > total time to Query " +
      // totalQueryTime+"\n\n");
      if (!resultSet.next())
         return null;
      E value = (E)resultSet.getObject(1);
      resultSet.close();
      statement.close();
      return value;
   }

   protected void execute(String template, List<T> beans) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         execute(connection, template, beans);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected void execute(Connection connection, String template, List<T> beans) throws Exception
   {
      PreparedStatement statement = connection.prepareStatement(template);
      QueryBuilder builder = eXoDS_.getQueryBuilder();
      for (T bean : beans)
      {
         String query = builder.mapDataToSql(template, mapper_.toParameters(bean));
         statement.addBatch(query);
         LOG.info(" addBatch " + query);
      }
      statement.executeBatch();
      statement.close();
      eXoDS_.commit(connection);
   }

   public DBObjectMapper<T> getDBObjectMapper()
   {
      return mapper_;
   }

}
