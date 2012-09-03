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
package org.exoplatform.services.database;

import org.exoplatform.services.database.annotation.Table;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 25, 2004
 * @version $Id: ObjectQuery.java 6006 2006-06-06 10:01:27Z thangvn $
 */
public class DBObjectQuery<T extends DBObject>
{

   private static SimpleDateFormat ft_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   private Class<? extends DBObject> type_;

   private String orderBy_;

   private String groupBy_;

   private List<Parameter> parameters_;

   private List<Parameter> selectParameter_;

   public DBObjectQuery(Class<T> type)
   {
      type_ = type;
      parameters_ = new ArrayList<Parameter>(3);
      selectParameter_ = new ArrayList<Parameter>(10);
   }

   public DBObjectQuery<T> addEQ(String field, Object value)
   {
      if (value == null)
         return this;
      parameters_.add(new Parameter(field, " = ", value));
      return this;
   }

   public DBObjectQuery<T> addGT(String field, Object value)
   {
      if (value == null)
         return this;
      parameters_.add(new Parameter(field, " > ", value));
      return this;
   }

   public DBObjectQuery<T> addLT(String field, Object value)
   {
      if (value == null)
         return this;
      parameters_.add(new Parameter(field, " < ", value));
      return this;
   }

   public DBObjectQuery<T> addLIKE(String field, String value)
   {
      if (value == null || value.length() < 1)
         return this;
      parameters_.add(new Parameter(field, " LIKE ", optimizeInputString(value)));
      return this;
   }

   public DBObjectQuery<T> addSUM(String field)
   {
      selectParameter_.add(new Parameter("SUM", field));
      return this;
   }

   public DBObjectQuery<T> addSelect(String field, String value)
   {
      selectParameter_.add(new Parameter(field, " AS ", value));
      return this;
   }

   public DBObjectQuery<T> addSelect(String... fields)
   {
      for (String field : fields)
      {
         selectParameter_.add(new Parameter(field, null, null));
      }
      return this;
   }

   public DBObjectQuery<T> addSelect(String field)
   {
      selectParameter_.add(new Parameter(field, null, null));
      return this;
   }

   public DBObjectQuery<T> addSelectCount(String type)
   {
      selectParameter_.add(new Parameter("countselect", type));
      return this;
   }

   public DBObjectQuery<T> addSelectMaxMin(String op, String field)
   {
      selectParameter_.add(new Parameter(op, field));
      return this;
   }

   public DBObjectQuery<T> setAscOrderBy(String field)
   {
      orderBy_ = " ORDER BY " + field + " ASC";
      return this;
   }

   public DBObjectQuery<T> setDescOrderBy(String field)
   {
      orderBy_ = " ORDER BY " + field + " DESC";
      return this;
   }

   public DBObjectQuery setGroupBy(String field)
   {
      groupBy_ = " GROUP BY " + field;
      return this;
   }

   public String toQuery()
   {
      return constuctQuery(false);
   }

   public String toQueryUseOR()
   {
      return constuctQuery(true);
   }

   private String constuctQuery(boolean useOR)
   {
      StringBuilder builder = new StringBuilder("SELECT ");
      if (selectParameter_.size() > 0)
      {
         for (int i = 0; i < selectParameter_.size(); i++)
         {
            if (i > 0)
               builder.append(", ");
            parameters_.get(i).build(builder);
         }
      }
      else
      {
         builder.append(" * ");
      }

      Table table = type_.getAnnotation(Table.class);
      builder.append(" FROM ").append(table.name());
      if (parameters_.size() > 0)
      {
         builder.append(" WHERE ");
         for (int i = 0; i < parameters_.size(); i++)
         {
            if (i > 0)
               builder.append(useOR ? " OR " : " AND ");
            parameters_.get(i).build(builder);
         }
      }
      if (orderBy_ != null)
         builder.append(orderBy_);
      return builder.toString();
   }

   public String toCountQuery()
   {
      return consturctCountQuery(false);
   }

   public String toCountQueryUseOR()
   {
      return consturctCountQuery(true);
   }

   private String consturctCountQuery(boolean useOR)
   {
      StringBuilder builder = new StringBuilder();
      Table table = type_.getAnnotation(Table.class);
      builder.append("SELECT COUNT(*) FROM  ").append(table.name());
      if (parameters_.size() > 0)
      {
         builder.append(" WHERE ");
         for (int i = 0; i < parameters_.size(); i++)
         {
            if (i > 0)
               builder.append(useOR ? " OR " : " AND ");
            parameters_.get(i).build(builder);
         }
      }
      return builder.toString();
   }

   /*
    * public String getHibernateGroupByQuery() { StringBuilder b = new
    * StringBuilder("SELECT ") ; if(selectParameter_.size() > 0){ for(int i = 0;
    * i < selectParameter_.size(); i++){ Parameter p = selectParameter_.get(i) ;
    * if(p.op_.equals("fieldselect")){ b.append(p.field_) ; }else
    * if(p.op_.equals("countselect")){ b.append("COUNT"); if (p.field_ != "" ||
    * p.field_.length() > 0){ b.append("(").append(p.field_).append(" )"); }else{
    * b.append("(*)"); } }else {
    * b.append(p.op_).append("(").append(p.field_).append(") "); } if(i <
    * selectParameter_.size() - 1 ) b.append(" , ") ; } } Table table =
    * type_.getAnnotation(Table.class) ; b.append(" FROM ").append(table.name())
    * ; if(parameters_.size() > 0) { b.append(" WHERE ") ; for(int i = 0; i <
    * parameters_.size(); i ++) { if(i > 0) b.append(" AND ") ; Parameter p =
    * parameters_.get(i) ; if(p.value_ instanceof String) {
    * b.append(p.field_).append(p.op_).append("'").append(p.value_).append("'") ;
    * } else if(p.value_ instanceof Date) { String value = ft_.format((Date)
    * p.value_) ;
    * b.append(' ').append(p.field_).append(p.op_).append("'").append(
    * value).append("'") ; } else if(p.op_.equals("max") || p.op_.equals("min")){
    * b.append(p.op_).append("(").append(p.field_).append(") "); } else{
    * b.append(' ').append(p.field_).append(p.op_).append(p.value_); } } }
    * if(groupBy_ != null ) b.append(groupBy_ ); if(orderBy_ != null )
    * b.append(orderBy_ ); return b.toString() ; }
    */

   public String optimizeInputString(String value)
   {
      value = value.replace('*', '%');
      value = value.replaceAll("'", "&#39;");
      value = value.replaceAll("<", "&#60;");
      value = value.replaceAll(">", "&#62;");
      return value;
   }

   public List<Parameter> getParameters()
   {
      return parameters_;
   }

   static public class Parameter
   {

      String op_;

      String field_;

      String label_;

      Object value_;

      public Parameter(String field, String op, Object value)
      {
         op_ = op;
         field_ = field;
         value_ = value;
      }

      public Parameter(String op, String field)
      {
         op_ = op;
         field_ = field;
      }

      void build(StringBuilder builder)
      {
         builder.append(' ').append(field_).append(op_);
         if (op_ == null || op_.trim().length() < 1 || value_ == null)
            return;
         builder.append(' ');
         if (CharSequence.class.isInstance(value_))
         {
            builder.append('\'').append(value_).append('\'');
         }
         else if (value_ instanceof Date)
         {
            String value = ft_.format((Date)value_);
            builder.append("'").append(value).append("'");
         }
         else
         {
            builder.append(value_);
         }
      }
   }

}
