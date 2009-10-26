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
package org.exoplatform.services.database.impl;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Created by The eXo Platform SAS . Author : Travis Gregg Date: Oct 25, 2004
 * Time: 1:12:22 PM <br>
 * Custom Hibernate type to be used instead of the built in type 'TEXT'. This
 * custom type was created to handle Oracle Clobs differently. Using the 'TEXT'
 * type, Oracle Clobs can not participate in transactions, nor can they be
 * pulled into batches. For some reason, handling them this way, using oracle
 * temporaries gets around this. <BR>
 * <BR>
 * Thanks to reflection (and code on the Hibernate forum) this class can be
 * compiled without having the oracle drivers (classes12.zip) in your classpath. <br>
 * This is based on some code found in the Hibernate user forum:
 * http://www.hibernate.org/56.html <br>
 * http://www.hibernate.org/73.html
 * 
 * @author tgregg
 */
public class TextClobType implements UserType
{

   public TextClobType()
   {
   }

   public int[] sqlTypes()
   {
      return new int[]{Types.CLOB};
   }

   public Class returnedClass()
   {
      return String.class;
   }

   public boolean equals(Object x, Object y) throws HibernateException
   {
      return (x == y) || (x != null && x.equals(y));
   }

   // TODO cleanup, fix of COR-129. [06.03.2009] 
   //  private void nullSafeSetOld(PreparedStatement stmt, Object value, int index) throws HibernateException,
   //                                                                          SQLException {
   //
   //    // if this is a PreparedStatement wrapper, get the underlying
   //    // PreparedStatement
   //    PreparedStatement realStatement = getRealStatement(stmt);
   //
   //    DatabaseMetaData dbMetaData = realStatement.getConnection().getMetaData();
   //
   //    if (value == null) {
   //      stmt.setNull(index, sqlTypes()[0]);
   //    } else if (ORACLE_DRIVER_NAME.equals(dbMetaData.getDriverName())) {
   //      if ((dbMetaData.getDriverMajorVersion() >= ORACLE_DRIVER_MAJOR_VERSION)
   //          && (dbMetaData.getDriverMinorVersion() >= ORACLE_DRIVER_MINOR_VERSION)) {
   //        try {
   //          // Code compliments of Scott Miller
   //          // support oracle clobs without requiring oracle libraries
   //          // at compile time
   //          // Note this assumes that if you are using the Oracle
   //          // Driver.
   //          // then you have access to the oracle.sql.CLOB class
   //
   //          // First get the oracle clob class
   //          Class oracleClobClass = Class.forName("oracle.sql.CLOB");
   //
   //          // Get the oracle connection class for checking
   //          Class oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection");
   //
   //          // now get the static factory method
   //          Class partypes[] = new Class[3];
   //          partypes[0] = Connection.class;
   //          partypes[1] = Boolean.TYPE;
   //          partypes[2] = Integer.TYPE;
   //          Method createTemporaryMethod = oracleClobClass.getDeclaredMethod("createTemporary",
   //                                                                           partypes);
   //          // now get ready to call the factory method
   //          Field durationSessionField = oracleClobClass.getField("DURATION_SESSION");
   //          Object arglist[] = new Object[3];
   //          Connection conn = realStatement.getConnection();
   //
   //          // Make sure connection object is right type
   //          if (!oracleConnectionClass.isAssignableFrom(conn.getClass())) {
   //            throw new HibernateException("JDBC connection object must be a oracle.jdbc.OracleConnection. "
   //                + "Connection class is " + conn.getClass().getName());
   //          }
   //
   //          arglist[0] = conn;
   //          arglist[1] = Boolean.TRUE;
   //          arglist[2] = durationSessionField.get(null); // null is
   //          // valid
   //          // because of
   //          // static field
   //
   //          // Create our CLOB
   //          Object tempClob = createTemporaryMethod.invoke(null, arglist); // null
   //          // is
   //          // valid
   //          // because
   //          // of
   //          // static
   //          // method
   //
   //          // get the open method
   //          partypes = new Class[1];
   //          partypes[0] = Integer.TYPE;
   //          Method openMethod = oracleClobClass.getDeclaredMethod("open", partypes);
   //
   //          // prepare to call the method
   //          Field modeReadWriteField = oracleClobClass.getField("MODE_READWRITE");
   //          arglist = new Object[1];
   //          arglist[0] = modeReadWriteField.get(null); // null is valid
   //          // because of
   //          // static field
   //
   //          // call open(CLOB.MODE_READWRITE);
   //          openMethod.invoke(tempClob, arglist);
   //
   //          // get the getCharacterOutputStream method
   //          // Method getCharacterOutputStreamMethod =
   //          // oracleClobClass.getDeclaredMethod(
   //          // "getCharacterOutputStream", null );
   //
   //          // use getAsciiOutputStream for special characters,
   //          // using the Writer obtained from 'getCharacterOutputStream"
   //          // causes
   //          // 'No more data to read from socket' when inserting special
   //          // characters
   //          Method getAsciiOutputStreamMethod = oracleClobClass.getDeclaredMethod("getAsciiOutputStream",
   //                                                                                null);
   //
   //          // call the getCharacterOutpitStream method
   //          OutputStream tempClobOutputStream = (OutputStream) getAsciiOutputStreamMethod.invoke(tempClob,
   //                                                                                               null);
   //
   //          // write the string to the clob
   //          tempClobOutputStream.write(((String) value).getBytes());
   //          tempClobOutputStream.flush();
   //          tempClobOutputStream.close();
   //
   //          // get the close method
   //          Method closeMethod = oracleClobClass.getDeclaredMethod("close", null);
   //
   //          // call the close method
   //          closeMethod.invoke(tempClob, null);
   //
   //          // add the clob to the statement
   //          realStatement.setClob(index, (Clob) tempClob);
   //        } catch (ClassNotFoundException e) {
   //          // could not find the class with reflection
   //          throw new HibernateException("Unable to find a required class.\n" + e.getMessage());
   //        } catch (NoSuchMethodException e) {
   //          // could not find the metho with reflection
   //          throw new HibernateException("Unable to find a required method.\n" + e.getMessage());
   //        } catch (NoSuchFieldException e) {
   //          // could not find the field with reflection
   //          throw new HibernateException("Unable to find a required field.\n" + e.getMessage());
   //        } catch (IllegalAccessException e) {
   //          throw new HibernateException("Unable to access a required method or field.\n"
   //              + e.getMessage());
   //        } catch (InvocationTargetException e) {
   //          throw new HibernateException(e.getMessage());
   //        } catch (IOException e) {
   //          throw new HibernateException(e.getMessage());
   //        }
   //      } else {
   //        throw new HibernateException("No CLOBS support. Use driver version "
   //            + ORACLE_DRIVER_MAJOR_VERSION + ", minor " + ORACLE_DRIVER_MINOR_VERSION);
   //      }
   //    } else {
   //      // this is the default way to handle Clobs that seems to work with
   //      // most Databases
   //      String str = (String) value;
   //      StringReader r = new StringReader(str);
   //      stmt.setCharacterStream(index, r, str.length());
   //    }
   //  }

