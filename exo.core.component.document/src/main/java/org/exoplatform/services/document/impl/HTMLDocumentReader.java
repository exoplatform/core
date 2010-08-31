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

import org.exoplatform.services.document.DocumentReadException;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS A parser of HTML files.
 * 
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 * @version March 04, 2006
 */
public class HTMLDocumentReader extends BaseDocumentReader
{

   /**
    * Initializes a newly created object for text/html files format parsing.
    * 
    * @param params the container parameters.
    */
   public HTMLDocumentReader()
   {
   }

   /**
    * Get the text/html,application/x-groovy+html mime type.
    * 
    * @return The string with text/html,application/x-groovy+html mime type.
    */
   public String[] getMimeTypes()
   {
      return new String[]{"text/html", "application/x-groovy+html"};
   }

   /**
    * Returns a text from html file content without user's tags and their bodies.
    * 
    * @param is an input stream with html file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new NullPointerException("InputStream is null.");
      }

      String refined_text = new String();
      try
      {
         byte[] buffer = new byte[2048];
         int len;
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         while ((len = is.read(buffer)) > 0)
         {
            bos.write(buffer, 0, len);
         }
         bos.close();

         String html = new String(bos.toByteArray());

         Parser parser = Parser.createParser(html, null);
         StringBean sb = new StringBean();

         // read links or not
         // sb.setLinks(true); //TODO make this configurable

         // extract text
         parser.visitAllNodesWith(sb);

         String text = sb.getStrings();
         refined_text = (text != null) ? text : ""; // delete(text);

      }
      catch (ParserException e)
      {
         throw new DocumentReadException(e.getMessage(), e);
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

      return refined_text;
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
      }
      return new Properties();
   }
}
