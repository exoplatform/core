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
package org.exoplatform.services.web.css;

import junit.framework.TestCase;

import org.exoplatform.services.web.css.model.StylesheetObject;
import org.exoplatform.services.web.css.sac.ParserFactory;
import org.exoplatform.services.web.css.sac.SerializationDocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CSSSerializerTestCase extends TestCase
{

   public CSSSerializerTestCase()
   {
   }

   public CSSSerializerTestCase(String s)
   {
      super(s);
   }

   private static final String[] sheets =
      {

         "@import \"another.css\";",

         // Selector test cases

         "foo {}", "foo bar {}", ":first-letter {}", "foo:first-letter {}", ":first-child {}", "foo:first-child {}",
         "foo > bar {}", "foo:hover {}", "foo.bar {}", ".bar {}", ".bar .foo {}", "foo:hover.bar {}",
         "foo.bar:hover {}", "foo:focus:hover {}",
         "foo,bar {}",
         "#bar {}",
         "foo#bar {}",
         "foo#bar.juu:first-child {}",
         "foo[bar=\"abc\"]",

         // Declarations test cases

         "foo { padding-left: 2 }", "foo { padding-left: 2px }", "foo { padding-left: 2.2em }", "foo { float: left }",
         "foo { background: url('bar') repeat-x center top }", "foo { color: #202020 }", "foo { bar: juu(1, 2); }",
         "foo { padding: 3px; }", "foo { padding: 3px 2px; }", "foo { padding: 3px 2px 1px; }",
         "foo { padding: 3px 0px 1px 2px; }", "foo { opacity: 0.92; }", "foo { display: inherit; }",
         "foo { font-family: \"Tahoma\",Geneva,Arial,Verdana,sans-serif; }", "foo { font-size: 10pt; }",
         "foo { margin-left: 10mm; }", "foo { margin-left: 10cm; }", "foo { width: 95%; }",

         // Import

         "@import \"another.css\";",};

   public void testSerialization() throws IOException
   {
      for (String leftSheet : sheets)
      {
         //      System.out.println("leftSheet = " + leftSheet);
         Parser parser = ParserFactory.createParser();
         StringWriter out = new StringWriter();
         SerializationDocumentHandler serializer = new SerializationDocumentHandler(out);
         parser.setDocumentHandler(serializer);
         parser.parseStyleSheet(new InputSource(new StringReader(leftSheet)));
         out.close();
         String rightSheet = out.toString();
         StylesheetObject left = StylesheetObject.createStylesheet(leftSheet);
         StylesheetObject right = StylesheetObject.createStylesheet(rightSheet);
         if (!left.equals(right))
         {
            fail("Stylesheet " + rightSheet + " should be the same than " + leftSheet);
         }
      }
   }
}
