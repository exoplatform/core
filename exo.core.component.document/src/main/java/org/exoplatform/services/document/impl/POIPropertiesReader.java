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

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLPropertiesTextExtractor;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.exoplatform.services.document.DCMetaData;
import org.exoplatform.services.document.DocumentReadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class POIPropertiesReader
{

   private final Properties props = new Properties();

   public Properties getProperties()
   {
      return props;
   }

   /**
    * Metadata extraction from OLE2 documents (legacy MS office file formats)
    * 
    * @param is
    * @return
    * @throws IOException
    * @throws DocumentReadException
    */
   public Properties readDCProperties(InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new NullPointerException("InputStream is null.");
      }

      @SuppressWarnings("serial")
      class POIRuntimeException extends RuntimeException
      {
         private Throwable ex;

         public POIRuntimeException(Throwable ex)
         {
            this.ex = ex;
         }

         public Throwable getException()
         {
            return ex;
         }
      }

      POIFSReaderListener readerListener = new POIFSReaderListener()
      {
         public void processPOIFSReaderEvent(final POIFSReaderEvent event)
         {

            PropertySet ps;
            try
            {
               ps = PropertySetFactory.create(event.getStream());

               if (ps instanceof SummaryInformation)
               {
                  SummaryInformation si = (SummaryInformation)ps;

                  if (si.getLastAuthor() != null && si.getLastAuthor().length() > 0)
                  {
                     props.put(DCMetaData.CONTRIBUTOR, si.getLastAuthor());
                  }
                  if (si.getComments() != null && si.getComments().length() > 0)
                  {
                     props.put(DCMetaData.DESCRIPTION, si.getComments());
                  }
                  if (si.getCreateDateTime() != null)
                  {
                     props.put(DCMetaData.DATE, si.getCreateDateTime());
                  }
                  if (si.getAuthor() != null && si.getAuthor().length() > 0)
                  {
                     props.put(DCMetaData.CREATOR, si.getAuthor());
                  }
                  if (si.getKeywords() != null && si.getKeywords().length() > 0)
                  {
                     props.put(DCMetaData.SUBJECT, si.getKeywords());
                  }
                  if (si.getLastSaveDateTime() != null)
                  {
                     props.put(DCMetaData.DATE, si.getLastSaveDateTime());
                  }
                  // if(docInfo.getProducer() != null)
                  // props.put(DCMetaData.PUBLISHER, docInfo.getProducer());
                  if (si.getSubject() != null && si.getSubject().length() > 0)
                  {
                     props.put(DCMetaData.SUBJECT, si.getSubject());
                  }
                  if (si.getTitle() != null && si.getTitle().length() > 0)
                  {
                     props.put(DCMetaData.TITLE, si.getTitle());
                  }

               }
            }
            catch (NoPropertySetStreamException e)
            {
               throw new POIRuntimeException(new DocumentReadException(e.getMessage(), e));
            }
            catch (MarkUnsupportedException e)
            {
               throw new POIRuntimeException(new DocumentReadException(e.getMessage(), e));
            }
            catch (UnsupportedEncodingException e)
            {
               throw new POIRuntimeException(new DocumentReadException(e.getMessage(), e));
            }
            catch (IOException e)
            {
               throw new POIRuntimeException(e);
            }
         }
      };

      try
      {
         POIFSReader poiFSReader = new POIFSReader();
         poiFSReader.registerListener(readerListener, SummaryInformation.DEFAULT_STREAM_NAME);
         poiFSReader.read(is);
      }
      catch (POIRuntimeException e)
      {
         Throwable ex = e.getException();
         if (ex instanceof IOException)
         {
            throw (IOException)ex;
         }
         else
         {
            throw (DocumentReadException)ex;
         }
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

      return props;
   }

   /**
    * Metadata extraction from ooxml documents (MS 2007 office file formats)
    * 
    * @param document
    * @return
    * @throws IOException
    * @throws DocumentReadException
    */
   public Properties readDCProperties(POIXMLDocument document) throws IOException, DocumentReadException
   {

      POIXMLPropertiesTextExtractor extractor = new POIXMLPropertiesTextExtractor(document);

      CoreProperties coreProperties = extractor.getCoreProperties();

      Nullable<String> lastModifiedBy = coreProperties.getUnderlyingProperties().getLastModifiedByProperty();
      if (lastModifiedBy != null && lastModifiedBy.getValue() != null && lastModifiedBy.getValue().length() > 0)
      {
         props.put(DCMetaData.CONTRIBUTOR, lastModifiedBy.getValue());
      }
      if (coreProperties.getDescription() != null && coreProperties.getDescription().length() > 0)
      {
         props.put(DCMetaData.DESCRIPTION, coreProperties.getDescription());
      }
      if (coreProperties.getCreated() != null)
      {
         props.put(DCMetaData.DATE, coreProperties.getCreated());
      }
      if (coreProperties.getCreator() != null && coreProperties.getCreator().length() > 0)
      {
         props.put(DCMetaData.CREATOR, coreProperties.getCreator());
      }
      if (coreProperties.getSubject() != null && coreProperties.getSubject().length() > 0)
      {
         props.put(DCMetaData.SUBJECT, coreProperties.getSubject());
      }
      if (coreProperties.getModified() != null)
      {
         props.put(DCMetaData.DATE, coreProperties.getModified());
      }
      if (coreProperties.getSubject() != null && coreProperties.getSubject().length() > 0)
      {
         props.put(DCMetaData.SUBJECT, coreProperties.getSubject());
      }
      if (coreProperties.getTitle() != null && coreProperties.getTitle().length() > 0)
      {
         props.put(DCMetaData.TITLE, coreProperties.getTitle());
      }

      return props;
   }

}
