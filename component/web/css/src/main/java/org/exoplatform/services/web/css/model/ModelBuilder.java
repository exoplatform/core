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

import org.exoplatform.services.web.css.sac.AbstractDocumentHandler;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

/**
 * A document handler implementation that creates an instance of a {@link org.exoplatform.services.web.css.model.StylesheetObject } object.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ModelBuilder extends AbstractDocumentHandler
{

   private StylesheetObject stylesheet;

   private RuleObject rule;

   public ModelBuilder()
   {
      stylesheet = new StylesheetObject();
   }

   /**
    * Returns the stylesheet object.
    *
    * @return the stylesheet
    */
   public StylesheetObject getStylesheet()
   {
      return stylesheet;
   }

   public void comment(String text) throws CSSException
   {
   }

   public void startDocument(InputSource source) throws CSSException
   {
   }

   public void endDocument(InputSource source) throws CSSException
   {
   }

   public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException
   {
      stylesheet.addImport(uri);
   }

   public void startSelector(SelectorList selectors) throws CSSException
   {
      rule = new RuleObject();
      for (int i = 0; i < selectors.getLength(); i++)
      {
         Selector selector = selectors.item(i);
         rule.addSelector(SelectorObject.create(selector));
      }
   }

   public void endSelector(SelectorList selectors) throws CSSException
   {
      stylesheet.addRule(rule);
      rule = null;
   }

   public void property(String name, LexicalUnit value, boolean important) throws CSSException
   {
      rule.addDeclaration(name, LexicalUnitObject.create(value));
   }
}
