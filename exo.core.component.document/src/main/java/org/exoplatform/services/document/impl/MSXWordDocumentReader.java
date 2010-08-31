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
package org.exoplatform.services.document.impl;

import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.exoplatform.services.document.DocumentReadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS A parser of Microsoft Word 2007 files (docx).
 * 
 * @author <a href="mailto:phunghainam@gmail.com">Phung Hai Nam</a>
 * @author Gennady Azarenkov
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: MSXWordDocumentReader.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class MSXWordDocumentReader extends BaseDocumentReader
{

   /**
    * @see org.exoplatform.services.document.DocumentReader#getMimeTypes()
    */
   public String[] getMimeTypes()
   {
      return new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
   }

   /**
    * Returns only a text from .docx file content.
    * 
    * @param is an input stream with .docx file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new NullPointerException("InputStream is null.");
      }
      String text = "";
      try
      {
         if (is.available() == 0)
         {
            return "";
         }
         
         XWPFDocument doc;
         try
         {
            doc = new XWPFDocument(is);
         }
         catch (IOException e)
         {
            throw new DocumentReadException("Can't open message.", e);
         }
         catch (OpenXML4JRuntimeException e)
         {
            throw new DocumentReadException("Can't open message.", e);
         }

         XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
         text = extractor.getText();
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
            }
         }
      }
      return text.trim();
   }

   /**
    * @see org.exoplatform.services.document.DocumentReader#getContentAsText(java.io.InputStream, java.lang.String)
    */
   public String getContentAsText(InputStream is, String encoding) throws IOException, DocumentReadException
   {
      // Ignore encoding
      return getContentAsText(is);
   }

   /**
    * @see org.exoplatform.services.document.DocumentReader#getProperties(java.io.InputStream)
    */
   public Properties getProperties(InputStream is) throws IOException, DocumentReadException
   {
      POIPropertiesReader reader = new POIPropertiesReader();
      reader.readDCProperties(new XWPFDocument(is));
      return reader.getProperties();
   }

}
