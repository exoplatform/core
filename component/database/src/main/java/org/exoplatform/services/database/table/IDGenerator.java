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
package org.exoplatform.services.database.table;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.database.DAO;
import org.exoplatform.services.database.DBObject;
import org.exoplatform.services.database.DBObjectMapper;
import org.exoplatform.services.database.DBTableManager;
import org.exoplatform.services.database.ExoDatasource;
import org.exoplatform.services.database.annotation.Table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Apr 4, 2006
 */
public class IDGenerator extends DAO<ExoLongID>
{

   private HashMap<Class, IDTracker> idTrackers_;

   public IDGenerator(ExoDatasource datasource) throws Exception
   {
      super(datasource, new ExoLongIDMapper());
      idTrackers_ = new HashMap<Class, IDTracker>();

      DBTableManager tableManager = datasource.getDBTableManager();
      if (tableManager.hasTable(ExoLongID.class))
         return;
      tableManager.createTable(ExoLongID.class, true);
   }

   public ExoLongID loadObjectByName(String name) throws Exception
   {
      Table table = ExoLongID.class.getAnnotation(Table.class);
      StringBuilder builder = new StringBuilder("SELECT EXO_NAME, EXO_START FROM ");
      builder.append(table.name()).append(" WHERE EXO_NAME = '").append(name).append('\'');
      return loadUnique(builder.toString());
   }

   public <T extends DBObject> long generateLongId(T bean) throws Exception
   {
      return generateLongId(bean.getClass());
   }

   // Lazy loading
   synchronized public <T extends DBObject> long generateLongId(Class<T> type) throws Exception
   {
      IDTracker idTracker = idTrackers_.get(type);

      if (idTracker == null)
      {
         ExoLongID instanceID = loadObjectByName(type.getName());
         long currentId = 0;

         if (instanceID == null)
         {
            instanceID = new ExoLongID(type.getName(), ExoLongID.BLOCK_SIZE);
            save(instanceID);
         }
         else
         {
            currentId = instanceID.getCurrentBlockId();
            instanceID.setNextBlock();
            update(instanceID);
         }
         idTracker = new IDTracker(instanceID, currentId);
         idTrackers_.put(type, idTracker);
      }

      // System.out.println("+++>>" + load(1)) ;

      long generatedId = ++idTracker.currentId;
      if (generatedId > idTracker.blockTracker.getCurrentBlockId() + ExoLongID.BLOCK_SIZE)
      {
         idTracker.blockTracker.setNextBlock();
         update(idTracker.blockTracker);
      }
      return generatedId;
   }

   public void restartTracker()
   {
      idTrackers_.clear();
   } // for testing

   static private class IDTracker
   {

      private ExoLongID blockTracker;

      private long currentId;

      private IDTracker(ExoLongID dbobject, long id)
      {
         blockTracker = dbobject;
         currentId = id;
      }
   }

   @SuppressWarnings("unused")
   static public class ExoLongIDMapper implements DBObjectMapper<ExoLongID>
   {

      public String[][] toParameters(ExoLongID bean) throws Exception
      {
         return null;
      }

      public void mapUpdate(ExoLongID bean, PreparedStatement pstm) throws Exception
      {
         pstm.setString(1, bean.getName());
         pstm.setLong(2, bean.getCurrentBlockId());
      }

      public void mapResultSet(ResultSet res, ExoLongID bean) throws Exception
      {
         bean.setName(res.getString(1));
         bean.setCurrentBlockId(res.getLong(2));
      }
   }

   public ExoLongID createInstance() throws Exception
   {
      return new ExoLongID();
   }

   @SuppressWarnings("unused")
   public ExoLongID load(long id) throws Exception
   {
      return null;
   }

   public PageList loadAll() throws Exception
   {
      return null;
   }

   @SuppressWarnings("unused")
   public ExoLongID remove(long id) throws Exception
   {
      return null;
   }

   @SuppressWarnings("unused")
   public void remove(ExoLongID bean) throws Exception
   {
   }

   @SuppressWarnings("unused")
   public void save(ExoLongID bean) throws Exception
   {
   }

   @SuppressWarnings("unused")
   public void save(List<ExoLongID> beans) throws Exception
   {
   }

   @SuppressWarnings("unused")
   public void update(ExoLongID bean) throws Exception
   {
   }

   @SuppressWarnings("unused")
   public void update(List<ExoLongID> beans) throws Exception
   {
   }
}
