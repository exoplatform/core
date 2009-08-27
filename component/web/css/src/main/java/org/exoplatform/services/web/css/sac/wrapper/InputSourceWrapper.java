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
package org.exoplatform.services.web.css.sac.wrapper;

import org.exoplatform.services.web.css.comment.CommentFilter;
import org.exoplatform.services.web.css.comment.CommentListener;
import org.w3c.css.sac.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class InputSourceWrapper extends InputSource
{

   private final CommentFilter reader;

   InputSourceWrapper(InputSource wrapped, CommentListener listener) throws IOException
   {
      super();

      //
      reader = createReader(wrapped, listener);

      //
      setMedia(wrapped.getMedia());
      setTitle(wrapped.getTitle());
      setURI(wrapped.getURI());
   }

   public Reader getCharacterStream()
   {
      return reader;
   }

   public void setByteStream(InputStream inputStream)
   {
      throw new UnsupportedOperationException();
   }

   public void setCharacterStream(Reader reader)
   {
      throw new UnsupportedOperationException();
   }

   public void setEncoding(String s)
   {
      throw new UnsupportedOperationException();
   }

   private static CommentFilter createReader(InputSource source, CommentListener listener) throws IOException
   {
      Reader reader;
      if (source.getCharacterStream() != null)
      {
         reader = source.getCharacterStream();
      }
      else if (source.getByteStream() != null)
      {
         reader = new InputStreamReader(source.getByteStream(), source.getEncoding());
      }
      else
      {
         throw new IllegalArgumentException("Bad input source");
      }
      return new CommentFilter(reader, listener);
   }

}
