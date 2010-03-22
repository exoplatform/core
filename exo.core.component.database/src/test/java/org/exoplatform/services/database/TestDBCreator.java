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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.creator.DBConnectionInfo;
import org.exoplatform.services.database.creator.DBCreator;
import org.exoplatform.services.naming.InitialContextBinder;
import org.exoplatform.services.naming.InitialContextInitializer;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: TestDBCreator.java 111 2010-11-11 11:11:11Z tolusha $
 */
public class TestDBCreator extends TestCase
{

   protected DBCreator dbCreator;

   private InitialContextBinder initialContextBinder;

   private InitialContextInitializer initContext;

   public void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();

      dbCreator = (DBCreator)container.getComponentInstanceOfType(DBCreator.class);
      initContext = (InitialContextInitializer)container.getComponentInstanceOfType(InitialContextInitializer.class);
      initialContextBinder = (InitialContextBinder)container.getComponentInstanceOfType(InitialContextBinder.class);
   }

   public void testDBCreate() throws Exception
   {
      assertNotNull(dbCreator);

      DBConnectionInfo dbInfo = dbCreator.createDatabase("testdb");

      Map<String, String> refAddr = new HashMap<String, String>();
      refAddr.put("driverClassName", dbInfo.getDriver());
      refAddr.put("url", dbInfo.getUrl());
      refAddr.put("username", dbInfo.getUsername());
      refAddr.put("password", dbInfo.getPassword());

      initialContextBinder.bind("testjdbcjcr", "javax.sql.DataSource",
         "org.apache.commons.dbcp.BasicDataSourceFactory", null, refAddr);

      DataSource ds = (DataSource)initContext.getInitialContext().lookup("testjdbcjcr");
      assertNotNull(ds);

      Connection conn = ds.getConnection();
      assertNotNull(conn);
   }
}
