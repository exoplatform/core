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
package org.exoplatform.services.organization.hibernate;

import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.hibernate.Session;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Aug 22, 2003 Time: 4:51:21 PM
 */
public class MembershipTypeDAOImpl implements MembershipTypeHandler
{
   private static final String queryFindMembershipType =
      "from m in class org.exoplatform.services.organization.impl.MembershipTypeImpl " + "where m.name = ? ";

   private static final String queryFindAllMembershipType =
      "from m in class org.exoplatform.services.organization.impl.MembershipTypeImpl";

   private HibernateService service_;

   public MembershipTypeDAOImpl(HibernateService service)
   {
      service_ = service;
   }

   final public MembershipType createMembershipTypeInstance()
   {
      return new MembershipTypeImpl();
   }

   public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      Date now = new Date();
      mt.setCreatedDate(now);
      mt.setModifiedDate(now);
      session.save(mt);
      session.flush();
      return mt;
   }

   public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      Date now = new Date();
      mt.setModifiedDate(now);
      session.update(mt);
      session.flush();
      return mt;
   }

   public MembershipType findMembershipType(String name) throws Exception
   {
      Session session = service_.openSession();
      MembershipType m = (MembershipType)service_.findOne(session, queryFindMembershipType, name);
      return m;
   }

   public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      MembershipTypeImpl m = (MembershipTypeImpl)session.get(MembershipTypeImpl.class, name);
      try
      {
         List entries =
            session.createQuery(
               "from m in class " + " org.exoplatform.services.organization.impl.MembershipImpl "
                  + "where m.membershipType = '" + name + "'").list();
         for (int i = 0; i < entries.size(); i++)
            session.delete(entries.get(i));
      }
      catch (Exception exp)
      {
      }

      if (m != null)
      {
         session.delete(m);
         session.flush();
      }
      return m;
   }

   public Collection findMembershipTypes() throws Exception
   {
      Session session = service_.openSession();
      return session.createQuery(queryFindAllMembershipType).list();
   }
}
