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

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.util.PDFTextStripper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.document.DCMetaData;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS A parser of Adobe PDF files.
 * 
 * @author Phung Hai Nam
 * @author Gennady Azarenkov
 * @version Oct 19, 2005
 */
public class PDFDocumentReader extends BaseDocumentReader
{

   protected static Log log = ExoLogger.getLogger("exo.core.component.document.PDFDocumentReader");

   /**
    * Get the application/pdf mime type.
    * 
    * @return The application/pdf mime type.
    */
   public String[] getMimeTypes()
   {
      return new String[]{"application/pdf"};
   }

   /**
    * Returns only a text from pdf file content.
    * 
    * @param is an input stream with .pdf file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(final InputStream is) throws IOException, DocumentReadException
   {

      try
      {
         return SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<String>()
         {
            public String run() throws Exception
            {
               if (is == null)
               {
                  throw new NullPointerException("InputStream is null.");
               }
               PDDocument pdDocument = null;
               StringWriter sw = new StringWriter();
               try
               {
                  if (is.available() == 0)
                     return "";

                  try
                  {
                     pdDocument = PDDocument.load(is);
                  }
                  catch (IOException e)
                  {
                     throw new DocumentReadException("Can not load PDF document.", e);
                  }

                  PDFTextStripper stripper = new PDFTextStripper();
                  stripper.setStartPage(1);
                  stripper.setEndPage(Integer.MAX_VALUE);
                  stripper.writeText(pdDocument, sw);
               }
               finally
               {
                  if (pdDocument != null)
                     try
                     {
                        pdDocument.close();
                     }
                     catch (IOException e)
                     {
                     }
                  if (is != null)
                     try
                     {
                        is.close();
                     }
                     catch (IOException e)
                     {
                     }
               }
               return sw.toString();
            }
         });

      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IOException)
         {
            throw (IOException)cause;
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
      try
      {
         return SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Properties>()
         {
            public Properties run() throws Exception
            {
               if (is == null)
               {
                  throw new NullPointerException("InputStream is null.");
               }

               PDDocument pdDocument = PDDocument.load(is);
               Properties props = new Properties();
               try
               {
                  if (pdDocument.isEncrypted())
                  {
                     try
                     {
                        pdDocument.decrypt("");
                     }
                     catch (InvalidPasswordException e)
                     {
                        throw new DocumentReadException("The pdf document is encrypted.", e);
                     }
                     catch (org.apache.pdfbox.exceptions.CryptographyException e)
                     {
                        throw new DocumentReadException(e.getMessage(), e);
                     }
                  }

                  PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
                  PDMetadata meta = catalog.getMetadata();
                  if (meta != null)
                  {
                     XMPMetadata metadata = meta.exportXMPMetadata();

                     XMPSchemaDublinCore dc = metadata.getDublinCoreSchema();
                     if (dc != null)
                     {
                        try
                        {
                           if (dc.getTitle() != null)
                              props.put(DCMetaData.TITLE, dc.getTitle());
                        }
                        catch (Exception e)
                        {
                           log.warn("getTitle failed: " + e);
                        }
                        try
                        {
                           if (dc.getDescription() != null)
                              props.put(DCMetaData.DESCRIPTION, dc.getDescription());
                        }
                        catch (Exception e)
                        {
                           log.warn("getSubject failed: " + e);
                        }

                        try
                        {
                           if (dc.getCreators() != null)
                           {
                              for (String creator : dc.getCreators())
                              {
                                 props.put(DCMetaData.CREATOR, creator);
                              }
                           }
                        }
                        catch (Exception e)
                        {
                           log.warn("getCreator failed: " + e);
                        }

                        try
                        {
                           if (dc.getDates() != null)
                           {
                              for (Calendar date : dc.getDates())
                              {
                                 props.put(DCMetaData.DATE, date);
                              }
                           }
                        }
                        catch (Exception e)
                        {
                           log.warn("getDate failed: " + e);
                        }
                     }

                     XMPSchemaPDF pdf = metadata.getPDFSchema();
                     if (pdf != null)
                     {
                        try
                        {
                           if (pdf.getKeywords() != null)
                              props.put(DCMetaData.SUBJECT, pdf.getKeywords());
                        }
                        catch (Exception e)
                        {
                           log.warn("getKeywords failed: " + e);
                        }

                        try
                        {
                           if (pdf.getProducer() != null)
                              props.put(DCMetaData.PUBLISHER, pdf.getProducer());
                        }
                        catch (Exception e)
                        {
                           log.warn("getProducer failed: " + e);
                        }
                     }

                     XMPSchemaBasic basic = metadata.getBasicSchema();
                     if (basic != null)
                     {
                        try
                        {
                           if (basic.getCreateDate() != null)
                              props.put(DCMetaData.DATE, basic.getCreateDate());
                        }
                        catch (Exception e)
                        {
                           log.warn("getCreationDate failed: " + e);
                        }
                        try
                        {
                           if (basic.getModifyDate() != null)
                              props.put(DCMetaData.DATE, basic.getModifyDate());
                        }
                        catch (Exception e)
                        {
                           log.warn("getModificationDate failed: " + e);
                        }
                        // try
                        // {
                        // if (basic.getCreatorTool() != null)
                        // props.put(DCMetaData.PUBLISHER, basic.getCreatorTool());
                        // }
                        // catch (Exception e)
                        // {
                        // log.warn("getCreatorTool failed: " + e);
                        // }
                     }
                  }
                  else
                  {
                     // The pdf doesn't contain any metadata, try to use the document
                     // information instead
                     PDDocumentInformation docInfo = pdDocument.getDocumentInformation();

                     if (docInfo != null)
                     {
                        try
                        {
                           if (docInfo.getAuthor() != null)
                              props.put(DCMetaData.CONTRIBUTOR, docInfo.getAuthor());
                        }
                        catch (Exception e)
                        {
                           log.warn("getAuthor failed: " + e);
                        }
                        try
                        {
                           if (docInfo.getCreationDate() != null)
                              props.put(DCMetaData.DATE, docInfo.getCreationDate());
                        }
                        catch (Exception e)
                        {
                           log.warn("getCreationDate failed: " + e);
                        }
                        try
                        {
                           if (docInfo.getCreator() != null)
                              props.put(DCMetaData.CREATOR, docInfo.getCreator());
                        }
                        catch (Exception e)
                        {
                           log.warn("getCreator failed: " + e);
                        }
                        try
                        {

                           if (docInfo.getKeywords() != null)
                              props.put(DCMetaData.SUBJECT, docInfo.getKeywords());
                        }
                        catch (Exception e)
                        {
                           log.warn("getKeywords failed: " + e);
                        }
                        try
                        {
                           if (docInfo.getModificationDate() != null)
                              props.put(DCMetaData.DATE, docInfo.getModificationDate());
                        }
                        catch (Exception e)
                        {
                           log.warn("getModificationDate failed: " + e);
                        }
                        try
                        {
                           if (docInfo.getProducer() != null)
                              props.put(DCMetaData.PUBLISHER, docInfo.getProducer());
                        }
                        catch (Exception e)
                        {
                           log.warn("getProducer failed: " + e);
                        }
                        try
                        {
                           if (docInfo.getSubject() != null)
                              props.put(DCMetaData.DESCRIPTION, docInfo.getSubject());
                        }
                        catch (Exception e)
                        {
                           log.warn("getSubject failed: " + e);
                        }
                        try
                        {
                           if (docInfo.getTitle() != null)
                              props.put(DCMetaData.TITLE, docInfo.getTitle());
                        }
                        catch (Exception e)
                        {
                           log.warn("getTitle failed: " + e);
                        }

                        // docInfo.getTrapped();
                     }
                  }
               }
               finally
               {
                  if (pdDocument != null)
                  {
                     pdDocument.close();
                  }

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
         });

      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IOException)
         {
            throw (IOException)cause;
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
   }

}
