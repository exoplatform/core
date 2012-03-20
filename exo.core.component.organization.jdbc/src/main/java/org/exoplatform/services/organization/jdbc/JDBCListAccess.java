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
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.database.DAO;
import org.exoplatform.services.database.DBObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: JDBCUserListAccess.java 111 2008-11-11 11:11:11Z $
 * @param <E>
 */
public class JDBCListAccess<E> implements ListAccess<E>
{

   /**
    * The DAO.
    */
   protected DAO dao;

   /**
    * The find query string.
    */
   protected String findQuery;

   /**
    * The count query string.
    */
   protected String countQuery;

   /**
    * JDBCUserListAccess constructor.
    * 
    * @param dao
    *          The DAO
    * @param findQuery
    *          Find query string
    * @param countQuery
    *          Count query string
    */
   public JDBCListAccess(DAO dao, String findQuery, String countQuery)
   {
      this.dao = dao;
      this.findQuery = findQuery;
      this.countQuery = countQuery;
   }

   /**
    * {@inheritDoc}
    */
   public int getSize() throws Exception
   {
      Connection connection = null;
      try
      {
         connection = dao.getExoDatasource().getConnection();

         Object retObj = dao.loadDBField(countQuery);
         if (retObj instanceof Integer)
         {
            return ((Integer)retObj).intValue();
         }
         else if (retObj instanceof BigDecimal)
         {
            return ((BigDecimal)retObj).intValue();
         }
         else
         {
            return ((Long)retObj).intValue();
         }
      }
      finally
      {
         dao.getExoDatasource().closeConnection(connection);
      }
   }

   /**
    * {@inheritDoc}
    */
   public E[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      Statement statement = null;
      ResultSet resultSet = null;

      Connection connection = dao.getExoDatasource().getConnection();
      try
      {
         if (index < 0)
         {
            throw new IllegalArgumentException("Illegal index: index must be a positive number");
         }

         if (length < 0)
         {
            throw new IllegalArgumentException("Illegal length: length must be a positive number");
         }

         E[] entities = null;

         statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         resultSet = statement.executeQuery(findQuery);

         for (int p = 0, counter = 0; counter < length; p++)
         {
            if (!resultSet.next())
               throw new IllegalArgumentException(
                  "Illegal index or length: sum of the index and the length cannot be greater than the list size");

            DBObject bean = dao.createInstance();
            dao.getDBObjectMapper().mapResultSet(resultSet, bean);

            if (p >= index)
            {
               if (entities == null)
               {
                  entities = (E[])Array.newInstance(bean.getClass(), length);
               }
               entities[counter++] = (E)bean;
            }
         }

         return entities == null ? (E[])new Object[0] : entities;
      }
      finally
      {
         if (resultSet != null)
         {
            resultSet.close();
         }

         if (statement != null)
         {
            statement.close();
         }

         dao.getExoDatasource().closeConnection(connection);
      }
   }
}
