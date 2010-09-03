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
import org.exoplatform.services.document.impl.MSXWordDocumentReader;

import java.io.InputStream;

/**
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: TestMSXWordDocumentReader.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class TestMSXWordDocumentReader extends BaseStandaloneTest
{
   DocumentReaderServiceImpl service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      service.addDocumentReader(new MSXWordDocumentReader());
   }

   public void testGetContentAsStringDoc() throws Exception
   {
      InputStream is = TestMSXWordDocumentReader.class.getResourceAsStream("/test.docx");
      try
      {
         String text =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
               .getContentAsText(is);
         assertTrue(text
            .contains("Before the test starts there is a directions section, which takes a few minutes to read"));
      }
      finally
      {
         is.close();
      }
   }
}
