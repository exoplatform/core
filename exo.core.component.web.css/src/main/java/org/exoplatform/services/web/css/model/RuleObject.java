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

import org.w3c.css.sac.DocumentHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RuleObject
{

   private SelectorListImpl selectors;

   private List<DeclarationObject> declarations;

   public RuleObject()
   {
      this.selectors = new SelectorListImpl();
      this.declarations = new ArrayList<DeclarationObject>();
   }

   public void addSelector(SelectorObject selector)
   {
      selectors.add(selector);
   }

   public List<SelectorObject> getSelectors()
   {
      return selectors;
   }

   public DeclarationObject addDeclaration(String name, LexicalUnitObject value)
   {
      DeclarationObject decl = new DeclarationObject(name, value);
      declarations.add(decl);
      return decl;
   }

   public List<DeclarationObject> getDeclarations()
   {
      return declarations;
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
      if (obj instanceof RuleObject)
      {
         RuleObject that = (RuleObject)obj;
         return selectors.equals(that.selectors) && declarations.equals(that.declarations);
      }
      return false;
   }

   protected void internalVisit(DocumentHandler handler) throws IllegalArgumentException
   {
      handler.startSelector(selectors);
      for (DeclarationObject declaration : declarations)
      {
         declaration.internalVisit(handler);
      }
      handler.endSelector(selectors);
   }
}
