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

import junit.framework.Assert;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CommentFilterObject implements CommentListener
{

   /** . */
   private final LinkedList<String> comments = new LinkedList<String>();

   /** . */
   private final CommentFilter filter;

   public CommentFilterObject(String s)
   {
      this.filter = new CommentFilter(new StringReader(s), this);
   }

   public void assertNoComment()
   {
      Assert.assertEquals(0, comments.size());
   }

   public void assertComment(String expectedComment)
   {
      assertComment(new String[]{expectedComment});
   }

   public void assertComment(String[] expectedComments)
   {
      Assert.assertEquals(expectedComments.length, comments.size());
      Iterator<String> i = comments.iterator();
      for (String comment : expectedComments)
      {
         Assert.assertEquals(comment, i.next());
      }
      comments.clear();
   }

   public void readChars(String s)
   {
      String chars = readChars(s.length(), s.length());
      Assert.assertEquals(s, chars);
   }

   public void readChars(int charsToRead, String s)
   {
      if (charsToRead < s.length())
      {
         Assert.fail();
      }
      String chars = readChars(charsToRead, s.length());
      Assert.assertEquals(s, chars);
   }

   public String readChars(int charsToRead)
   {
      return readChars(charsToRead, charsToRead);
   }

   public String readChars(int wantedLength, int expectedLength)
   {
      try
      {
         char[] chars = new char[wantedLength];
         Assert.assertEquals(expectedLength, filter.read(chars, 0, wantedLength));
         if (expectedLength == -1)
         {
            return null;
         }
         else
         {
            return new String(chars, 0, expectedLength);
         }
      }
      catch (IOException e)
      {
         throw new AssertionError(e);
      }
   }

   public void comment(String text)
   {
      comments.addLast(text);
   }

}