   /**
    * {@inheritDoc}
    */
   public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException
   {
      if (value == null)
      {
         stmt.setNull(index, sqlTypes()[0]);
      }
      else
      {
         final String str = value instanceof String ? (String)value : value.toString();
         stmt.setCharacterStream(index, new StringReader(str), str.length());
      }
   }

   /**
    * This method tries to determine if the passed PreparedStatement is a Wrapper
    * for an actual PreparedStatement. Database objects are often wrapped in
    * pooling implementations to handle connection clean up, and EJB type
    * transaction participation. We need to get at the real PreparedStatement to
    * determin if it is an Oracle PreparedStatement that is being wrapped. This
    * allows us to handle Oracle differently, since Oracle LOB types work
    * differently in JDBC than all other databases.
    * 
    * @param stmt
    * @return The passed statement, or the PreparedStatement that the passed stmt
    *         is wrapping.
    * @throws HibernateException
    */
   PreparedStatement getRealStatement(PreparedStatement stmt) throws HibernateException
   {
      Method[] methods = stmt.getClass().getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         String returnType = methods[i].getReturnType().getName();

         // if the method has no parameters and the return type is either
         // Statement or PreparedStatement
         // then reflectively call this method to get the underlying
         // PreparedStatement
         // (JBoss returns a Statement that we must cast)
         if (((Statement.class.getName().equals(returnType)) || (PreparedStatement.class.getName().equals(returnType)))
            && methods[i].getParameterTypes().length == 0)
         {
            Statement s = null;
            try
            {
               s = (Statement)methods[i].invoke(stmt, null);
            }
            catch (SecurityException e)
            {
               throw new HibernateException("Security Error getting method [getDelegate] on ["
                  + stmt.getClass().getName() + "::" + methods[i].getName() + "]", e);
            }
            catch (IllegalArgumentException e)
            {
               throw new HibernateException("Error calling method [getDelegate] on [" + stmt.getClass().getName()
                  + "::" + methods[i].getName() + "]", e);
            }
            catch (IllegalAccessException e)
            {
               throw new HibernateException("Error calling method [getDelegate] on [" + stmt.getClass().getName()
                  + "::" + methods[i].getName() + "]", e);
            }
            catch (InvocationTargetException e)
            {
               throw new HibernateException("Error calling method [getDelegate] on [" + stmt.getClass().getName()
                  + "::" + methods[i].getName() + "]", e);
            }
            return (PreparedStatement)s;

         }
      }

