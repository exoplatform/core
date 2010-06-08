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
package org.exoplatform.services.web.css.model;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LexicalUnitTestCase extends TestCase
{

   public void testInitialState()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      assertEquals(null, a.getPrevious());
      assertEquals(null, a.getNext());
   }

   public void testAttachNext()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      a.attachNext(b);
      assertSame(b, a.getNext());
      assertEquals(null, a.getPrevious());
      assertEquals(null, b.getNext());
      assertSame(a, b.getPrevious());
   }

   public void testAttachPrevious()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      b.attachPrevious(a);
      assertSame(b, a.getNext());
      assertEquals(null, a.getPrevious());
      assertEquals(null, b.getNext());
      assertSame(a, b.getPrevious());
   }

   public void testDetachNext()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      try
      {
         a.detachNext();
         fail();
      }
      catch (IllegalStateException e)
      {
         assertEquals(null, a.getNext());
      }
   }

   public void testDetachPrevious()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      try
      {
         a.detachPrevious();
         fail();
      }
      catch (IllegalStateException e)
      {
         assertEquals(null, a.getPrevious());
      }
   }

   public void testHasNext()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      assertFalse(a.hasNext());
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      a.attachNext(b);
      assertTrue(a.hasNext());
      a.detachNext();
      assertFalse(a.hasNext());
   }

   public void testHasPrevious()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      assertFalse(a.hasPrevious());
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      a.attachPrevious(b);
      assertTrue(a.hasPrevious());
      a.detachPrevious();
      assertFalse(a.hasPrevious());
   }

   public void testDetach1()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject c = new IntegerLexicalUnitObject((short)0, 0);
      b.attachPrevious(a);
      b.attachNext(c);
      b.detach();
      assertEquals(null, a.getPrevious());
      assertEquals(null, a.getNext());
      assertEquals(null, b.getPrevious());
      assertEquals(null, b.getNext());
      assertEquals(null, c.getPrevious());
      assertEquals(null, c.getNext());
   }

   public void testDetach2()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      b.attachPrevious(a);
      b.detach();
      assertEquals(null, a.getPrevious());
      assertEquals(null, a.getNext());
      assertEquals(null, b.getPrevious());
      assertEquals(null, b.getNext());
   }

   public void testDetach3()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      b.attachPrevious(a);
      a.detach();
      assertEquals(null, a.getPrevious());
      assertEquals(null, a.getNext());
      assertEquals(null, b.getPrevious());
      assertEquals(null, b.getNext());
   }

   public void testIterator()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject c = new IntegerLexicalUnitObject((short)0, 0);
      b.attachPrevious(a);
      b.attachNext(c);
      assertEquals(new ArrayList<LexicalUnitObject>(), list(a.previous()));
      assertEquals(Arrays.asList(b, c), list(a.next()));
      assertEquals(Arrays.asList(a), list(b.previous()));
      assertEquals(Arrays.asList(c), list(b.next()));
      assertEquals(Arrays.asList(b, a), list(c.previous()));
      assertEquals(new ArrayList<LexicalUnitObject>(), list(c.next()));
      assertEquals(new ArrayList<LexicalUnitObject>(), list(a.previous(false)));
      assertEquals(Arrays.asList(b, c), list(a.next(false)));
      assertEquals(Arrays.asList(a), list(b.previous(false)));
      assertEquals(Arrays.asList(c), list(b.next(false)));
      assertEquals(Arrays.asList(b, a), list(c.previous()));
      assertEquals(new ArrayList<LexicalUnitObject>(), list(c.next()));
      assertEquals(Arrays.asList(a), list(a.previous(true)));
      assertEquals(Arrays.asList(a, b, c), list(a.next(true)));
      assertEquals(Arrays.asList(b, a), list(b.previous(true)));
      assertEquals(Arrays.asList(b, c), list(b.next(true)));
      assertEquals(Arrays.asList(c, b, a), list(c.previous(true)));
      assertEquals(Arrays.asList(c), list(c.next(true)));
   }

   public void testEquals1()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)1, 3);
      LexicalUnitObject c = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject d = new IntegerLexicalUnitObject((short)1, 3);
      a.attachNext(b);
      c.attachNext(d);
      assertTrue(a.equals(c));
   }

   public void testEquals2()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)1, 3);
      LexicalUnitObject c = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject d = new IntegerLexicalUnitObject((short)2, 3);
      a.attachNext(b);
      c.attachNext(d);
      assertFalse(a.equals(c));
   }

   public void testEquals3()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject b = new IntegerLexicalUnitObject((short)1, 3);
      LexicalUnitObject c = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject d = new IntegerLexicalUnitObject((short)1, 4);
      a.attachNext(b);
      c.attachNext(d);
      assertFalse(a.equals(c));
   }

   public void testEquals4()
   {
      LexicalUnitObject a = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject c = new IntegerLexicalUnitObject((short)0, 0);
      LexicalUnitObject d = new IntegerLexicalUnitObject((short)1, 3);
      c.attachNext(d);
      assertFalse(a.equals(c));
   }

   private <T> List<T> list(Iterable<T> i)
   {
      List<T> list = new ArrayList<T>();
      for (T t : i)
      {
         list.add(t);
      }
      return list;
   }
}
