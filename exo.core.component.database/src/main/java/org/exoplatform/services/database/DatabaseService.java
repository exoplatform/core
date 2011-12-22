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

import org.exoplatform.services.transaction.TransactionService;

import java.sql.Connection;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Apr 4, 2006 This service should provide a single
 * interface to access the diffent datasource.
 */
public interface DatabaseService
{

   /**
    * This method should return the default datasource of the application
    * 
    * @return
    * @throws Exception
    */
   public ExoDatasource getDatasource() throws Exception;

   /**
    * This method should look up the datasouce by the datasource name and return.
    * If the datasource is not found then the method should return null
    * 
    * @param dsname
    * @return
    * @throws Exception
    */
   public ExoDatasource getDatasource(String dsname) throws Exception;

   public Connection getConnection() throws Exception;

   public Connection getConnection(String dsName) throws Exception;

   public void closeConnection(Connection conn) throws Exception;

   /**
    * This method should return the transaction service
    * 
    * @return
    * @throws Exception
    */
   public TransactionService getTransactionService() throws Exception;

}
