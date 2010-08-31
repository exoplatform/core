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

import org.exoplatform.services.document.DocumentReaderService;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestMSXExcelDocumentReader extends BaseStandaloneTest
{

   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

   DocumentReaderService service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = (DocumentReaderService)getComponentInstanceOfType(DocumentReaderService.class);
   }

   public void testGetContentAsString() throws Exception
   {
      InputStream is = TestMSXExcelDocumentReader.class.getResourceAsStream("/test.xlsx");
      try
      {
         String text =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
               .getContentAsText(is);

         String expected =
            "Ronaldo Eric Cantona Kaka Ronaldonho " + "ID Group Functionality Executor Begin End Tested "
               + "XNNL XNNL Xay dung vung quan li nguyen lieu NamPH "
               + getDate(2005, 2, 2)
               + " "
               + getDate(2005, 10, 2)
               + " Tested "
               + "XNNL XNNL XNNL_HAVEST NamPH 1223554.0 "
               + getDate(2005, 10, 1)
               + " Tested "
               + "XNNL XNNL XNNL_PIECE_OF_GROUND NamPH "
               + getDate(2005, 10, 12)
               + " "
               + getDate(2005, 10, 2)
               + " Tested "
               + "XNNL XNNL XNNL_76 NamPH TRUE() "
               + getDate(1984, 12, 10)
               + " No "
               + "XNNL XNNL XNNL_CREATE_REAP NamPH none "
               + getDate(2005, 10, 3)
               + " No "
               + "XNNL XNNL XNNL_SCALE NamPH "
               + getDate(1984, 12, 10)
               + " "
               + getDate(2005, 10, 5)
               + " Tested "
               + "XNNL XNNL LASUCO_PROJECT NamPH "
               + getDate(2005, 10, 5)
               + " "
               + getDate(2005, 10, 6)
               + " No "
               + "XNNL XNNL LASUCO_PROJECT NamPH Tested "
               + "XNNL XNNL XNNL_BRANCH NamPH "
               + getDate(2005, 12, 12)
               + " "
               + getDate(2005, 6, 10)
               + " Tested "
               + "XNNL XNNL XNNL_SUGAR_RACE NamPH "
               + getDate(2005, 5, 9)
               + " "
               + getDate(2005, 6, 10)
               + " No "
               + "XNNL XNNL F_XNNL_DISTRI NamPH "
               + getDate(2005, 5, 9)
               + " "
               + getDate(2005, 6, 10)
               + " Tested "
               + "XNNL XNNL XNNL_LASUCO_USER NamPH "
               + getDate(2005, 9, 9)
               + " "
               + getDate(2005, 6, 10) + " No";

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

      return (DATE_FORMAT.format(date.getTime()));
   }

}
