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

import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.Selector;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class SelectorObject implements Selector
{

   public static SelectorObject create(Selector selector)
   {
      if (selector instanceof ConditionalSelector)
      {
         ConditionalSelector conditional = (ConditionalSelector)selector;
         return new ConditionalSelectorObject(conditional.getSelectorType(), (SimpleSelectorObject)create(conditional
            .getSimpleSelector()), ConditionObject.create(conditional.getCondition()));
      }
      else if (selector instanceof ElementSelector)
      {
         ElementSelector element = (ElementSelector)selector;
         return new ElementSelectorObject(element.getSelectorType(), element.getNamespaceURI(), element.getLocalName());
      }
      else if (selector instanceof DescendantSelector)
      {
         DescendantSelector descendant = (DescendantSelector)selector;
         SimpleSelectorObject simple = (SimpleSelectorObject)create(descendant.getSimpleSelector());
         SelectorObject ancestor = create(descendant.getAncestorSelector());
         return new DescendantSelectorObject(descendant.getSelectorType(), ancestor, simple);
      }
      else
      {
         throw new UnsupportedOperationException("Cannot create selector object for " + selector);
      }
   }

   private final short type;

   public SelectorObject(short type)
   {
      this.type = type;
   }

   public short getSelectorType()
   {
      return type;
   }

   public boolean equals(Object obj)
   {
      if (obj == null)
      {
         return false;
      }
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof SelectorObject)
      {
         SelectorObject that = (SelectorObject)obj;
         if (type == that.type)
         {
            return safeEquals(that);
         }
      }
      return false;
   }

   protected abstract boolean safeEquals(SelectorObject that);

}
