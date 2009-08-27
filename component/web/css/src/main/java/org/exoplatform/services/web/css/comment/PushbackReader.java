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
package org.exoplatform.services.web.css.comment;

import java.io.IOException;
import java.io.Reader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PushbackReader extends Reader
{

   private final Reader reader;

   private final StringBuilder builder;

   private boolean endReached;

   PushbackReader(Reader reader)
   {
      this.reader = reader;
      this.builder = new StringBuilder();
      this.endReached = false;
   }

   public int read(char cbuf[], int off, int len) throws IOException
   {
      // Copy anything we could have in our buffer
      int count = Math.min(len, builder.length());
      for (int i = 0; i < count; i++)
      {
         cbuf[off++] = builder.charAt(i);
      }
      len -= count;
      builder.delete(0, count);

      //
      if (endReached)
      {
         if (count == 0)
         {
            return -1;
         }
         else
         {
            return count;
         }
      }
      else
      {
         int tmp = reader.read(cbuf, off, len);
         if (tmp == -1)
         {
            endReached = true;
            if (count == 0)
            {
               return -1;
            }
            else
            {
               return count;
            }
         }
         else
         {
            return count + tmp;
         }
      }
   }

   public void pushback(char[] chars)
   {
      builder.insert(0, chars);
   }

   public void pushback(char c)
   {
      builder.insert(0, c);
   }

   public void pushback(String s)
   {
      builder.insert(0, s);
   }

   public void close() throws IOException
   {
      reader.close();
   }
}
