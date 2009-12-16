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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by The eXo Platform SAS A parser of XML files.
 * 
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 * @version March 07, 2006
 */
public class XMLDocumentReader extends BaseDocumentReader
{

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
         throw new NullPointerException("InputStream is null.");
      }
      try
      {
         byte[] buffer = new byte[2048];
         int len;
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         while ((len = is.read(buffer)) > 0)
            bos.write(buffer, 0, len);
         bos.close();
         String xml = new String(bos.toByteArray());
         return delete(xml);
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
      }
      return new Properties();
   }

   /**
    * Cleans the string from tags.
    * 
    * @param str the string which contain a text with user's tags.
    * @return The string cleaned from user's tags and their bodies.
    */
   private String delete(String str)
   {
      try
      {
         StringBuffer input = new StringBuffer(str);
         String patternString = "<+[^>]*>+";
         Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
         Matcher matcher = pattern.matcher(input);
         while (matcher.find())
         {
            int start = matcher.start();
            int end = matcher.end();
            input.delete(start, end);
            matcher = pattern.matcher(input);
         }
         return input.substring(0, input.length());
      }
      catch (PatternSyntaxException e)
      {
      }
      return "";
   }

}
