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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.test.BasicTestCase;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestMSWordDocumentReader extends BasicTestCase
{
   DocumentReaderService service_;

   public void setUp() throws Exception
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      service_ = (DocumentReaderService)pcontainer.getComponentInstanceOfType(DocumentReaderService.class);
   }

   public void testGetContentAsStringTemplate() throws Exception
   {
      InputStream is = TestMSWordDocumentReader.class.getResourceAsStream("/test.dot");
      String text = service_.getDocumentReader("application/msworddot").getContentAsText(is);
      System.out.println("text [" + text + "]");
      String etalon = "exotest";
      System.out.println("etalon [" + etalon + "]");

      System.out.println("[" + text.length() + "] [" + etalon.length() + "]");
      assertEquals("Wrong string returned", etalon, text);
   }

   public void testGetContentAsStringDoc() throws Exception
   {
      InputStream is = TestMSWordDocumentReader.class.getResourceAsStream("/test.doc");
      String text = service_.getDocumentReader("application/msword").getContentAsText(is);
      System.out.println("text [" + text + "]");

      /*
       * String etalon = "Hello.\n" +"This is the test document 12345\n"
       * +"Table\n" +"Title One Two\n" +"Hello_Title Hello_One Hello_Two\n";
       * System.out.println("etalon ["+etalon+"]");
       * System.out.println("["+text.length()+"] ["+etalon.length()+"]");
       * assertEquals("Wrong string returned",etalon ,text );
       */
   }
}
