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
import org.exoplatform.services.document.impl.XMLDocumentReader;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestXMLDocumentReader extends BaseStandaloneTest
{
   DocumentReaderServiceImpl service;

   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      service.addDocumentReader(new XMLDocumentReader());
   }

   public void testGetContentAsString() throws Exception
   {
      InputStream is = TestXMLDocumentReader.class.getResourceAsStream("/test.xml");
      try
      {
         String text = service.getDocumentReader("text/xml").getContentAsText(is);
         String expected = "John\n" + "  Alice\n" + "  Reminder\n" + "  Don't forget it this weekend!";
         assertEquals("Wrong string returned", expected, text.trim());
      }
      finally
      {
         is.close();
      }
   }

   public void testGetContentAsStringXXE() throws Exception
   {
      InputStream is = TestXMLDocumentReader.class.getResourceAsStream("/test.xml");
      file = createTempFile("test", ".xml");
      replaceFirstInFile(
         is,
         file,
         new String[]{"<note>", "<to>"},
         new String[]{
            "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \""
               + TestXMLDocumentReader.class.getResource("/test.txt") + "\">]><note>",
            "<to>&xxe;"});
      is = new FileInputStream(file);
      try
      {
         String text = service.getDocumentReader("text/xml").getContentAsText(is);
         String expected = "John\n" + "  Alice\n" + "  Reminder\n" + "  Don't forget it this weekend!";
         assertEquals("Wrong string returned", expected, text.trim());
      }
      finally
      {
         is.close();
      }
   }

   /**
    * test XXE External Entity point to non-existing resource
    */
   
   public void testGetContentAsStringXXE2() throws Exception
   {
      InputStream is = TestXMLDocumentReader.class.getResourceAsStream("/test.xml");
      file = createTempFile("test", ".xml");
      replaceFirstInFile(
         is,
         file,
         new String[]{"<note>", "<to>"},
         new String[]{
            "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \""
               + TestXMLDocumentReader.class.getResource("/test123.txt") + "\">]><note>",
            "<to>&xxe;"});
      is = new FileInputStream(file);
      try
      {
         String text = service.getDocumentReader("text/xml").getContentAsText(is);
         String expected = "John\n" + "  Alice\n" + "  Reminder\n" + "  Don't forget it this weekend!";
         assertEquals("Wrong string returned", expected, text.trim());
      }
      finally
      {
         is.close();
      }
   }
   
   public void testGetContentAsStringXEE() throws Exception
   {
      InputStream is = TestXMLDocumentReader.class.getResourceAsStream("/test.xml");
      file = createTempFile("test", ".xml");
      replaceFirstInFile(is, file, new String[]{"<note>", "<to>"},
         new String[]{
            "<!DOCTYPE lolz [<!ENTITY xee \"xee\">"
               + "<!ENTITY xee1 \"&xee;&xee;&xee;&xee;&xee;&xee;&xee;&xee;&xee;&xee;\">"
               + "<!ENTITY xee2 \"&xee1;&xee1;&xee1;&xee1;&xee1;&xee1;&xee1;&xee1;&xee1;&xee1;\">"
               + "<!ENTITY xee3 \"&xee2;&xee2;&xee2;&xee2;&xee2;&xee2;&xee2;&xee2;&xee2;&xee2;\">"
               + "<!ENTITY xee4 \"&xee3;&xee3;&xee3;&xee3;&xee3;&xee3;&xee3;&xee3;&xee3;&xee3;\">"
               + "<!ENTITY xee5 \"&xee4;&xee4;&xee4;&xee4;&xee4;&xee4;&xee4;&xee4;&xee4;&xee4;\">"
               + "<!ENTITY xee6 \"&xee5;&xee5;&xee5;&xee5;&xee5;&xee5;&xee5;&xee5;&xee5;&xee5;\">]>"
               + "<note>", "<to>&xee6;"});
      is = new FileInputStream(file);
      try
      {
         String text = service.getDocumentReader("text/xml").getContentAsText(is);
         assertTrue(text.isEmpty());
      }
      finally
      {
         is.close();
      }
   }

   public void testCDATAGetContentAsString() throws Exception
   {
      InputStream is = TestXMLDocumentReader.class.getResourceAsStream("/testCDATA.xml");
      try
      {
         String text = service.getDocumentReader("text/xml").getContentAsText(is);
         String expected = "This is a text inside CDATA.";
         assertEquals("Wrong string returned", expected, text.trim());
      }
      finally
      {
         is.close();
      }
   }

   public void testI18ngetContentAsString() throws Exception
   {
      InputStream is = TestXMLDocumentReader.class.getResourceAsStream("/testUTF8.xml");
      try
      {
         String text = service.getDocumentReader("text/xml").getContentAsText(is);
         final String expected =
            "\u0426\u0435 \u0442\u0435\u0441\u0442\u043e\u0432\u0438\u0439 \u0442\u0435\u043a\u0441\u0442.\n"
               + "Archim\u00E8de et Lius \u00E0 Ch\u00E2teauneuf testing chars en \u00E9t\u00E9";
         assertEquals("Wrong string returned", expected, text.trim());
      }
      finally
      {
         is.close();
      }
   }
}
