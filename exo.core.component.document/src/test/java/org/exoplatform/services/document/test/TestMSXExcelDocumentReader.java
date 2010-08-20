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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.test.BasicTestCase;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestMSXExcelDocumentReader extends BasicTestCase
{
   DocumentReaderService service_;

   @Override
   public void setUp() throws Exception
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      service_ = (DocumentReaderService)pcontainer.getComponentInstanceOfType(DocumentReaderService.class);
   }

   public void testGetContentAsString() throws Exception
   {
      InputStream is = TestMSXExcelDocumentReader.class.getResourceAsStream("/test.xlsx");
      String text = service_.getDocumentReader("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").getContentAsText(is);
      System.out.println(" text [" + text + "]");
      /*
       * String etalon =
       * "Ronaldo Eric Cantona Kaka Ronaldonho ID Group Functionality Executor Begin End Tested "
       * +
       * "XNNL XNNL Xay dung vung quan li nguyen lieu NamPH 2005-02-02 00:00:00.000+0200 2005-10-02 00:00:00.000+0300 Tested "
       * +
       * "XNNL XNNL XNNL_HAVEST NamPH 1223554.0 2005-10-01 00:00:00.000+0300 Tested "
       * +
       * "XNNL XNNL XNNL_PIECE_OF_GROUND NamPH 2005-10-12 00:00:00.000+0300 2005-10-02 00:00:00.000+0300 Tested "
       * +"XNNL XNNL XNNL_76 NamPH TRUE 1984-12-10 00:00:00.000+0200 No "
       * +"XNNL XNNL XNNL_CREATE_REAP NamPH none 2005-10-03 00:00:00.000+0300 No "
       * +
       * "XNNL XNNL XNNL_SCALE NamPH 1984-12-10 00:00:00.000+0200 2005-10-05 00:00:00.000+0300 Tested "
       * +
       * "XNNL XNNL LASUCO_PROJECT NamPH 2005-10-05 00:00:00.000+0300 2005-10-06 00:00:00.000+0300 No "
       * +"XNNL XNNL LASUCO_PROJECT NamPH Tested "+
       * "XNNL XNNL XNNL_BRANCH NamPH 2005-12-12 00:00:00.000+0200 2005-06-10 00:00:00.000+0300 Tested "
       * +
       * "XNNL XNNL XNNL_SUGAR_RACE NamPH 2005-05-09 00:00:00.000+0300 2005-06-10 00:00:00.000+0300 No "
       * +
       * "XNNL XNNL F_XNNL_DISTRI NamPH 2005-05-09 00:00:00.000+0300 2005-06-10 00:00:00.000+0300 Tested "
       * +
       * "XNNL XNNL XNNL_LASUCO_USER NamPH 2005-09-09 00:00:00.000+0300 2005-06-10 00:00:00.000+0300 No "
       * ; System.out.println(" etalon ["+etalon+"]");
       * assertEquals("String length is incorect",etalon.length(),text.length());
       * assertEquals("Wrong string returned",etalon ,text );
       */

   }
}
