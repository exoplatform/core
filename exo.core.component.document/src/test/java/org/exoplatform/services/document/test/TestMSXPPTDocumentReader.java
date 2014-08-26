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
import org.exoplatform.services.document.impl.MSXPPTDocumentReader;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestMSXPPTDocumentReader extends BaseStandaloneTest
{
   DocumentReaderServiceImpl service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      service.addDocumentReader(new MSXPPTDocumentReader());
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
            "TEST POWERPOINT " + "Manchester United " + "AC Milan " + "SLIDE 2 " + "Eric Cantona " + "Kaka "
               + "Ronaldo " + "The natural scients universitys ";

         assertEquals("Wrong string returned", etalon, text);
      }
      finally
      {
         is.close();
      }
   }

   public void testGetContentAsString2() throws Exception
   {
      InputStream is = TestMSXPPTDocumentReader.class.getResourceAsStream("/test2.pptx");
      try
      {
         String text =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.presentationml.presentation")
               .getContentAsText(is);
         int lastIndex = -1;
         for (int i = 1; i <= 25; i++)
         {
            String content = "foo" + i;
            int index = text.indexOf(content);
            assertFalse("Cannot found: "+ content, index == -1);
            assertTrue("The content " + content + " has not the right position", index > lastIndex);
            lastIndex = index;
         }
      }
      finally
      {
         is.close();
      }
   }

   public void testPPSXGetContentAsString() throws Exception
   {
      InputStream is = TestMSXPPTDocumentReader.class.getResourceAsStream("/testPPT.ppsx");
      try
      {
         String content =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.presentationml.slideshow")
               .getContentAsText(is);
         assertTrue(content
            .contains("This is a test file data with the same content as every other file being tested for"));
         assertTrue(content.contains("Different words to test against"));
         assertTrue(content.contains("Quest"));
         assertTrue(content.contains("Hello"));
         assertTrue(content.contains("Watershed"));
         assertTrue(content.contains("Avalanche"));
         assertTrue(content.contains("Black Panther"));
      }
      finally
      {
         is.close();
      }
   }

   public void testPPTMGetContentAsString() throws Exception
   {
      InputStream is = TestMSXPPTDocumentReader.class.getResourceAsStream("/testPPT.pptm");
      try
      {
         String content =
            service.getDocumentReader("application/vnd.ms-powerpoint.presentation.macroenabled.12")
               .getContentAsText(is);
         assertTrue(content
            .contains("This is a test file data with the same content as every other file being tested for"));
         assertTrue(content.contains("Different words to test against"));
         assertTrue(content.contains("Quest"));
         assertTrue(content.contains("Hello"));
         assertTrue(content.contains("Watershed"));
         assertTrue(content.contains("Avalanche"));
         assertTrue(content.contains("Black Panther"));
      }
      finally
      {
         is.close();
      }
   }

   public void testPPSMGetContentAsString() throws Exception
   {
      InputStream is = TestMSXPPTDocumentReader.class.getResourceAsStream("/testPPT.ppsm");
      try
      {
         String content =
            service.getDocumentReader("application/vnd.ms-powerpoint.slideshow.macroenabled.12").getContentAsText(is);
         assertTrue(content
            .contains("This is a test file data with the same content as every other file being tested for"));
         assertTrue(content.contains("Different words to test against"));
         assertTrue(content.contains("Quest"));
         assertTrue(content.contains("Hello"));
         assertTrue(content.contains("Watershed"));
         assertTrue(content.contains("Avalanche"));
         assertTrue(content.contains("Black Panther"));
      }
      finally
      {
         is.close();
      }
   }
}
