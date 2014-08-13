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

import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
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

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.document.MSXWordDocumentReader");

   /**
    * @see org.exoplatform.services.document.DocumentReader#getMimeTypes()
    */
   public String[] getMimeTypes()
   {
      //Supported document types:
      // "application/vnd.openxmlformats-officedocument.wordprocessingml.document" - "x.docx"
      // "application/vnd.openxmlformats-officedocument.wordprocessingml.template" - "x.dotx"
      // "application/vnd.ms-word.document.macroenabled.12" - "x.docm"
      // "application/vnd.ms-word.template.macroenabled.12" - "x.dotm"

      return new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
         "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
         "application/vnd.ms-word.document.macroenabled.12", "application/vnd.ms-word.template.macroenabled.12"};
   }

   /**
    * Returns only a text from .docx file content.
    * 
    * @param is an input stream with .docx file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(final InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new IllegalArgumentException("InputStream is null.");
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
            doc = SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<XWPFDocument>()
            {
               public XWPFDocument run() throws Exception
               {
                  return new XWPFDocument(is);
               }
            });
         }
         catch (IOException e)
         {
            throw new DocumentReadException("Can't open message.", e);
         }
         catch (OpenXML4JRuntimeException e)
         {
            throw new DocumentReadException("Can't open message.", e);
         }

         final XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
         text = SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
         {
            public String run()
            {
               return extractor.getText();
            }
         });
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
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + e.getMessage());
               }
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
   public Properties getProperties(final InputStream is) throws IOException, DocumentReadException
   {
      try {
         OPCPackage container = SecurityHelper
             .doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<OPCPackage>() {
                public OPCPackage run() throws Exception {
                   return OPCPackage.open(is);
                }
             });
         POIXMLProperties xmlProperties = new POIXMLProperties(container);
         POIPropertiesReader reader = new POIPropertiesReader();
         reader.readDCProperties(xmlProperties);
         return reader.getProperties();
      } catch (InvalidFormatException e) {
         throw new DocumentReadException("The format of the document to read is invalid.", e);
      } catch (XmlException e) {
         throw new DocumentReadException("Problem during the document parsing.", e);
      } catch (OpenXML4JException e) {
         throw new DocumentReadException("Problem during the document parsing.", e);
      }
   }

}
