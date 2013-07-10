/*
 * Copyright (C) 2013 eXo Platform SAS.
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

import java.util.Collection;
import java.util.HashSet;

/**
 * A sub class of {@link HashSet} that will clean up its content before
 * adding a membership with <code>*</code> as membership type to prevent
 * bug of type COR-288.
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class MembershipHashSet extends HashSet<MembershipEntry>
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 8990049840572010725L;

   /**
    * The default constructor
    */
   public MembershipHashSet()
   {
   }

   /**
    * The default constructor
    * @param c a collection of memberships to add
    */
   public MembershipHashSet(Collection<? extends MembershipEntry> c)
   {
      super(c);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean add(MembershipEntry me)
   {
      if (MembershipEntry.ANY_TYPE.equals(me.getMembershipType()))
      {
         // We first remove the existing membership types
         // A loop is needed since we could have several membership types
         // for the same group
         while (contains(me))
            remove(me);
      }
      return super.add(me);
   }
}
