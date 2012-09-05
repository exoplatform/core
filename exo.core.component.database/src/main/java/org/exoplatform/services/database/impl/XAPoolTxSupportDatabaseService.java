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
package org.exoplatform.services.database.impl;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.database.DatabaseService;
import org.exoplatform.services.database.ExoDatasource;
import org.exoplatform.services.transaction.TransactionService;

import java.security.PrivilegedAction;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS
 * Author : Tuan Nguyen tuan08@users.sourceforge.net Apr 4, 2006
 */
public class XAPoolTxSupportDatabaseService implements DatabaseService
{
   private HashMap<String, ExoDatasource> datasources_;

   private ExoDatasource defaultDS_;

   private TransactionService txService_;

   public XAPoolTxSupportDatabaseService(InitParams params, TransactionService txService) throws Exception
   {
      datasources_ = new HashMap<String, ExoDatasource>(5);
      txService_ = txService;
      Iterator<?> i = params.getPropertiesParamIterator();
      while (i.hasNext())
      {
         PropertiesParam param = (PropertiesParam)i.next();
         String name = param.getName();
         ExoDatasource ds = new ExoDatasource(createDatasource(param.getProperties()));
         datasources_.put(name, ds);
         if (defaultDS_ == null)
            defaultDS_ = ds;
      }
   }

   public ExoDatasource getDatasource() throws Exception
   {
      return defaultDS_;
   }

   public ExoDatasource getDatasource(String dsName) throws Exception
   {
      return datasources_.get(dsName);
   }

   public Connection getConnection() throws Exception
   {
      return defaultDS_.getConnection();
   }

   public Connection getConnection(String dsName) throws Exception
   {
      ExoDatasource ds = datasources_.get(dsName);
      return ds.getConnection();
   }

   public void closeConnection(Connection conn) throws Exception
   {
      defaultDS_.closeConnection(conn);
   }

   public TransactionService getTransactionService() throws Exception
   {
      return txService_;
   }

   private DataSource createDatasource(Map<String, String> props) throws Exception
   {
      StandardXADataSource ds = SecurityHelper.doPrivilegedAction(new PrivilegedAction<StandardXADataSource>()
      {
         public StandardXADataSource run()
         {
            return new StandardXADataSource();
         }
      });

      ds.setDriverName(props.get("connection.driver"));
      ds.setUrl(props.get("connection.url"));
      ds.setUser(props.get("connection.login"));
      ds.setPassword(props.get("connection.password"));
      ds.setTransactionManager(txService_.getTransactionManager());

      StandardXAPoolDataSource pool = new StandardXAPoolDataSource(3);
      pool.setMinSize(Integer.parseInt(props.get("connection.min-size")));
      pool.setMaxSize(Integer.parseInt(props.get("connection.max-size")));
      pool.setUser(props.get("connection.login"));
      pool.setPassword(props.get("connection.password"));
      pool.setDataSource(ds);
      return pool;
   }
}
