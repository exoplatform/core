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

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ConditionObject implements Condition
{

   public static ConditionObject create(Condition condition)
   {
      if (condition instanceof AttributeCondition)
      {
         AttributeCondition attribute = (AttributeCondition)condition;
         return new AttributeConditionObject(attribute.getConditionType(), attribute.getNamespaceURI(), attribute
            .getLocalName(), attribute.getSpecified(), attribute.getValue());
      }
      else if (condition instanceof CombinatorCondition)
      {
         CombinatorCondition combinator = (CombinatorCondition)condition;
         return new CombinatorConditionObject(combinator.getConditionType(), create(combinator.getFirstCondition()),
            create(combinator.getSecondCondition()));
      }
      else
      {
         throw new UnsupportedOperationException("Condition " + condition + " not yet supported");
      }
   }

   private final short type;

   public ConditionObject(short type)
   {
      this.type = type;
   }

   public short getConditionType()
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
      if (obj instanceof ConditionObject)
      {
         ConditionObject that = (ConditionObject)obj;
         if (type == that.type)
         {
            return safeEquals(that);
         }
      }
      return false;
   }

   protected abstract boolean safeEquals(ConditionObject that);
}
