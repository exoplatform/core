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
package org.exoplatform.services.security.jaas;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 29 avr. 2004
 */
public class JAASGroup implements Group, Serializable
{

   private static final long serialVersionUID = -1224998629739318069L;

   public static final String ROLES = "Roles";

   private String name = null;

   @SuppressWarnings("unchecked")
   private HashSet members = null;

   @SuppressWarnings("unchecked")
   public JAASGroup(String n)
   {
      this.name = n;
      this.members = new HashSet();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public synchronized boolean addMember(Principal principal)
   {
      return members.add(principal);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized boolean removeMember(Principal principal)
   {
      return members.remove(principal);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public boolean isMember(Principal principal)
   {
      Enumeration en = members();
      while (en.hasMoreElements())
      {
         Principal principal1 = (Principal)en.nextElement();
         if (principal1.getName().equals(principal.getName()))
            return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Enumeration members()
   {
      class MembersEnumeration implements Enumeration
      {
         private Iterator itor;

         public MembersEnumeration(Iterator itor)
         {
            this.itor = itor;
         }

         public boolean hasMoreElements()
         {
            return this.itor.hasNext();
         }

         public Object nextElement()
         {
            return this.itor.next();
         }
      }
      return new MembersEnumeration(members.iterator());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      return getName().hashCode();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object object)
   {
      if (!(object instanceof Group))
         return false;
      return ((Group)object).getName().equals(getName());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return getName();
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

}
