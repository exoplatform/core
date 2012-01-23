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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.xmlbeans.XmlException;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS A parser of Microsoft PowerPoint 2007 files (pptx).
 * 
 * @author <a href="mailto:phunghainam@gmail.com">Phung Hai Nam</a>
 * @author Gennady Azarenkov
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: MSXPPTDocumentReader.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 */
public class MSXPPTDocumentReader extends BaseDocumentReader
{

   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.services.document.impl.MSXPPTDocumentReader");

   /**
    * @see org.exoplatform.services.document.DocumentReader#getMimeTypes()
    */
   public String[] getMimeTypes()
   {
      //Supported mimetypes:
      // "application/vnd.openxmlformats-officedocument.presentationml.presentation" -"x.pptx";
      // "application/vnd.openxmlformats-officedocument.presentationml.slideshow" - "x.ppsx";
      // "application/vnd.ms-powerpoint.presentation.macroenabled.12" - "testPPT.pptm";
      // "application/vnd.ms-powerpoint.slideshow.macroenabled.12" - "testPPT.ppsm";
      //
      //Not supported mimetypes:
      // "application/vnd.ms-powerpoint.template.macroenabled.12" - "testPPT.potm"; Has errors
      // "application/vnd.openxmlformats-officedocument.presentationml.template" - "x.potx"; Not tested
      // "application/vnd.ms-powerpoint.addin.macroenabled.12" - "x.ppam"; Not tested

      return new String[]{"application/vnd.openxmlformats-officedocument.presentationml.presentation",
         "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
         "application/vnd.ms-powerpoint.presentation.macroenabled.12",
         "application/vnd.ms-powerpoint.slideshow.macroenabled.12"};
   }

   /**
    * Returns only a text from .pptx file content.
    * 
    * @param is an input stream with .pptx file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(final InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new IllegalArgumentException("InputStream is null.");
      }
      try
      {
         if (is.available() == 0)
         {
            return "";
         }
         
         final XSLFPowerPointExtractor ppe;
         try
         {
            ppe = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<XSLFPowerPointExtractor>()
            {
               public XSLFPowerPointExtractor run() throws Exception
               {
                  return new XSLFPowerPointExtractor(OPCPackage.open(is));
               }
            });
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof IOException)
            {
               throw new DocumentReadException("Can't open presentation.", cause);
            }
            else if (cause instanceof OpenXML4JRuntimeException)
            {
               throw new DocumentReadException("Can't open presentation.", cause);
            }
            else if (cause instanceof OpenXML4JException)
            {
               throw new DocumentReadException("Can't open presentation.", cause);
            }
            else if (cause instanceof XmlException)
            {
               throw new DocumentReadException("Can't open presentation.", cause);
            }
            else if (cause instanceof RuntimeException)
            {
               throw (RuntimeException)cause;
            }
            else
            {
               throw new RuntimeException(cause);
            }
         }
         return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
         {
            public String run()
            {
               return ppe.getText(true, true);
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
   }

   public String getContentAsText(InputStream is, String encoding) throws IOException, DocumentReadException
   {
      // Ignore encoding
      return getContentAsText(is);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.exoplatform.services.document.DocumentReader#getProperties(java.io.
    *      InputStream)
    */
   public Properties getProperties(final InputStream is) throws IOException, DocumentReadException
   {
      final POIPropertiesReader reader = new POIPropertiesReader();
      try
      {
         SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
         {
            public Void run() throws Exception
            {
               reader.readDCProperties(new XSLFSlideShow(OPCPackage.open(is)));
               return null;
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof InvalidFormatException)
         {
            throw new DocumentReadException("Can't read properties from OOXML document", cause);
         }
         else if (cause instanceof OpenXML4JException)
         {
            throw new DocumentReadException("Can't read properties from OOXML document", cause);
         }
         else if (cause instanceof XmlException)
         {
            throw new DocumentReadException("Can't read properties from OOXML document", cause);
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
      return reader.getProperties();
   }

}
