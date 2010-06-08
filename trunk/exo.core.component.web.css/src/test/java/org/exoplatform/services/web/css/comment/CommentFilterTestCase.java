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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CommentFilterTestCase extends TestCase
{

   public CommentFilterTestCase()
   {
   }

   public CommentFilterTestCase(String s)
   {
      super(s);
   }

   public void testA() throws IOException
   {
      CommentFilterObject object = createFilter("foo");
      object.readChars("foo");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertNoComment();
   }

   public void testB() throws IOException
   {
      CommentFilterObject object = createFilter("/* */");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertComment(" ");
   }

   public void testC1() throws IOException
   {
      CommentFilterObject object = createFilter("/* */foo");
      object.assertNoComment();
      object.readChars(1, 0);
      object.assertComment(" ");
      object.readChars("foo");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertNoComment();
   }

   public void testC2() throws IOException
   {
      CommentFilterObject object = createFilter("/* */foo");
      object.assertNoComment();
      object.readChars(6, 0);
      object.assertComment(" ");
      object.readChars(6, "foo");
      object.assertNoComment();
      object.readChars(6, -1);
      object.assertNoComment();
   }

   public void testD1() throws IOException
   {
      CommentFilterObject object = createFilter("foo/* */bar");
      object.readChars(3, "foo");
      object.assertNoComment();
      object.readChars(1, 0);
      object.assertComment(" ");
      object.readChars("bar");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertNoComment();
   }

   public void testD2() throws IOException
   {
      CommentFilterObject object = createFilter("foo/* */bar");
      object.readChars(4, "foo");
      object.assertNoComment();
      object.readChars(1, 0);
      object.assertComment(" ");
      object.readChars("bar");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertNoComment();
   }

   public void testE() throws IOException
   {
      CommentFilterObject object = createFilter("/* *//* */");
      object.readChars(1, 0);
      object.assertComment(" ");
      object.readChars(1, -1);
      object.assertComment(" ");
   }

   public void testF() throws IOException
   {
      CommentFilterObject object = createFilter("/***/");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertComment("*");
   }

   public void testG() throws IOException
   {
      CommentFilterObject object = createFilter("/****/");
      object.assertNoComment();
      object.readChars(1, -1);
      object.assertComment("**");
   }

   public void testH() throws IOException
   {
      CommentFilterObject object = createFilter("/*");
      try
      {
         object.readChars(1, 0);
         fail();
      }
      catch (IllegalStateException expected)
      {
      }
   }

   public void testI() throws IOException
   {
      CommentFilterObject object = createFilter("/**");
      try
      {
         object.readChars(1, 0);
         fail();
      }
      catch (IllegalStateException expected)
      {
      }
   }

   public void testJ() throws IOException
   {
      CommentFilterObject object = createFilter("a/b");
      object.assertNoComment();
      object.readChars("a/b");
      object.assertNoComment();
   }

   public CommentFilterObject createFilter(String s)
   {
      return new CommentFilterObject(s);
   }
}
