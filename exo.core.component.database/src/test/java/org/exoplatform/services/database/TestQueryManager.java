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

/**
 * Created by The eXo Platform SAS Author : Nhu Dinh Thuan
 * nhudinhthuan@exoplatform.com Mar 30, 2007
 */
public class TestQueryManager extends TestCase
{

   QueryBuilder manager_;

   public TestQueryManager(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      manager_ = new QueryBuilder();
   }

   public void testPaser() throws Exception
   {
      String template = "select name from $table where id = $id and name like '&yahoo'";
      String[][] parameters = {{"table", "student"}, {"id", "12345"}};
      String pamameterSql = manager_.mapDataToSql(template, parameters);
   }

   static public class Student
   {

      String name, value;

      public Student(String n, String v)
      {
         name = n;
         value = v;
      }

      public String getName()
      {
         return name;
      }

      public void setValue(String value)
      {
         this.value = value;
      }
   }

}
