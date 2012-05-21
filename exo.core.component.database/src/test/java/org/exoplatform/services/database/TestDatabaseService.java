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

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.impl.XAPoolTxSupportDatabaseService;
import org.exoplatform.services.database.table.ExoLongID;
import org.exoplatform.services.database.table.IDGenerator;
import org.exoplatform.services.transaction.TransactionService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.transaction.UserTransaction;

/*
 * Thu, May 15, 2003 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestDatabaseService.java 5332 2006-04-29 18:32:44Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestDatabaseService extends TestCase
{

   public void testDatabaseService() throws Exception
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      DatabaseService service =
         (DatabaseService)pcontainer.getComponentInstanceOfType(XAPoolTxSupportDatabaseService.class);
      assertNotNull(service);
      assertConfiguration(service);
      assertDBTableManager(service);
      assertIDGenerator(service);
   }

   private void assertConfiguration(DatabaseService service) throws Exception
   {
      TransactionService txservice = service.getTransactionService();
      assertTrue(service != null);
      // TransactionManager tm = txservice.getTransactionManager() ;
      UserTransaction utx = txservice.getUserTransaction();
      Connection conn = service.getConnection();
      Statement s = null;
      utx.begin();
      try
      {
         s = conn.createStatement();
         s.addBatch("create table test (name varchar(25), data varchar(25))");
         s.addBatch("insert into test values('name1', 'value1')");
         s.executeBatch();
         s.close();
         /*
          * Call conn.commit() will cause an exception since the connection is now
          * part of a global transaction. You should call utx.commit() here
          */
         conn.commit();
         utx.commit();
      }
      catch (Exception ex)
      {
         utx.rollback();
      }
      // tm.rollback() ;
      service.closeConnection(conn);
      conn = service.getConnection();
      s = conn.createStatement();
      ResultSet rs = s.executeQuery("select name from test");
      if (rs.next())
      {
         fail("Should not have any data in the test table");
      }
   }

   private void assertDBTableManager(DatabaseService service) throws Exception
   {
      ExoDatasource datasource = service.getDatasource();
      DBTableManager dbManager = datasource.getDBTableManager();
      assertEquals(dbManager.hasTable(Mock.class), false);
      dbManager.createTable(Mock.class, true);

      assertEquals(dbManager.hasTable(Mock.class), true);
      dbManager.dropTable(Mock.class);

      assertEquals(dbManager.hasTable(Mock.class), false);
   }

   private void assertIDGenerator(DatabaseService service) throws Exception
   {
      ExoDatasource datasource = service.getDatasource();
      IDGenerator idGenerator = new IDGenerator(datasource);

      for (int i = 0; i < 10; i++)
      {
         idGenerator.generateLongId(ExoLongID.class);
      }

      idGenerator.restartTracker();
      idGenerator.generateLongId(ExoLongID.class);
   }

}
