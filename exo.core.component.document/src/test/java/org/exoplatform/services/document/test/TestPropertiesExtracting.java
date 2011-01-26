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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.document.DCMetaData;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.impl.DocumentReaderServiceImpl;
import org.exoplatform.services.document.impl.HTMLDocumentReader;
import org.exoplatform.services.document.impl.MSExcelDocumentReader;
import org.exoplatform.services.document.impl.MSOutlookDocumentReader;
import org.exoplatform.services.document.impl.MSWordDocumentReader;
import org.exoplatform.services.document.impl.MSXExcelDocumentReader;
import org.exoplatform.services.document.impl.MSXPPTDocumentReader;
import org.exoplatform.services.document.impl.MSXWordDocumentReader;
import org.exoplatform.services.document.impl.OpenOfficeDocumentReader;
import org.exoplatform.services.document.impl.PDFDocumentReader;
import org.exoplatform.services.document.impl.PPTDocumentReader;
import org.exoplatform.services.document.impl.TextPlainDocumentReader;
import org.exoplatform.services.document.impl.XMLDocumentReader;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class TestPropertiesExtracting extends BaseStandaloneTest
{
   DocumentReaderServiceImpl service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      InitParams params = new InitParams();
      service.addDocumentReader(new TextPlainDocumentReader(params));
      service.addDocumentReader(new XMLDocumentReader());
      service.addDocumentReader(new HTMLDocumentReader());
      service.addDocumentReader(new MSExcelDocumentReader());
      service.addDocumentReader(new MSOutlookDocumentReader());
      service.addDocumentReader(new MSWordDocumentReader());
      service.addDocumentReader(new MSXExcelDocumentReader());
      service.addDocumentReader(new MSXPPTDocumentReader());
      service.addDocumentReader(new MSXWordDocumentReader());
      service.addDocumentReader(new OpenOfficeDocumentReader());
      service.addDocumentReader(new PDFDocumentReader());
      service.addDocumentReader(new PPTDocumentReader());
   }

   public void testPDFDocumentReaderServiceXMPMetadata() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/MyTest.pdf");
      try
      {
         DocumentReader rdr = service.getDocumentReader("application/pdf");
         Properties testprops = rdr.getProperties(is);
         Properties etalon = new Properties();
         etalon.put(DCMetaData.TITLE, "Test de convertion de fichier tif");
         etalon.put(DCMetaData.CREATOR, "Christian Klaus");
         etalon.put(DCMetaData.DESCRIPTION, "20080901 TEST Christian Etat OK");
         //         Calendar c = ISO8601.parseEx("2008-09-01T08:01:10+00:00");
         //         etalon.put(DCMetaData.DATE, c);
         evalProps(etalon, testprops, false);
      }
      finally
      {
         is.close();
      }
   }

   public void testPDFDocumentReaderServiceBrokenFile() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/pfs_accapp.pdf");
      try
      {

         DocumentReader rdr = service.getDocumentReader("application/pdf");
         Properties testprops = rdr.getProperties(is);
         Properties etalon = new Properties();
         etalon.put(DCMetaData.TITLE, "Personal Account Opening Form VN");
         etalon.put(DCMetaData.CREATOR, "mr");
         etalon.put(DCMetaData.PUBLISHER, "Adobe LiveCycle Designer ES 8.2");
         evalProps(etalon, testprops, false);
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
         evalProps(etalon, props, true);
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
         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, new Date(1283247255041L));
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
         evalProps(etalon, props, true);
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

         evalProps(etalon, props, true);
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
         date.set(2010, 7, 31, 7, 53, 0);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "Subject");
         etalon.put(DCMetaData.CREATOR, "nikolaz");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props, true);
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
         date.set(2010, 7, 31, 7, 59, 37);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props, true);
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
         date.set(2010, 7, 31, 8, 7, 25);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime());
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "KHANH NGUYEN GIA");
         etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props, true);
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
         date.set(2010, 7, 31, 14, 13, 23);

         etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.LANGUAGE, "ru-RU");
         etalon.put(DCMetaData.DATE, "2010-09-03T14:37:59.10");
         etalon.put(DCMetaData.SUBJECT, "test-Subject");
         etalon.put(DCMetaData.CREATOR, "Sergiy Karpenko");
         etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

         evalProps(etalon, props, true);
      }
      finally
      {
         is.close();
      }
   }

   private void evalProps(Properties etalon, Properties testedProps, boolean testSize)
   {
      Iterator it = etalon.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry prop = (Map.Entry)it.next();
         Object tval = testedProps.get(prop.getKey());
         assertNotNull(prop.getKey() + " property not founded. ", tval);
         assertEquals(prop.getKey() + " property value is incorrect", prop.getValue(), tval);
      }
      if (testSize)
      {
         assertEquals("size is incorrect", etalon.size(), testedProps.size());
      }
   }

}
