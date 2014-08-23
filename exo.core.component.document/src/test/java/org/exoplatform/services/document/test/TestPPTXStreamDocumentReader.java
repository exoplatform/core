/*
 * Copyright (C) 2014 eXo Platform SAS.
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

import org.exoplatform.services.document.DCMetaData;
import org.exoplatform.services.document.impl.DocumentReaderServiceImpl;
import org.exoplatform.services.document.impl.MSPPTXStreamDocumentReader;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS
 * 
 * @version $Id: $
 */

public class TestPPTXStreamDocumentReader extends BaseStandaloneTest {
  DocumentReaderServiceImpl service;
  String mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = new DocumentReaderServiceImpl(null);
    service.addDocumentReader(new MSPPTXStreamDocumentReader());
  }

  public void testGetContentAsString() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/test.pptx");
    try {
      String text = service.getDocumentReader(mimeType).getContentAsText(is);
      String etalon =
         "TEST POWERPOINT\n" + "Manchester United \n" + "AC Milan\n" + "SLIDE 2 \n" + "Eric Cantona\n" + "Kaka\n"
            + "Ronaldo\n" + "The natural scients universitys\n";
      
      assertEquals("Wrong string returned.", normalizeWhitespaces(etalon), normalizeWhitespaces(text));
    } finally {
      is.close();
    }
  }
  
  public void testGetProperties() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/test.pptx");
    try {
      Properties props = service.getDocumentReader(mimeType).getProperties(is);
      Properties etalon = new Properties();

      etalon.put(DCMetaData.TITLE, "test-Title");
      etalon.put(DCMetaData.SUBJECT, "test-Subject");
      etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
      etalon.put(DCMetaData.DESCRIPTION, "test-Comments");

      evalProps(etalon, props, true);
    } catch (Exception e) {
      throw new Exception("Failed to getProperties of test.pptx.", e);
    } finally {
      is.close();
    }
  }
  
  public void testGetContentAsString2() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/eXo-JCR-1.15.pptx");
    try {
      String text = service.getDocumentReader(mimeType).getContentAsText(is);
      String slide1_text = "What’s new in eXo JCR 1.15?";
      assertTrue("Missing text of the first slide", normalizeWhitespaces(text).contains(slide1_text));
    } finally {
      is.close();
    }
  }
  
  public void testGetProperties2() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/eXo-JCR-1.15.pptx");
    try {
      Properties props = service.getDocumentReader(mimeType).getProperties(is);
      Properties etalon = new Properties();
      etalon.put(DCMetaData.TITLE, "Présentation PowerPoint");
      evalProps(etalon, props, true);
    } catch (Exception e) {
      throw new Exception("Failed to getProperties of eXo-JCR-1.15.pptx.", e);
    } finally {
      is.close();
    }
  }

  public void testPPSXGetContentAsString() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/testPPT.ppsx");
    try {
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
    } finally {
      is.close();
    }
  }

  public void testPPTMGetContentAsString() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/testPPT.pptm");
    try {
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
    } finally {
      is.close();
    }
  }

  public void testPPSMGetContentAsString() throws Exception {
    InputStream is = TestPPTXStreamDocumentReader.class.getResourceAsStream("/testPPT.ppsm");
    try {
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
    } finally {
      is.close();
    }
  }  
  
  private void evalProps(Properties etalon, Properties testedProps, boolean testSize) {
    Iterator it = etalon.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry prop = (Map.Entry)it.next();
      Object tval = testedProps.get(prop.getKey());
      assertNotNull(prop.getKey() + " property not founded. ", tval);
      assertEquals(prop.getKey() + " property value is incorrect", prop.getValue(), tval);
    }
    if (testSize) {
      assertEquals("Incorrect size.", etalon.size(), testedProps.size());
    }
  }
}
