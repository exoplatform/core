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
import org.exoplatform.services.web.css.sac.OrientationFilter;
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
public class OrientationFilterTestCase extends TestCase
{

   public OrientationFilterTestCase()
   {
   }

   public OrientationFilterTestCase(String s)
   {
      super(s);
   }

   private static final String[] sheets =
      {"foo { width: 70%; /* orientation=lt */ width: 30%; }", "foo { width: 30%; }",

      "foo {}", "foo {}",

      "foo { padding-left: 3px; }", "foo { padding-right: 3px; }",

      "/* orientation=disable */ foo { padding-left: 3px; }", "foo { padding-left: 3px; }",

      "/* orientation=disable */ foo { padding-left: 3px; } /* orientation=enable */ foo { padding-left: 3px; }",
         "foo { padding-left: 3px; } foo { padding-right: 3px; }",

         "foo { padding-left: 3px; /* orientation=ignore */ }", "foo { padding-left: 3px; }",

         "foo { padding-right: 3px; }", "foo { padding-left: 3px; }",

         "foo { margin-left: 3px; }", "foo { margin-right: 3px; }",

         "foo { margin-right: 3px; }", "foo { margin-left: 3px; }",

         "foo { float: left; }", "foo { float: right; }",

         "foo { float: right; }", "foo { float: left; }",

         "foo { padding: 3px; }", "foo { padding: 3px; }",

         "foo { padding: 3px 2px; }", "foo { padding: 3px 2px; }",

         "foo { padding: 3px 2px 1px; }", "foo { padding: 3px 2px 1px; }",

         "foo { padding: 3px 2px 1px 0px; }", "foo { padding: 3px 0px 1px 2px; }",

         "foo { background: url(foo.gif) left; }", "foo { background: url(foo-rt.gif) right; }",

      };

   public void testSerialization() throws IOException
   {
      for (int i = 0; i < sheets.length; i += 2)
      {
         String ltSheet = sheets[i];
         //      System.out.println("ltSheet = " + ltSheet);
         String expectedRTSheet = sheets[i + 1];
         Parser parser = ParserFactory.createParser();
         StringWriter out = new StringWriter();
         SerializationDocumentHandler serializer = new SerializationDocumentHandler(out);
         OrientationFilter filter = new OrientationFilter(serializer, true);
         parser.setDocumentHandler(filter);
         parser.parseStyleSheet(new InputSource(new StringReader(ltSheet)));
         out.close();
         String actualRTSheet = out.toString();
         StylesheetObject expected = StylesheetObject.createStylesheet(expectedRTSheet);
         StylesheetObject actual = StylesheetObject.createStylesheet(actualRTSheet);
         if (!expected.equals(actual))
         {
            fail("Stylesheet " + actualRTSheet + " should be the same than " + expectedRTSheet);
         }
      }
   }
}