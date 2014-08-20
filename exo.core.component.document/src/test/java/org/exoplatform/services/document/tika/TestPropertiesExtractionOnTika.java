/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.document.tika;

import org.exoplatform.services.document.DCMetaData;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.test.BaseStandaloneTest;
import org.exoplatform.services.document.test.TestPropertiesExtracting;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: TestPropertiesExtractionOnTika.java 111 2008-11-11 11:11:11Z serg $
 */
public class TestPropertiesExtractionOnTika extends BaseStandaloneTest
{
   DocumentReaderService service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = (DocumentReaderService)getComponentInstanceOfType(DocumentReaderService.class);

   }

   // TODO fix this test
   //   public void testPDFDocumentReaderServiceXMPMetadata() throws Exception
   //   {
   //      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/MyTest.pdf");
   //      try
   //      {
   //         DocumentReader rdr = service.getDocumentReader("application/pdf");
   //         Properties testprops = rdr.getProperties(is);
   //         Properties etalon = new Properties();
   //         etalon.put(DCMetaData.TITLE, "Test de convertion de fichier tif");
   //         etalon.put(DCMetaData.CREATOR, "Christian Klaus");
   //         etalon.put(DCMetaData.SUBJECT, "20080901 TEST Christian Etat OK");
   //         Calendar c = ISO8601.parseEx("2008-09-01T08:01:10+00:00");
   //         etalon.put(DCMetaData.DATE, c);
   //         evalProps(etalon, testprops);
   //      }
   //      finally
   //      {
   //         is.close();
   //      }
   //   }
   public void testCSSDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.css");
          try
          {
             Properties props = service.getDocumentReader("text/css").getProperties(is);
             evalProps(new Properties(), props);

          }
          finally
          {
              is.close();
          }
    }

   public void testPDFDocumentReaderServiceXMPMetadataTikasFile() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/tikaTestPDF.pdf");
      try
      {
         DocumentReader rdr = service.getDocumentReader("application/pdf");
         Properties testprops = rdr.getProperties(is);
         Properties etalon = new Properties();
         etalon.put(DCMetaData.TITLE, "Document title");
         etalon.put(DCMetaData.CREATOR, "Document author");
         evalProps(etalon, testprops);
      }
      finally
      {
         is.close();
      }
   }

   public void testWordDocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.doc");
      try
      {
         Properties props = service.getDocumentReader("application/msword").getProperties(is);
         Properties etalon = new Properties();
         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, new Date(1283247060000L));
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   public void testPPTDocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.ppt");
      try
      {
         Properties props = service.getDocumentReader("application/powerpoint").getProperties(is);
         Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(0);
         date.set(2010, 7, 31, 11, 34, 15);
         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   public void testExcelDocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.xls");
      try
      {
         Properties props = service.getDocumentReader("application/excel").getProperties(is);
         Properties etalon = new Properties();
         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, new Date(1283247293000L));
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "KHANH NGUYEN GIA");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   public void testXWordDocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.docx");
      try
      {
         Properties props =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
               .getProperties(is);
         Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(0);
         date.set(2010, 7, 31, 12, 13, 0);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "Subject");
         etalon.put(DCMetaData.CREATOR, "nikolaz");
         //etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   public void testXPPTDocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.pptx");
      try
      {
         Properties props =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.presentationml.presentation")
               .getProperties(is);
         Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(0);
         date.set(2010, 7, 31, 9, 55, 58);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   public void testXPPTDocumentReaderService2() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/testDate.pptx");
      try
      {
         Properties properties =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.presentationml.presentation").getProperties(is);
         Object date = properties.get(DCMetaData.DATE);
         assertNotNull(date);
         assertTrue(date instanceof Date);
      }
      finally
      {
         is.close();
      }
   }

   public void testXExcelDocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.xlsx");
      try
      {
         Properties props =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
               .getProperties(is);
         Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(0);
         date.set(2005, 9, 7, 12, 59, 55);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "KHANH NGUYEN GIA");
         //etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   public void testXExcelDocumentReaderService2() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/testDate.xlsx");
      try
      {
         Properties properties =
            service.getDocumentReader("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").getProperties(is);
         Object date = properties.get(DCMetaData.DATE);
         assertNotNull(date);
         assertTrue(date instanceof Date);
      }
      finally
      {
         is.close();
      }
   }

   public void testOODocumentReaderService() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.odt");
      try
      {
         Properties props = service.getDocumentReader("application/vnd.oasis.opendocument.text").getProperties(is);
         Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(0);
         date.set(2010, 8, 3, 14, 37, 59);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.LANGUAGE, "ru-RU");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Sergiy Karpenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props);
      }
      finally
      {
         is.close();
      }
   }

   private void evalProps(Properties etalon, Properties testedProps)
   {
      Iterator<Entry<Object, Object>> it = etalon.entrySet().iterator();
      while (it.hasNext())
      {
         Entry<Object, Object> prop = it.next();
         Object tval = testedProps.get(prop.getKey());
         assertNotNull(prop.getKey() + " property not founded. ", tval);
         assertEquals(prop.getKey() + " property value is incorrect", prop.getValue(), tval);
      }
   }

}
