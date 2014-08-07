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
package org.exoplatform.services.document.test;

import org.exoplatform.services.document.impl.DocumentReaderServiceImpl;
import org.exoplatform.services.document.impl.MSExcelDocumentReader;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestMSExcelDocumentReader extends BaseStandaloneTest
{

   DocumentReaderServiceImpl service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      service.addDocumentReader(new MSExcelDocumentReader());
   }

   public void testClassicExcelGetContentAsString() throws Exception
   {
      InputStream is = TestMSXExcelDocumentReader.class.getResourceAsStream("/testEXCEL.xls");
      try
      {
         String content = service.getDocumentReader("application/vnd.ms-excel").getContentAsText(is);
         assertTrue(content.contains("Sample Excel Worksheet"));
         assertTrue(content.contains("Numbers and their Squares"));
         assertTrue(content.contains("Number"));
         assertFalse(content.contains("9"));
         assertFalse(content.contains("9.0"));
         assertTrue(content.contains("Written and saved in Microsoft Excel X for Mac Service Release 1."));
      }
      finally
      {
         is.close();
      }
   }

   public void testGetContentAsString() throws Exception
   {
      InputStream is = TestMSExcelDocumentReader.class.getResourceAsStream("/test.xls");
      try
      {
         String text = service.getDocumentReader("application/excel").getContentAsText(is);
         String expected =
             "Sheet2 Sheet1 "
               + "Ronaldo Eric Cantona Kaka Ronaldonho "
               + "Group Functionality Executor Begin End Tested "
               + "XNNL XNNL Xay dung vung quan li nguyen lieu NamPH "
               + " Tested "
               + "XNNL XNNL XNNL_HAVEST NamPH "
               + " Tested "
               + "XNNL XNNL XNNL_PIECE_OF_GROUND NamPH "
               + " Tested "
               + "XNNL XNNL XNNL_76 NamPH "
               + "XNNL XNNL XNNL_CREATE_REAP NamPH none "
               + "XNNL XNNL XNNL_SCALE NamPH "
               + " Tested "
               + "XNNL XNNL LASUCO_PROJECT NamPH "
               + "XNNL XNNL LASUCO_PROJECT NamPH Tested "
               + "XNNL XNNL XNNL_BRANCH NamPH "
               + " Tested "
               + "XNNL XNNL XNNL_SUGAR_RACE NamPH "
               + "XNNL XNNL F_XNNL_DISTRI NamPH "
               + " Tested "
               + "XNNL XNNL XNNL_LASUCO_USER NamPH ";

         assertEquals("Wrong string returned", normalizeWhitespaces(expected), normalizeWhitespaces(text));
      }
      finally
      {
         is.close();
      }

   }

   public String getDate(int year, int month, int day)
   {
      Calendar date = Calendar.getInstance();
      date.setTimeInMillis(0);
      date.set(year, month - 1, day, 0, 0, 0);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
      return (dateFormat.format(date.getTime()));
   }
}
