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

import com.sun.rowset.CachedRowSetImpl;

import org.exoplatform.commons.utils.PageList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

/**
 * Created by The eXo Platform SAS Author : Nhu Dinh Thuan
 * nhudinhthuan@exoplatform.com Mar 30, 2007
 */
public class DBPageList<T extends DBObject> extends PageList
{

   protected DAO<T> dao_;

   protected String query_;

   public DBPageList(int pageSize, DAO<T> dao, DBObjectQuery<T> query) throws Exception
   {
      super(pageSize);
      dao_ = dao;
      query_ = query.toQuery();

      Object retObj = dao_.<Object> loadDBField(query.toCountQuery());
      if (retObj instanceof Integer)
      {
         super.setAvailablePage(((Integer)retObj).intValue());
      }
      else if (retObj instanceof BigDecimal)
      {
         super.setAvailablePage(((BigDecimal)retObj).intValue());
      }
      else
      {
         super.setAvailablePage(((Long)retObj).intValue());
      }
   }

   public DBPageList(int pageSize, DAO<T> dao, String query, String queryCounter) throws Exception
   {
      super(pageSize);
      dao_ = dao;
      query_ = query;

      Object retObj = dao_.<Object> loadDBField(queryCounter);
      if (retObj instanceof Integer)
      {
         super.setAvailablePage(((Integer)retObj).intValue());
      }
      else if (retObj instanceof BigDecimal)
      {
         super.setAvailablePage(((BigDecimal)retObj).intValue());
      }
      else
      {
         super.setAvailablePage(((Long)retObj).intValue());
      }
      // super.setAvailablePage(counter.intValue());
   }

   protected void populateCurrentPage(int currentPage) throws Exception
   {
      this.currentPage_ = currentPage;
      if (currentListPage_ != null)
         currentListPage_.clear();
      else
         currentListPage_ = new ArrayList<T>();
      loadPageList(this, query_);
   }

   @SuppressWarnings("unchecked")
   private void loadPageList(DBPageList<T> pageList, String query) throws Exception
   {
      Connection connection = null;
      try
      {
         connection = dao_.getExoDatasource().getConnection();
         Statement statement =
            connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         ResultSet resultSet = statement.executeQuery(query);

         CachedRowSet crs = new CachedRowSetImpl();
         crs.setPageSize(pageList.getPageSize());
         crs.populate(resultSet, (pageList.getCurrentPage() - 1) * pageList.getPageSize() + 1);

         while (crs.next())
         {
            T bean = dao_.createInstance();
            dao_.getDBObjectMapper().mapResultSet(crs, bean);
            currentListPage_.add(bean);
         }

         resultSet.close();
         statement.close();
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         dao_.getExoDatasource().closeConnection(connection);
      }
   }

   public List<T> getAll() throws Exception
   {
      Connection connection = null;
      try
      {
         connection = dao_.getExoDatasource().getConnection();
         Statement statement =
            connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         ResultSet resultSet = statement.executeQuery(query_);

         List<T> list = new ArrayList<T>();
         while (resultSet.next())
         {
            T bean = dao_.createInstance();
            dao_.getDBObjectMapper().mapResultSet(resultSet, bean);
            list.add(bean);
         }
         resultSet.close();
         statement.close();
         return list;
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         dao_.getExoDatasource().closeConnection(connection);
      }
   }

}
