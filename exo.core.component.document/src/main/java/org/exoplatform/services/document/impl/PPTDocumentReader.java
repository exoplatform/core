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

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS A parser of Microsoft PowerPoint files.
 * Parses the files with application/powerpoint mime type.
 * 
 * @author <a href="mailto:phunghainam@gmail.com">Phung Hai Nam</a>
 * @author Gennady Azarenkov
 * @version Oct 19, 2005
 */
public class PPTDocumentReader extends BaseDocumentReader
{

   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.services.document.impl.PPTDocumentReader");

   /**
    * Get the application/powerpoint mime type.
    * 
    * @return The application/powerpoint mime type.
    */
   public String[] getMimeTypes()
   {
      return new String[]{"application/powerpoint", "application/ppt", "application/vnd.ms-powerpoint"};
   }

   /**
    * Returns only a text from .ppt file content.
    * 
    * @param is an input stream with .ppt file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(InputStream is) throws IOException, DocumentReadException
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
         
         PowerPointExtractor ppe;
         try
         {
            ppe = new PowerPointExtractor(is);
         }
         catch (IOException e)
         {
            throw new DocumentReadException("Can't open presentation.", e);
         }
         return ppe.getText(true, true);
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
   public Properties getProperties(InputStream is) throws IOException, DocumentReadException
   {
      POIPropertiesReader reader = new POIPropertiesReader();
      reader.readDCProperties(is);
      return reader.getProperties();
   }

}
