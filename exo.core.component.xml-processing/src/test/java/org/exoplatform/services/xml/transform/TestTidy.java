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
package org.exoplatform.services.xml.transform;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.xml.BaseTest;
import org.exoplatform.services.xml.transform.html.HTMLTransformer;
import org.exoplatform.services.xml.transform.html.HTMLTransformerService;

import java.io.*;
import java.util.Properties;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by the Exo Development team.
 */
public class TestTidy extends BaseTest
{
   private HTMLTransformer htmlTransformer;

   public void setUp() throws Exception
   {
      StandaloneContainer.setConfigurationPath("src/test/resources/conf/standalone/test-configuration.xml");
      StandaloneContainer container = StandaloneContainer.getInstance();
      HTMLTransformerService htmlService =
         (HTMLTransformerService)container.getComponentInstanceOfType(HTMLTransformerService.class);
      assertNotNull("htmlService", htmlService);
      htmlTransformer = htmlService.getTransformer();

   }

   public void testTidy() throws Exception
   {
      try
      {
         File outputFile = File.createTempFile("rss-out-", ".xhtml");
         outputFile.deleteOnExit();
         InputStream res = resourceStream("rss-in.html");

         // output file
         OutputStream outputFileOutputStream = new FileOutputStream(outputFile);

         htmlTransformer.initResult(new StreamResult(outputFileOutputStream));
         htmlTransformer.transform(new StreamSource(res));

         outputFileOutputStream.close();

         // read the output file
         FileInputStream outputFileInputStream = new FileInputStream(outputFile);
         assertTrue("Output is empty", outputFileInputStream.available() > 0);

         // validate output xml
         validateXML(outputFileInputStream);

      }
      catch (Exception e)
      {
         fail("testTidy() ERROR: " + e.toString());
      }
   }

   public void testSAXResultType() throws Exception
   {
      File outputFile = File.createTempFile("rss-out-", ".xml");
      InputStream res = resourceStream("rss-in.xhtml");

      assertTrue("Empty input file", res.available() > 0);

      // create empty transformation
      TransformerHandler transformHandler = // a copy of the source to the result
         ((SAXTransformerFactory)SAXTransformerFactory.newInstance()).newTransformerHandler();

      OutputStream output = new FileOutputStream(outputFile);

      transformHandler.setResult(new StreamResult(output));

      SAXResult saxResult = new SAXResult(transformHandler);
      htmlTransformer.initResult(saxResult);
      htmlTransformer.transform(new StreamSource(res));

      output.flush();
      output.close();
      // read the output file
      FileInputStream outputFileInputStream = new FileInputStream(outputFile);
      assertTrue("Output is empty", outputFileInputStream.available() > 0);
      // validate output xml
      validateXML(outputFileInputStream);
   }

   public void testProps() throws Exception
   {
      try
      {
         Properties props = htmlTransformer.getOutputProperties();

         assertEquals(props.getProperty("quiet"), "true");
         props.setProperty("quiet", "false");

         htmlTransformer.setOutputProperties(props);
         assertEquals(htmlTransformer.getOutputProperties().getProperty("quiet"), "false");

      }
      catch (Exception e)
      {
         fail("testProps() ERROR: " + e.toString());
      }

   }

}
