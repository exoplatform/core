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

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestTextPlainDocumentReader extends BaseStandaloneTest
{
   DocumentReaderService service;

   public void setUp() throws Exception
   {
      super.setUp();
      service = (DocumentReaderService)getComponentInstanceOfType(DocumentReaderService.class);
   }

   public void testGetContentAsString() throws Exception
   {
      InputStream is = TestTextPlainDocumentReader.class.getResourceAsStream("/test.txt");
      try
      {
         String text = service.getDocumentReader("text/plain").getContentAsText(is);
         assertEquals("Wrong string returned", "This is a test text\n", text);
      }
      finally
      {
         is.close();
      }
   }

   public void testGetContentAsStringWithEncoding() throws Exception
   {
      InputStream is = TestTextPlainDocumentReader.class.getResourceAsStream("/testUTF8.txt");
      try
      {
         String text = service.getDocumentReader("text/plain").getContentAsText(is, "UTF-8");
         String expected =
            "\ufeff\u0426\u0435 \u0442\u0435\u0441\u0442\u043e\u0432\u0438\u0439 \u0442\u0435\u043a\u0441\u0442. \u042d\u0442\u043e \u0442\u0435\u0441\u0442\u043e\u0432\u044b\u0439 \u0442\u0435\u043a\u0441\u0442.";
         assertEquals("Wrong string returned", expected, text);
      }
      finally
      {
         is.close();
      }
   }

}
