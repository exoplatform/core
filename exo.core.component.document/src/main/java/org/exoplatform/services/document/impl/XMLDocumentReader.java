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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by The eXo Platform SAS A parser of XML files.
 * 
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 * @version March 07, 2006
 */
public class XMLDocumentReader extends BaseDocumentReader
{

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.document.XMLDocumentReader");

   /**
    * Get the text/xml, application/xml, application/x-google-gadget mime types.
    * 
    * @return The string with text/xml,  application/xml, application/x-google-gadget mime type.
    */
   public String[] getMimeTypes()
   {
      return new String[]{"text/xml", "application/xml", "application/x-google-gadget"};
   }

   /**
    * Returns a text from xml file content which situated between tags.
    * 
    * @param is an input stream with html file content.
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

         //         byte[] buffer = new byte[2048];
         //         int len;
         //         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         //         while ((len = is.read(buffer)) > 0)
         //            bos.write(buffer, 0, len);
         //         bos.close();
         //         String xml = new String(bos.toByteArray());
         return parse(is);
      }
      finally
      {
         if (is != null)
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
      return new Properties();
   }

   /**
    * Cleans the string from tags.
    * 
    * @param str the string which contain a text with user's tags.
    * @return The string cleaned from user's tags and their bodies.
    */
   private String parse(InputStream is)
   {
      final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      //      saxParserFactory.setNamespaceAware(true);
      //      saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      SAXParser saxParser;
      StringWriter writer = new StringWriter();

      DefaultHandler dh = new WriteOutContentHandler(writer);
      try
      {
         saxParser =
            SecurityHelper
               .doPrivilegedParserConfigurationOrSAXExceptionAction(new PrivilegedExceptionAction<SAXParser>()
            {
               public SAXParser run() throws Exception
               {
                  return saxParserFactory.newSAXParser();
               }
            });
         saxParser.parse(is, dh);
      }
      catch (SAXException e)
      {
         return "";
      }
      catch (IOException e)
      {
         return "";
      }
      catch (ParserConfigurationException e)
      {
         return "";
      }

      return writer.toString();

   }

   class WriteOutContentHandler extends DefaultHandler
   {
      private final Writer writer;

      public WriteOutContentHandler(Writer writer)
      {
         this.writer = writer;
      }

      /**
       * Writes the given characters to the given character stream.
       */
      @Override
      public void characters(char[] ch, int start, int length) throws SAXException
      {
         try
         {
            writer.write(ch, start, length);
         }
         catch (IOException e)
         {
            throw new SAXException(e.getMessage(), e);
         }
      }

      @Override
      public void endDocument() throws SAXException
      {
         try
         {
            writer.flush();
         }
         catch (IOException e)
         {
            throw new SAXException(e.getMessage(), e);
         }
      }
   }

}
