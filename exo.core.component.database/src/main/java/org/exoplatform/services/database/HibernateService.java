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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by The eXo Platform SAS
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Jun 14, 2003 Time: 1:12:22 PM
 */
public interface HibernateService
{
   public Configuration getHibernateConfiguration();

   public Session openSession();

   public Session openNewSession();

   public void closeSession(Session session);

   /** Close the session that assign to the current thread */
   public void closeSession();

   SessionFactory getSessionFactory();

   public Object findOne(Session session, String query, String id) throws Exception;

   public Collection<?> findAll(Session session, String query) throws Exception;

   public Object findExactOne(Session session, String query, String id) throws Exception;

   public Object findOne(Class<?> clazz, java.io.Serializable id) throws Exception;

   public Object findOne(ObjectQuery q) throws Exception;

   public Object create(Object obj) throws Exception;

   public Object update(Object obj) throws Exception;

   public Object save(Object obj) throws Exception;

   public Object remove(Object obj) throws Exception;

   public Object remove(Class<?> clazz, Serializable id) throws Exception;

   public Object remove(Session session, Class<?> clazz, Serializable id) throws Exception;
}
