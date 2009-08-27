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

import org.exoplatform.services.web.css.comment.CommentListener;
import org.exoplatform.services.web.css.model.LexicalUnitObject;
import org.exoplatform.services.web.css.model.StringLexicalUnitObject;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OrientationFilter extends FilterDocumentHandler implements CommentListener
{

   private static final Pattern URL_PARSER = Pattern.compile("^(.*)\\.(\\p{Alpha}{3})$");

   private final boolean rt;

   private String previousName;

   private LexicalUnit previousValue;

   private boolean active;

   public OrientationFilter(DocumentHandler next, boolean rt)
   {
      super(next);

      //
      this.rt = rt;
      this.active = true;
   }

   public boolean isRT()
   {
      return rt;
   }

   public boolean isLT()
   {
      return !rt;
   }

   public void comment(String text) throws CSSException
   {
      text = text.trim().toLowerCase();
      if ("orientation=ignore".equals(text))
      {
         boolean previousActive = active;
         active = false;
         flush();
         active = previousActive;
      }
      else if ("orientation=enable".equals(text))
      {
         flush();
         active = true;
      }
      else if ("orientation=disable".equals(text))
      {
         flush();
         active = false;
      }
      else if (rt && "orientation=lt".equals(text))
      {
         previousName = null;
         previousValue = null;
      }
   }

   private void flush()
   {
      if (previousName != null)
      {

         //
         if (active)
         {
            if (rt)
            {
               if ("margin-left".equals(previousName))
               {
                  previousName = "margin-right";
               }
               else if ("margin-right".equals(previousName))
               {
                  previousName = "margin-left";
               }
               else if ("padding-left".equals(previousName))
               {
                  previousName = "padding-right";
               }
               else if ("padding-right".equals(previousName))
               {
                  previousName = "padding-left";
               }
               else if ("float".equals(previousName) && previousValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT)
               {
                  String s = previousValue.getStringValue();
                  if ("left".equals(s))
                  {
                     previousValue = new StringLexicalUnitObject(LexicalUnit.SAC_IDENT, "right");
                  }
                  else if ("right".equals(s))
                  {
                     previousValue = new StringLexicalUnitObject(LexicalUnit.SAC_IDENT, "left");
                  }
               }
               else if ("padding".equals(previousName))
               {
                  int count = 0;
                  for (LexicalUnit current = previousValue; current != null; current = current.getNextLexicalUnit())
                  {
                     count++;
                  }
                  if (count == 4)
                  {
                     LexicalUnitObject top = LexicalUnitObject.create(previousValue);
                     LexicalUnitObject right = top.getNext();
                     LexicalUnitObject bottom = right.getNext();
                     LexicalUnitObject left = bottom.getNext();

                     //
                     right.detach();
                     left.detach();
                     top.attachNext(left);
                     bottom.attachPrevious(left);
                     bottom.attachNext(right);

                     //
                     previousValue = top;
                  }
               }
               else if ("background".equals(previousName))
               {
                  LexicalUnitObject lu = LexicalUnitObject.create(previousValue);
                  StringLexicalUnitObject lor = null;
                  StringLexicalUnitObject url = null;
                  for (LexicalUnitObject current : lu.next(true))
                  {
                     if (current.getLexicalUnitType() == LexicalUnit.SAC_URI)
                     {
                        url = (StringLexicalUnitObject)current;
                     }
                     else if (current.getLexicalUnitType() == LexicalUnit.SAC_IDENT)
                     {
                        String s = current.getStringValue();
                        if ("left".equals(s) || "right".equals(s))
                        {
                           lor = (StringLexicalUnitObject)current;
                        }
                     }
                  }
                  if (lor != null && url != null)
                  {
                     Matcher matcher = URL_PARSER.matcher(url.getStringValue());
                     if (matcher.find())
                     {
                        lor.setStringValue("left".equals(lor.getStringValue()) ? "right" : "left");
                        url.setStringValue(matcher.group(1) + "-rt." + matcher.group(2));
                        previousValue = lu;
                     }
                  }
               }
            }
         }

         //
         super.property(previousName, previousValue, false);

         //
         previousName = null;
         previousValue = null;
      }
   }

   public void endDocument(InputSource source) throws CSSException
   {
      flush();
      super.endDocument(source);
   }

   public void startSelector(SelectorList selectors) throws CSSException
   {
      flush();
      super.startSelector(selectors);
   }

   public void endSelector(SelectorList selectors) throws CSSException
   {
      flush();
      super.endSelector(selectors);
   }

   public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException
   {
      flush();
      super.importStyle(uri, media, defaultNamespaceURI);
   }

   public void property(String name, LexicalUnit value, boolean important) throws CSSException
   {
      flush();
      previousName = name;
      previousValue = value;
   }
}
