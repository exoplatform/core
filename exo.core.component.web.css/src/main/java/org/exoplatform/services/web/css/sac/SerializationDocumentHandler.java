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

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SimpleSelector;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SerializationDocumentHandler extends AbstractDocumentHandler implements DocumentHandler
{

   private final Writer writer;

   public SerializationDocumentHandler(Writer writer)
   {
      this.writer = writer;
   }

   public void startDocument(InputSource source) throws CSSException
   {

   }

   public void endDocument(InputSource source) throws CSSException
   {

   }

   public void comment(String text) throws CSSException
   {
   }

   public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException
   {
      write("@import \"");
      write(uri);
      write("\";\n");
   }

   public void startSelector(SelectorList selectors) throws CSSException
   {
      for (int i = 0; i < selectors.getLength(); i++)
      {
         Selector selector = selectors.item(i);
         if (i > 0)
         {
            write(",");
         }
         write(selector);
      }
      write(" {\n");
   }

   public void endSelector(SelectorList selectors) throws CSSException
   {
      write("}\n");
   }

   public void property(String name, LexicalUnit value, boolean important) throws CSSException
   {
      write(name);
      write(": ");
      write(value, " ");
      write(";\n");
   }

   private void write(LexicalUnit lu, String separator)
   {
      String prefix = "";
      for (LexicalUnit current = lu; current != null; current = current.getNextLexicalUnit())
      {
         write(prefix);
         write(current);
         prefix = separator;
      }
   }

   private void write(LexicalUnit lu)
   {
      short type = lu.getLexicalUnitType();
      switch (type)
      {
         case LexicalUnit.SAC_URI :
            write("url(");
            write(lu.getStringValue());
            write(")");
            break;
         case LexicalUnit.SAC_STRING_VALUE :
            write('"');
            write(lu.getStringValue());
            write('"');
            break;
         case LexicalUnit.SAC_IDENT :
            write(lu.getStringValue());
            break;
         case LexicalUnit.SAC_REAL :
            write(lu.getFloatValue());
            break;
         case LexicalUnit.SAC_PIXEL :
            write(lu.getFloatValue());
            write("px");
            break;
         case LexicalUnit.SAC_MILLIMETER :
            write(lu.getFloatValue());
            write("mm");
            break;
         case LexicalUnit.SAC_CENTIMETER :
            write(lu.getFloatValue());
            write("cm");
            break;
         case LexicalUnit.SAC_PERCENTAGE :
            write(lu.getFloatValue());
            write("%");
            break;
         case LexicalUnit.SAC_POINT :
            write(lu.getFloatValue());
            write("pt");
            break;
         case LexicalUnit.SAC_EM :
            write(lu.getFloatValue());
            write("em");
            break;
         case LexicalUnit.SAC_INTEGER :
            write(lu.getIntegerValue());
            break;
         case LexicalUnit.SAC_FUNCTION :
            write(lu.getFunctionName());
            write("(");
            write(lu.getParameters(), "");
            write(")");
            break;
         case LexicalUnit.SAC_RGBCOLOR :
            write("rgb(");
            write(lu.getParameters(), "");
            write(")");
            break;
         case LexicalUnit.SAC_OPERATOR_COMMA :
            write(",");
            break;
         case LexicalUnit.SAC_INHERIT :
            write("inherit");
            break;
         default :
            throw new UnsupportedOperationException("Lexical unit type " + type + " is not handled");
      }
   }

   private void write(Condition condition)
   {
      switch (condition.getConditionType())
      {
         case Condition.SAC_PSEUDO_CLASS_CONDITION : {
            AttributeCondition pseudoClassCond = (AttributeCondition)condition;
            write(":");
            write(pseudoClassCond.getValue());
            break;
         }
         case Condition.SAC_CLASS_CONDITION : {
            AttributeCondition classCond = (AttributeCondition)condition;
            write(".");
            write(classCond.getValue());
            break;
         }
         case Condition.SAC_ATTRIBUTE_CONDITION : {
            AttributeCondition attributeCond = (AttributeCondition)condition;
            write("[");
            write(attributeCond.getLocalName());
            write("=");
            write(attributeCond.getValue());
            write("]");
            break;
         }
         case Condition.SAC_AND_CONDITION : {
            CombinatorCondition andCond = (CombinatorCondition)condition;
            Condition first = andCond.getFirstCondition();
            Condition second = andCond.getSecondCondition();
            write(first);
            write(second);
            break;
         }
         case Condition.SAC_ID_CONDITION : {
            AttributeCondition idCond = (AttributeCondition)condition;
            write("#");
            write(idCond.getValue());
            break;
         }
         default :
            throw new UnsupportedOperationException("condition type = " + condition.getConditionType() + " with class "
               + condition.getClass().getName());
      }
   }

   private void write(Selector sel)
   {

      switch (sel.getSelectorType())
      {
         case Selector.SAC_CONDITIONAL_SELECTOR : {
            ConditionalSelector conditionalSel = (ConditionalSelector)sel;
            Condition condition = conditionalSel.getCondition();
            SimpleSelector simpleSel = conditionalSel.getSimpleSelector();
            write(simpleSel);
            write(condition);
            break;
         }
         case Selector.SAC_CHILD_SELECTOR : {
            DescendantSelector childSel = (DescendantSelector)sel;
            Selector ancestorSel = childSel.getAncestorSelector();
            SimpleSelector simpleSel = childSel.getSimpleSelector();

            //
            if (simpleSel.getSelectorType() == Selector.SAC_PSEUDO_ELEMENT_SELECTOR)
            {
               ElementSelector elementSel = (ElementSelector)simpleSel;
               write(ancestorSel);
               write(":");
               write(elementSel.getLocalName());
            }
            else
            {
               write(ancestorSel);
               write(">");
               write(simpleSel);
            }
            break;
         }
         case Selector.SAC_DESCENDANT_SELECTOR : {
            DescendantSelector descendantSel = (DescendantSelector)sel;
            SimpleSelector simpleSel = descendantSel.getSimpleSelector();
            write(descendantSel.getAncestorSelector());

            //
            if (simpleSel.getSelectorType() == Selector.SAC_PSEUDO_ELEMENT_SELECTOR)
            {
               ElementSelector pseudoElementSel = (ElementSelector)simpleSel;
               write(":");
               write(pseudoElementSel.getLocalName());
            }
            else
            {
               write(" ");
               write(simpleSel);
            }
            break;
         }
         case Selector.SAC_ELEMENT_NODE_SELECTOR : {
            ElementSelector elementSel = (ElementSelector)sel;

            //
            if (elementSel.getLocalName() == null)
            {
               // Universal element selector * that we can omit
            }
            else
            {
               write(elementSel.getLocalName());
            }
            break;
         }
         default :
            throw new UnsupportedOperationException("Selector type = " + sel.getSelectorType() + " with class "
               + sel.getClass().getName());
      }
   }

   private void write(float f)
   {
      if (Math.floor(f) == f)
      {
         write((int)f);
      }
      else
      {
         write(Float.toString(f));
      }
   }

   private void write(char c)
   {
      write(Character.toString(c));
   }

   private void write(int i)
   {
      write(Integer.toString(i));
   }

   private void write(String s)
   {
      try
      {
         writer.write(s);
         writer.flush();
      }
      catch (IOException e)
      {
         throw new CSSException(e);
      }
   }
}