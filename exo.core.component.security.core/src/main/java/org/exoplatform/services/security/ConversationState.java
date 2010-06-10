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
package org.exoplatform.services.security;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by The eXo Platform SAS .
 *
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ConversationState
{

   /**
    * "subject".
    */
   public static final String SUBJECT = "subject";

   /**
    * ThreadLocal keeper for ConversationState.
    */
   private static ThreadLocal<ConversationState> current = new ThreadLocal<ConversationState>();

   /**
    * See {@link Identity}.
    */
   private Identity identity;

   /**
    * Additions attributes of ConversationState.
    */
   private HashMap<String, Object> attributes;

   public ConversationState(Identity identity)
   {
      this.identity = identity;
      this.attributes = new HashMap<String, Object>();
   }

   /**
    * @return current ConversationState or null if it was not preset
    */
   public static ConversationState getCurrent()
   {
      return current.get();
   }

   /**
    * Preset current ConversationState.
    *
    * @param state ConversationState
    */
   public static void setCurrent(ConversationState state)
   {
      checkPermissions();
      current.set(state);
   }

   /**
    * @return Identity
    */
   public Identity getIdentity()
   {
      return identity;
   }

   /**
    * sets attribute.
    *
    * @param key
    * @param value
    */
   public void setAttribute(String name, Object value)
   {
      checkPermissions();
      this.attributes.put(name, value);
   }

   /**
    * @param key
    * @return attribute
    */
   public Object getAttribute(String name)
   {
      return this.attributes.get(name);
   }

   /**
    * @return all attribute names
    */
   public Set<String> getAttributeNames()
   {
      return attributes.keySet();
   }

   /**
    * removes attribute.
    *
    * @param key
    */
   public void removeAttribute(String name)
   {
      this.attributes.remove(name);
   }

   /**
    *  Checks if modification allowed
    */
   private static void checkPermissions()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(PermissionConstants.MODIFY_CONVERSATION_STATE_PERMISSION);
      }
   }
}
