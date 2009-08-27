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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FunctionLexicalUnitObject extends LexicalUnitObject
{

   private String functionName;

   public FunctionLexicalUnitObject(short type, String functionName)
   {
      super(type);
      this.functionName = functionName;
   }

   public String getFunctionName()
   {
      return functionName;
   }

   protected boolean safeEquals(LexicalUnitObject that)
   {
      if (super.safeEquals(that))
      {
         if (that instanceof FunctionLexicalUnitObject)
         {
            String thatFunctionName = ((FunctionLexicalUnitObject)that).functionName;
            return functionName != null ? functionName.equals(thatFunctionName) : thatFunctionName == null;
         }
      }
      return false;
   }
}
