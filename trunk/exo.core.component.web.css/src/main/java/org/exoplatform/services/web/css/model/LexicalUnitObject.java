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

import org.w3c.css.sac.LexicalUnit;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class LexicalUnitObject implements LexicalUnit
{

   public static LexicalUnitObject create(LexicalUnit that)
   {
      LexicalUnitObject lu = _create(that);

      //
      LexicalUnitObject currentObject = lu;
      LexicalUnit currentThat = that;
      while (true)
      {
         LexicalUnit nextThat = currentThat.getNextLexicalUnit();
         if (nextThat != null)
         {
            LexicalUnitObject nextObject = _create(nextThat);
            currentObject.attachNext(nextObject);
            currentObject = nextObject;
            currentThat = nextThat;
         }
         else
         {
            break;
         }
      }

      //
      currentObject = lu;
      currentThat = that;
      while (true)
      {
         LexicalUnit previousThat = currentThat.getPreviousLexicalUnit();
         if (previousThat != null)
         {
            LexicalUnitObject previousObject = _create(previousThat);
            currentObject.attachPrevious(previousObject);
            currentObject = previousObject;
            currentThat = previousThat;
         }
         else
         {
            break;
         }
      }

      //
      if (that.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR)
      {
         LexicalUnit params = that.getParameters();
         lu.parameters = create(params);
      }

      //
      return lu;
   }

   private static LexicalUnitObject _create(LexicalUnit that)
   {
      short type = that.getLexicalUnitType();
      switch (type)
      {
         case SAC_IDENT :
         case SAC_STRING_VALUE :
         case SAC_URI :
            return new StringLexicalUnitObject(type, that.getStringValue());
         case SAC_PERCENTAGE :
         case SAC_EM :
         case SAC_PIXEL :
         case SAC_MILLIMETER :
         case SAC_CENTIMETER :
         case SAC_REAL :
         case SAC_POINT :
            return new FloatLexicalUnitObject(type, that.getFloatValue());
         case SAC_INTEGER :
            return new IntegerLexicalUnitObject(type, that.getIntegerValue());
         case SAC_INHERIT :
         case SAC_RGBCOLOR :
            return new SimpleLexicalUnitObject(type);
         case SAC_OPERATOR_COMMA :
            return new SimpleLexicalUnitObject(type);
         case SAC_FUNCTION :
            return new FunctionLexicalUnitObject(type, that.getFunctionName());
         default :
            throw new UnsupportedOperationException("Lexical unit type " + type + " is not handled");
      }
   }

   private final short type;

   private LexicalUnitObject next;

   private LexicalUnitObject previous;

   LexicalUnitObject parameters;

   protected LexicalUnitObject(short type)
   {
      this.type = type;
   }

   protected LexicalUnitObject getObject()
   {
      return this;
   }

   protected Class<LexicalUnitObject> getObjectClass()
   {
      return LexicalUnitObject.class;
   }

   public short getLexicalUnitType()
   {
      return type;
   }

   public LexicalUnit getNextLexicalUnit()
   {
      return getNext();
   }

   public LexicalUnit getPreviousLexicalUnit()
   {
      return getPrevious();
   }

   //

   public String getDimensionUnitText()
   {
      throw new UnsupportedOperationException();
   }

   public LexicalUnit getParameters()
   {
      return parameters;
   }

   public LexicalUnit getSubValues()
   {
      return parameters;
   }

   //

   public int getIntegerValue()
   {
      return 0;
   }

   public float getFloatValue()
   {
      return 0;
   }

   public String getFunctionName()
   {
      return null;
   }

   public String getStringValue()
   {
      return null;
   }

   //

   public LexicalUnitObject getNext()
   {
      return next != null ? next.getObject() : null;
   }

   public LexicalUnitObject getPrevious()
   {
      return previous != null ? previous.getObject() : null;
   }

   public boolean hasNext()
   {
      return next != null;
   }

   public boolean hasPrevious()
   {
      return previous != null;
   }

   public LexicalUnitObject get(int index)
   {
      if (index == 0)
      {
         return this.getObject();
      }
      else if (index > 0)
      {
         return next == null ? null : next.get(index - 1);
      }
      else
      {
         return previous == null ? null : previous.get(index + 1);
      }
   }

   public void detach()
   {
      if (hasNext())
      {
         detachNext();
      }
      if (hasPrevious())
      {
         detachPrevious();
      }
   }

   public void detachPrevious()
   {
      if (!hasPrevious())
      {
         throw new IllegalStateException();
      }
      previous.next = null;
      previous = null;
   }

   public void detachNext()
   {
      if (!hasNext())
      {
         throw new IllegalStateException();
      }
      next.previous = null;
      next = null;
   }

   public void attachPrevious(LexicalUnitObject previous)
   {
      if (hasPrevious())
      {
         throw new IllegalStateException();
      }
      if (previous.hasNext())
      {
         throw new IllegalStateException();
      }
      previous.next = this;
      this.previous = previous;
   }

   public void attachNext(LexicalUnitObject next)
   {
      if (hasNext())
      {
         throw new IllegalStateException();
      }
      if (next.hasPrevious())
      {
         throw new IllegalStateException();
      }
      next.previous = this;
      this.next = next;
   }

   public Iterable<LexicalUnitObject> next()
   {
      return iterable(false, true);
   }

   public Iterable<LexicalUnitObject> previous()
   {
      return iterable(false, false);
   }

   public Iterable<LexicalUnitObject> next(boolean includeSelf)
   {
      return iterable(includeSelf, true);
   }

   public Iterable<LexicalUnitObject> previous(boolean includeSelf)
   {
      return iterable(includeSelf, false);
   }

   private Iterable<LexicalUnitObject> iterable(final boolean includeSelf, final boolean left)
   {
      return new Iterable<LexicalUnitObject>()
      {
         public Iterator<LexicalUnitObject> iterator()
         {
            return new Iterator<LexicalUnitObject>()
            {
               LexicalUnitObject current = LexicalUnitObject.this;

               {
                  if (!includeSelf)
                  {
                     next();
                  }
               }

               private LexicalUnitObject getNext()
               {
                  return left ? current.next : current.previous;
               }

               public boolean hasNext()
               {
                  return current != null;
               }

               public LexicalUnitObject next()
               {
                  if (current == null)
                  {
                     throw new NoSuchElementException();
                  }
                  LexicalUnitObject tmp = current;
                  current = getNext();
                  return tmp.getObject();
               }

               public void remove()
               {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
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
      if (obj instanceof LexicalUnitObject)
      {
         LexicalUnitObject that = (LexicalUnitObject)obj;
         if (next == null)
         {
            if (that.next != null)
            {
               return false;
            }
         }
         else if (!next.safeEquals(that.next))
         {
            return false;
         }
         if (previous == null)
         {
            if (that.previous != null)
            {
               return false;
            }
         }
         else if (!previous.safeEquals(that.previous))
         {
            return false;
         }
         return safeEquals(that);
      }
      return false;
   }

   protected boolean safeEquals(LexicalUnitObject lexicalUnitObject)
   {
      return type == lexicalUnitObject.type;
   }
}
