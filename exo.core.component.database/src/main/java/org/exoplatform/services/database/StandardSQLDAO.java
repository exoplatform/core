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

import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Apr 4, 2006
 */
public class StandardSQLDAO<T extends DBObject> extends DAO<T>
{

   protected Class<T> type_;

   public StandardSQLDAO(ExoDatasource datasource, Class<T> type)
   {
      super(datasource);
      this.type_ = type;
   }

   public StandardSQLDAO(ExoDatasource datasource, DBObjectMapper<T> mapper, Class<T> type)
   {
      super(datasource, mapper);
      this.type_ = type;
   }

   public T createInstance() throws Exception
   {
      return type_.newInstance();
   }

   public T load(long id) throws Exception
   {
      return super.loadUnique(eXoDS_.getQueryBuilder().createSelectQuery(type_, id));
   }

   public PageList loadAll() throws Exception
   {
      QueryBuilder queryBuilder = eXoDS_.getQueryBuilder();
      String query = queryBuilder.createSelectQuery(type_, -1);
      StringBuilder queryCounter = new StringBuilder("SELECT COUNT(*) ");
      queryCounter.append(query.substring(query.indexOf("FROM")));
      return new DBPageList<T>(20, this, query, queryCounter.toString());
   }

   @SuppressWarnings("unchecked")
   public void update(List<T> list) throws Exception
   {
      if (list == null)
         throw new Exception("The given beans null ");
      if (list.size() < 1)
         return;
      for (T bean : list)
      {
         if (bean.getDBObjectId() < 0)
         {
            throw new Exception("The given bean " + bean.getClass() + " doesn't have an id");
         }
      }
      execute(eXoDS_.getQueryBuilder().createUpdateQuery(type_), list);
   }

   public void update(T bean) throws Exception
   {
      String query = eXoDS_.getQueryBuilder().createUpdateQuery(type_, bean.getDBObjectId());
      execute(query, bean);
   }

   @SuppressWarnings("unchecked")
   public void save(List<T> list) throws Exception
   {
      if (list == null)
         throw new Exception("The given beans null ");
      if (list.size() < 1)
         return;
      for (T bean : list)
      {
         if (bean.getDBObjectId() != -1)
            continue;
         bean.setDBObjectId(eXoDS_.getIDGenerator().generateLongId(bean));
      }
      execute(eXoDS_.getQueryBuilder().createInsertQuery(type_), list);
   }

   public void save(T bean) throws Exception
   {
      if (bean.getDBObjectId() == -1)
         bean.setDBObjectId(eXoDS_.getIDGenerator().generateLongId(bean));
      execute(eXoDS_.getQueryBuilder().createInsertQuery(bean.getClass(), bean.getDBObjectId()), bean);
   }

   public T remove(long id) throws Exception
   {
      T bean = load(id);
      if (bean == null)
         return null;
      execute(eXoDS_.getQueryBuilder().createRemoveQuery(type_, id), (T)null);
      return bean;
   }

   public void remove(T bean) throws Exception
   {
      execute(eXoDS_.getQueryBuilder().createRemoveQuery(type_, bean.getDBObjectId()), (T)null);
   }

   // protected void invokeEvent(String prefix, String action, T bean) throws
   // Exception {
   // Table table = bean.getClass().getAnnotation(Table.class);
   // StringBuilder builder = new
   // StringBuilder(prefix).append('.').append(action)
   // .append('.').append(table.name());
   // DBObjectEvent<StandardSQLDAO, T> event = new DBObjectEvent<StandardSQLDAO,
   // T>(builder.toString(), this, bean);
   // listenerService_.broadcast(event);
   // }

   public Class<T> getType()
   {
      return type_;
   }
}
