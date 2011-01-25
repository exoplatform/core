/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.services.document.test.usecases;

import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.impl.DocumentReaderServiceImpl;
import org.exoplatform.services.document.impl.PDFDocumentReader;
import org.exoplatform.services.document.test.BaseStandaloneTest;
import org.exoplatform.services.document.test.TestPropertiesExtracting;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: TestPDFNullPointer.java 111 2008-11-11 11:11:11Z serg $
 */
public class TestPDFNullPointer extends BaseStandaloneTest
{
   DocumentReaderServiceImpl service;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      service.addDocumentReader(new PDFDocumentReader());
   }

   public void testPDFDocumentReaderServiceXMPMetadata() throws Exception
   {
      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/pfs_accapp.pdf");
      try
      {
         try
         {
            DocumentReader rdr = service.getDocumentReader("application/pdf");
            Properties testprops = rdr.getProperties(is);
            fail("There must be exception.");
         }
         catch (DocumentReadException e)
         {
            //ok
         }
      }
      finally
      {
         is.close();
      }
   }

   private void evalProps(Properties etalon, Properties testedProps)
   {
      Iterator it = etalon.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry prop = (Map.Entry)it.next();
         Object tval = testedProps.get(prop.getKey());
         assertNotNull(prop.getKey() + " property not founded. ", tval);
         assertEquals(prop.getKey() + " property value is incorrect", prop.getValue(), tval);
      }
      assertEquals("size is incorrect", etalon.size(), testedProps.size());
   }

}
