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
package org.exoplatform.services.web.css.sac;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AbstractDocumentHandler implements DocumentHandler
{

   public void startDocument(InputSource source) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void endDocument(InputSource source) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void comment(String text) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void ignorableAtRule(String atRule) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void namespaceDeclaration(String prefix, String uri) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void startMedia(SACMediaList media) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void endMedia(SACMediaList media) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void startPage(String name, String pseudo_page) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void endPage(String name, String pseudo_page) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void startFontFace() throws CSSException
   {
      throw new UnsupportedOperationException("font face not implemented");
   }

   public void endFontFace() throws CSSException
   {
      throw new UnsupportedOperationException("font face not implemented");
   }

   public void startSelector(SelectorList selectors) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void endSelector(SelectorList selectors) throws CSSException
   {
      throw new UnsupportedOperationException();
   }

   public void property(String name, LexicalUnit value, boolean important) throws CSSException
   {
      throw new UnsupportedOperationException();
   }
}
