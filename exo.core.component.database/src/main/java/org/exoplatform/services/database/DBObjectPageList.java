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

import org.exoplatform.commons.utils.PageList;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 21, 2004
 * @version $Id: DBObjectPageList.java 5332 2006-04-29 18:32:44Z geaz $
 */
public class DBObjectPageList extends PageList<Object>
{

   private String findQuery_;

   private String countQuery_;

   private HibernateService service_;

   private Map<String, Object> binding = new HashMap<String, Object>();

   public DBObjectPageList(HibernateService service, Class<?> objectType) throws Exception
   {
      super(20);
      service_ = service;
      findQuery_ = "from o in class " + objectType.getName();
      countQuery_ = "select count(o) from " + objectType.getName() + " o";
      Session session = service_.openSession();
      List<?> l = session.createQuery(countQuery_).list();
      Number count = (Number)l.get(0);
      setAvailablePage(count.intValue());
   }

   public DBObjectPageList(HibernateService service, ObjectQuery oq) throws Exception
   {
      super(20);
      service_ = service;
      findQuery_ = oq.getHibernateQueryWithBinding();
      countQuery_ = oq.getHibernateCountQueryWithBinding();
      binding = oq.getBindingFields();

      Session session = service_.openSession();

      Query countQuery = session.createQuery(countQuery_);
      bindFields(countQuery);

      List<?> l = countQuery.list();

      Number count = (Number)l.get(0);
      setAvailablePage(count.intValue());
   }

   public DBObjectPageList(HibernateService service, int pageSize, String query, String countQuery) throws Exception
   {
      super(pageSize);
      service_ = service;
      findQuery_ = query;
      countQuery_ = countQuery;
      Session session = service_.openSession();
      List<?> l = session.createQuery(countQuery_).list();
      Number count = (Number)l.get(0);
      setAvailablePage(count.intValue());
   }

   @Override
   protected void populateCurrentPage(int page) throws Exception
   {
      Session session = service_.openSession();
      Query query = session.createQuery(findQuery_);
      bindFields(query);

      int from = getFrom();
      query.setFirstResult(from);
      query.setMaxResults(getTo() - from);
      currentListPage_ = query.list();
   }

   @Override
   public List getAll() throws Exception
   {
      Session session = service_.openSession();

      Query query = session.createQuery(findQuery_);
      bindFields(query);

      return query.list();
   }

   /**
      * Bind a value to a named query parameter.
      * 
      * @param query
      */
   private void bindFields(Query query)
   {
      for (Entry<String, Object> entry : binding.entrySet())
      {
         query.setParameter(entry.getKey(), entry.getValue());
      }
   }
}
