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
/**
 * 
 */
/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.organization.hibernate;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.User;
import org.hibernate.Session;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: HibernateUserListAccess.java 111 2008-11-11 11:11:11Z $
 */
public abstract class HibernateUserListAccess implements ListAccess<User>
{

   /**
    * The Hibernate Service.
    */
   protected HibernateService service;

   /**
    * Find query string.
    */
   protected String findQuery;

   /**
    * Count query string.
    */
   protected String countQuery;

   /**
    * HibernateUserListAccess constructor.
    * 
    * @param service
    *          The Hibernate Service.
    * @param findQuery
    *          Find query string
    * @param countQuery
    *          Count query string
    */
   public HibernateUserListAccess(HibernateService service, String findQuery, String countQuery)
   {
      this.service = service;
      this.findQuery = findQuery;
      this.countQuery = countQuery;
   }

   /**
    * {@inheritDoc}
    */
   public int getSize() throws Exception
   {
      Session session = service.openSession();
      return getSize(session);
   }

   /**
    * {@inheritDoc}
    */
   public User[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      Session session = service.openSession();
      return load(session, index, length);
   }

   protected abstract User[] load(Session session, int index, int length) throws Exception;

   protected abstract int getSize(Session session) throws Exception;
}
