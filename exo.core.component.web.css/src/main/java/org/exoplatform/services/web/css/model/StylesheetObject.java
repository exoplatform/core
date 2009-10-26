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

import org.exoplatform.services.web.css.sac.ParserFactory;
import org.exoplatform.services.web.css.sac.SerializationDocumentHandler;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

/**
 * A stylesheet.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class StylesheetObject
{

   /**
    * Parse the css to create a stylesheet.
    *
    * @param css the css
    * @return the stylesheet
    * @throws IllegalArgumentException if the css argument is null
    * @throws CSSException a css exception
    */
   public static StylesheetObject createStylesheet(String css) throws IllegalArgumentException, CSSException
   {
      if (css == null)
      {
         throw new IllegalArgumentException("No css provided");
      }
      try
      {
         return createStylesheet(new InputSource(new StringReader(css)));
      }
      catch (IOException e)
      {
         throw new UndeclaredThrowableException(e);
      }
   }

   /**
    * Parse the css to create a stylesheet.
    *
    * @param css the css
    * @return the stylesheet
    * @throws IllegalArgumentException if the css argument is null
    * @throws IOException related to the reader access
    * @throws CSSException a css exception
    */
   public static StylesheetObject createStylesheet(Reader css) throws IllegalArgumentException, IOException,
      CSSException
   {
      if (css == null)
      {
         throw new IllegalArgumentException("No css provided");
      }
      return createStylesheet(new InputSource(css));
   }

   private static StylesheetObject createStylesheet(InputSource source) throws IOException, CSSException
   {
      Parser parser = ParserFactory.createParser();
      ModelBuilder builder = new ModelBuilder();
      parser.setDocumentHandler(builder);
      parser.parseStyleSheet(source);
      return builder.getStylesheet();
   }

   private static final SACMediaList SAC_MEDIA_LIST = new SACMediaList()
   {
      public int getLength()
      {
         return 0;
      }

      public String item(int i)
      {
         return null;
      }
   };

   private final List<RuleObject> rules;

   private final List<String> imports;

   public StylesheetObject()
   {
      rules = new ArrayList<RuleObject>();
      imports = new ArrayList<String>();
   }

   /**
    * Add a rule to the style sheet.
    *
    * @param rule the rule to add
    * @throws IllegalArgumentException if the rule is null
    */
   public void addRule(RuleObject rule) throws IllegalArgumentException
   {
      if (rule == null)
      {
         throw new IllegalArgumentException("No rule provided");
      }
      rules.add(rule);
   }

   /**
    * Add an imported URI.
    *
    * @param uri the uri import to add
    * @throws IllegalArgumentException if the uri is null
    */
   public void addImport(String uri) throws IllegalArgumentException
   {
      if (uri == null)
      {
         throw new IllegalArgumentException("No uri provided");
      }
      imports.add(uri);
   }

   /**
    * Returns the list of the rules.
    *
    * @return the rules
    */
   public List<RuleObject> getRules()
   {
      return rules;
   }

   /**
    * Returns the list of imported URIs.
    *
    * @return the imported uris
    */
   public List<String> getImports()
   {
      return imports;
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
      if (obj instanceof StylesheetObject)
      {
         StylesheetObject that = (StylesheetObject)obj;
         return rules.equals(that.rules);
      }
      return false;
   }

   /**
    * Serialize the css to the provided writer.
    *
    * @param writer the writer
    * @throws IOException              any exception related to the writer
    * @throws IllegalArgumentException if the writer is null
    */
   public final void writeTo(Writer writer) throws IOException, IllegalArgumentException
   {
      if (writer == null)
      {
         throw new IllegalArgumentException("No writer provided");
      }
      internalVisit(new SerializationDocumentHandler(writer));
   }

   /**
    * Visitor pattern implementation based on the {@link org.w3c.css.sac.DocumentHandler} for callbacks.
    *
    * @param handler the handler
    * @throws IllegalArgumentException if the handler is null
    */
   public final void visit(DocumentHandler handler) throws IllegalArgumentException
   {
      if (handler == null)
      {
         throw new IllegalArgumentException("No handler provided");
      }
      internalVisit(handler);
   }

   protected void internalVisit(DocumentHandler handler) throws IllegalArgumentException
   {
      InputSource source = new InputSource();
      handler.startDocument(source);
      for (String import_ : imports)
      {
         handler.importStyle(import_, SAC_MEDIA_LIST, null);
      }
      for (RuleObject rule : rules)
      {
         rule.internalVisit(handler);
      }
      handler.endDocument(source);
   }
}
