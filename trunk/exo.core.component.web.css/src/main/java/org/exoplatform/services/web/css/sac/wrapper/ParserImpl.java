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
package org.exoplatform.services.web.css.sac.wrapper;

import org.exoplatform.services.web.css.comment.CommentListener;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SelectorList;

import java.io.IOException;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserImpl implements Parser
{

   private Parser delegate;

   private ParserDocumentHandler handler;

   public ParserImpl()
   {
      delegate = newParser();
   }

   private Parser newParser()
   {
      //    return new org.w3c.flute.parser.Parser();
      return new org.apache.batik.css.parser.Parser();
      //    return new com.steadystate.css.parser.SACParserCSS21();
   }

   public void setLocale(Locale locale) throws CSSException
   {
      delegate.setLocale(locale);
   }

   public void setDocumentHandler(DocumentHandler documentHandler)
   {
      handler = new ParserDocumentHandler(documentHandler);
      delegate.setDocumentHandler(handler);
   }

   public void setSelectorFactory(SelectorFactory selectorFactory)
   {
      delegate.setSelectorFactory(selectorFactory);
   }

   public void setConditionFactory(ConditionFactory conditionFactory)
   {
      delegate.setConditionFactory(conditionFactory);
   }

   public void setErrorHandler(ErrorHandler errorHandler)
   {
      delegate.setErrorHandler(errorHandler);
   }

   public void parseStyleSheet(InputSource inputSource) throws CSSException, IOException
   {
      delegate.parseStyleSheet(wrap(inputSource));
   }

   public void parseStyleSheet(String uri) throws CSSException, IOException
   {
      delegate.parseStyleSheet(wrap(new InputSource(uri)));
   }

   public void parseStyleDeclaration(InputSource inputSource) throws CSSException, IOException
   {
      delegate.parseStyleDeclaration(wrap(inputSource));
   }

   public void parseRule(InputSource inputSource) throws CSSException, IOException
   {
      delegate.parseRule(wrap(inputSource));
   }

   public String getParserVersion()
   {
      return delegate.getParserVersion();
   }

   public SelectorList parseSelectors(InputSource inputSource) throws CSSException, IOException
   {
      return delegate.parseSelectors(wrap(inputSource));
   }

   public LexicalUnit parsePropertyValue(InputSource inputSource) throws CSSException, IOException
   {
      return delegate.parsePropertyValue(wrap(inputSource));
   }

   public boolean parsePriority(InputSource inputSource) throws CSSException, IOException
   {
      return delegate.parsePriority(wrap(inputSource));
   }

   private InputSource wrap(InputSource wrapped) throws IOException
   {
      return new InputSourceWrapper(wrapped, new CommentListener()
      {
         public void comment(String text)
         {
            handler.next.comment(text);
         }
      });
   }
}
