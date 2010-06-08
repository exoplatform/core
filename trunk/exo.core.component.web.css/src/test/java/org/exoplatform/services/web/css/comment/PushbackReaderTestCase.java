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

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PushbackReaderTestCase extends TestCase
{

   public PushbackReaderTestCase()
   {
   }

   public PushbackReaderTestCase(String s)
   {
      super(s);
   }

   public void testA() throws IOException
   {
      PushbackReader reader = createReader("foo");
      char[] chars = new char[3];
      assertEquals(3, reader.read(chars));
      assertEquals("foo", new String(chars));
      chars = new char[1];
      assertEquals(-1, reader.read(chars));
   }

   public void testB() throws IOException
   {
      PushbackReader reader = createReader();
      char[] chars = new char[1];
      assertEquals(-1, reader.read(chars));
   }

   public void testC() throws IOException
   {
      PushbackReader reader = createReader();
      char[] chars = new char[1];
      assertEquals(-1, reader.read(chars));
      reader.pushback("foo");
      chars = new char[3];
      assertEquals(3, reader.read(chars));
      assertEquals("foo", new String(chars));
      chars = new char[1];
      assertEquals(-1, reader.read(chars));
   }

   public void testD() throws IOException
   {
      PushbackReader reader = createReader("bar");
      reader.pushback("foo");
      char[] chars = new char[6];
      assertEquals(6, reader.read(chars));
      assertEquals("foobar", new String(chars));
      chars = new char[1];
      assertEquals(-1, reader.read(chars));
   }

   private PushbackReader createReader(String s)
   {
      return new PushbackReader(new StringReader(s));
   }

   private PushbackReader createReader()
   {
      return new PushbackReader(new StringReader(""));
   }

}
