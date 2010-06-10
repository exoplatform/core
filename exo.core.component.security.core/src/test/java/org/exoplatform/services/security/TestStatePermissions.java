/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.security;

import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: TestStatePermissions.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class TestStatePermissions extends BaseSecurityTest
{
   private ConversationState state;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      Collection<MembershipEntry> memberships = null;

      memberships = new ArrayList<MembershipEntry>();
      memberships.add(new MembershipEntry("/group1", "*"));
      memberships.add(new MembershipEntry("/group2", "member"));

      Identity identity = new Identity("user", memberships);
      state = new ConversationState(identity);
   }

   /**
    * Checks that modification is permitted
    */
   public void testStateSetCurrentWithPermission()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               ConversationState.setCurrent(state);
               ConversationState.setCurrent(null);
               return null;
            }
         }, PermissionConstants.MODIFY_CONVERSATION_STATE_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks that modification is denied if no permission given
    */
   public void testStateSetCurrentWithNoPermission()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               ConversationState.setCurrent(state);
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // it's ok
      }
   }

   /**
    * Checks that modification is permitted
    */
   public void testStateSetAttributeWithPermission()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               state.setAttribute("attribute", "value");
               return null;
            }
         }, PermissionConstants.MODIFY_CONVERSATION_STATE_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks that modification is denied if no permission given
    */
   public void testStateSetAttributeWithNoPermission()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               state.setAttribute("attribute", "value");
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // it's ok
      }
   }
}
