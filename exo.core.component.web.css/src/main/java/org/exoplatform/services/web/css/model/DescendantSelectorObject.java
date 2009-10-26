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

import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescendantSelectorObject extends SelectorObject implements DescendantSelector
{

   private SelectorObject ancestor;

   private SimpleSelectorObject simple;

   public DescendantSelectorObject(short type, SelectorObject ancestor, SimpleSelectorObject simple)
   {
      super(type);
      this.ancestor = ancestor;
      this.simple = simple;
   }

   public Selector getAncestorSelector()
   {
      return ancestor;
   }

   public SimpleSelector getSimpleSelector()
   {
      return simple;
   }

   protected boolean safeEquals(SelectorObject that)
   {
      if (that instanceof DescendantSelectorObject)
      {
         DescendantSelectorObject thatDescendant = (DescendantSelectorObject)that;
         return simple.equals(thatDescendant.simple) && ancestor.equals(thatDescendant.ancestor);
      }
      return false;
   }
}
