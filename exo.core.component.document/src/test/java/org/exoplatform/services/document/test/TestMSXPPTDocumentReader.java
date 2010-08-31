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

public class TestMSXPPTDocumentReader extends BaseStandaloneTest
{
   DocumentReaderService service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = (DocumentReaderService)getComponentInstanceOfType(DocumentReaderService.class);
   }

   public void testGetContentAsString() throws Exception
   {
      InputStream is = TestMSXPPTDocumentReader.class.getResourceAsStream("/test.pptx");
      try
      {
         String text =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.presentationml.presentation")
               .getContentAsText(is);
         String etalon =
            "TEST POWERPOINT\n" + "Manchester United \n" + "AC Milan\n" + "SLIDE 2 \n" + "Eric Cantona\n" + "Kaka\n"
               + "Ronaldo\n" + "The natural scients universitys\n";

         assertEquals("Wrong string returned", etalon, text);
      }
      finally
      {
         is.close();
      }
   }
}
