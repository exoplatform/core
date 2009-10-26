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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AttributeConditionObject extends ConditionObject implements AttributeCondition
{

   private String namespaceURI;

   private String localName;

   private boolean specified;

   private String value;

   public AttributeConditionObject(short type, String namespaceURI, String localName, boolean specified, String value)
   {
      super(type);
      this.namespaceURI = namespaceURI;
      this.localName = localName;
      this.specified = specified;
      this.value = value;
   }

   public AttributeConditionObject(short type, String localName, String value)
   {
      super(type);
      this.localName = localName;
      this.value = value;
   }

   public String getNamespaceURI()
   {
      return namespaceURI;
   }

   public String getLocalName()
   {
      return localName;
   }

   public boolean getSpecified()
   {
      return specified;
   }

   public String getValue()
   {
      return value;
   }

   protected boolean safeEquals(ConditionObject that)
   {
      if (that instanceof AttributeConditionObject)
      {
         AttributeConditionObject thatAttribute = (AttributeConditionObject)that;
         if (localName == null)
         {
            return thatAttribute.localName == null;
         }
         return localName.equals(thatAttribute.localName) && value.equals(thatAttribute.value);
      }
      return false;
   }
}
