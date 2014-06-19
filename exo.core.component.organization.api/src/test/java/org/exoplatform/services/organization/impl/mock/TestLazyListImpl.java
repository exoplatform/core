/*
 * Copyright (C) 2014 eXo Platform SAS.
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
package org.exoplatform.services.organization.impl.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestLazyListImpl
{
   private LazyListImpl list;

   @Before
   public void init()
   {
      list = new LazyListImpl();
      list.add(new UserImpl("foo1"));
      list.add(new UserImpl("foo2"));
      list.add(new UserImpl("foo3"));
      list.add(new UserImpl("foo4"));
      list.add(new UserImpl("foo5"));
   }

   @Test
   public void testLoad() throws Exception
   {
      try
      {
         list.load(-1, 5);
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
         // ok
      }
      try
      {
         list.load(0, -1);
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
         // ok
      }
      try
      {
         list.load(0, 6);
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
         // ok
      }
      try
      {
         list.load(1, 5);
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
         // ok
      }
      // Length == size
      User[] users = list.load(0, 5);
      assertNotNull(users);
      assertEquals(5, users.length);
      int startIndex = 1;
      for (User user : users)
      {
         assertNotNull(user);
         assertEquals("foo" + startIndex++, user.getUserName());
      }
      // Length < size
      users = list.load(0, 3);
      assertNotNull(users);
      assertEquals(3, users.length);
      startIndex = 1;
      for (User user : users)
      {
         assertNotNull(user);
         assertEquals("foo" + startIndex++, user.getUserName());
      }
      // Length < size and index > 0
      users = list.load(2, 3);
      assertNotNull(users);
      assertEquals(3, users.length);
      startIndex = 3;
      for (User user : users)
      {
         assertNotNull(user);
         assertEquals("foo" + startIndex++, user.getUserName());
      }
   }

   @Test
   public void testGetSize() throws Exception
   {
      assertEquals(5, list.getSize());
   }
}
