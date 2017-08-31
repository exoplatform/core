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
package org.exoplatform.services.xml.resolving.impl;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.xml.BaseTest;
import org.exoplatform.services.xml.resolving.XMLResolvingService;
import org.xml.sax.InputSource;

/**
 * Created by the Exo Development team.
 */
public class TestXMLResolver extends BaseTest
{

   private XMLResolvingService service;

   public void setUp() throws Exception
   {
      if (service == null)
      {
         StandaloneContainer.setConfigurationPath("src/test/resources/conf/standalone/test-configuration.xml");
         StandaloneContainer container = StandaloneContainer.getInstance();
         service = (XMLResolvingService)container.getComponentInstanceOfType(XMLResolvingService.class);
      }
   }

   public void testLookupFailed() throws Exception
   {
      javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();
      org.xml.sax.XMLReader reader = jaxpParser.getXMLReader();

      reader.setEntityResolver(service.getEntityResolver());
      try
      {
         reader.parse(new InputSource(resourceStream("tmp/dtd-not-found.xml")));

      }
      catch (Throwable e)
      {
         return;
      }
      fail("Lookup should have been Failed as there is not such local DTD.");
   }

   public void testWebXmlResolving() throws Exception
   {
      try
      {

         javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
         factory.setNamespaceAware(true);
         javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();
         org.xml.sax.XMLReader reader = jaxpParser.getXMLReader();

         reader.setEntityResolver(service.getEntityResolver());

         reader.parse(new InputSource(resourceStream("web.xml")));

      }
      catch (Exception e)
      {

         fail("testWebXmlResolving() ERROR: " + e.toString());
      }

   }

}