      // Did not find a parameterless method that returned a Statement, return
      // what we were passed
      return stmt;

   }

   /**
    * @throws HibernateException
    * @see net.sf.hibernate.UserType#deepCopy(java.lang.Object)
    */
   public Object deepCopy(Object value) throws HibernateException
   {
      if (value == null)
      {
         return null;
      }
      String stringValue = (String)value;
      return new String(stringValue);
   }

   /**
    * @see net.sf.hibernate.UserType#isMutable()
    */
   public boolean isMutable()
   {
      return false;
   }

   /**
    * Impl copied from net.sf.hibernate.type.TextType Generated 10:08:31 AM May
    * 21, 2004
    * 
    * @param rs
    * @param names
    * @param owner
    * @return
    * @throws HibernateException
    * @throws SQLException
    * @see net.sf.hibernate.UserType#nullSafeGet(java.sql.ResultSet,
    *      java.lang.String[], java.lang.Object)
    */
   public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException
   {
      // Retrieve the value of the designated column in the current row of
      // this
      // ResultSet object as a java.io.Reader object
      Reader charReader = rs.getCharacterStream(names[0]);

      // if the corresponding SQL value is NULL, the reader we got is NULL as
      // well
      if (charReader == null)
         return null;

      // Fetch Reader content up to the end - and put characters in a
      // StringBuffer
      StringBuffer sb = new StringBuffer();
      try
      {
         char[] buffer = new char[2048];
         while (true)
         {
            int amountRead = charReader.read(buffer, 0, buffer.length);
            if (amountRead == -1)
               break;
            sb.append(buffer, 0, amountRead);
         }
      }
      catch (IOException ioe)
      {
         throw new HibernateException("IOException occurred reading text", ioe);
      }
      finally
      {
         try
         {
            charReader.close();
         }
         catch (IOException e)
         {
            throw new HibernateException("IOException occurred closing stream", e);
         }
      }

      // Return StringBuffer content as a large String
      return sb.toString();
   }

   // Hibernate3 methods !

   /*
    * Reconstruct an object from the cacheable representation. At the very least
    * this method should perform a deep copy if the type is mutable. (optional
    * operation)
    * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable,
    * java.lang.Object)
    */
   public Object assemble(Serializable cached, Object owner) throws HibernateException
   {
      // ?
      return deepCopy(cached);
   }

   /*
    * Transform the object into its cacheable representation. At the very least
    * this method should perform a deep copy if the type is mutable. That may not
    * be enough for some implementations, however; for example, associations must
    * be cached as identifier values. (optional operation)
    * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
    */
   public Serializable disassemble(Object value) throws HibernateException
   {
      // ?
      return new String(value.toString());
   }

   /*
    * Get a hashcode for the instance, consistent with persistence "equality"
    * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
    */
   public int hashCode(Object x) throws HibernateException
   {
      // ?
      return super.hashCode();
   }

   /*
    * During merge, replace the existing (target) value in the entity we are
    * merging to with a new (original) value from the detached entity we are
    * merging. For immutable objects, or null values, it is safe to simply return
    * the first parameter. For mutable objects, it is safe to return a copy of
    * the first parameter. For objects with component values, it might make sense
    * to recursively replace component values.
    * @see org.hibernate.usertype.UserType#replace(java.lang.Object,
    * java.lang.Object, java.lang.Object)
    */
   public Object replace(Object original, Object target, Object owner) throws HibernateException
   {
      // ?
      return original;
   }
}
