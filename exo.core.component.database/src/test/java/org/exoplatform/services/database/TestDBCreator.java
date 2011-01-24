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
package org.exoplatform.services.database;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.creator.DBConnectionInfo;
import org.exoplatform.services.database.creator.DBCreator;
import org.exoplatform.services.naming.InitialContextInitializer;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id$
 */
public class TestDBCreator extends TestCase
{

   protected DBCreator dbCreator;

   private InitialContextInitializer initContext;

   @Override
   public void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();

      dbCreator = (DBCreator)container.getComponentInstanceOfType(DBCreator.class);
      initContext = (InitialContextInitializer)container.getComponentInstanceOfType(InitialContextInitializer.class);
   }

   public void testDBCreate() throws Exception
   {
      assertNotNull(dbCreator);

      DBConnectionInfo dbInfo = dbCreator.createDatabase("testdb");
      DBConnectionInfo dbInfo1 = dbCreator.getDBConnectionInfo("testdb");

      Map<String, String> connProps = dbInfo.getProperties();
      Map<String, String> connProps1 = dbInfo1.getProperties();

      assertEquals(connProps.get("driverClassName"), connProps1.get("driverClassName"));
      assertEquals(connProps.get("password"), connProps1.get("password"));
      assertEquals(connProps.get("url"), connProps1.get("url"));
      assertEquals(connProps.get("username"), connProps1.get("username"));

      Map<String, String> refAddr = dbInfo.getProperties();

      initContext.bind("testjdbcjcr", "javax.sql.DataSource", "org.apache.commons.dbcp.BasicDataSourceFactory", null,
         refAddr);

      DataSource ds = (DataSource)initContext.getInitialContext().lookup("testjdbcjcr");
      assertNotNull(ds);

      Connection conn = ds.getConnection();
      assertNotNull(conn);
   }

   public void testDBCreateMultiThread() throws Exception
   {
      DBCreateThread[] queue = new DBCreateThread[100];

      for (int i = 0; i < queue.length; i++)
      {
         queue[i] = new DBCreateThread(i);
         queue[i].start();
      }

      for (int i = 0; i < queue.length; i++)
      {
         queue[i].join();
      }

      for (int i = 0; i < queue.length; i++)
      {
         DataSource ds = (DataSource)initContext.getInitialContext().lookup("testjdbcjcr_" + i);
         assertNotNull(ds);

         Connection conn = ds.getConnection();
         assertNotNull(conn);
      }

   }

   class DBCreateThread extends Thread
   {

      private final int threadNumber;

      DBCreateThread(int threadNumber)
      {
         this.threadNumber = threadNumber;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void run()
      {
         try
         {
            DBConnectionInfo dbInfo = dbCreator.createDatabase("testdb_" + threadNumber);

            Map<String, String> refAddr = dbInfo.getProperties();

            initContext.bind("testjdbcjcr_" + threadNumber, "javax.sql.DataSource",
               "org.apache.commons.dbcp.BasicDataSourceFactory", null, refAddr);
         }
         catch (Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

}
