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

import org.exoplatform.commons.utils.IdentifierUtil;
import org.exoplatform.commons.utils.ListenerStack;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipEventListenerHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.hibernate.Session;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net Date: Aug 22, 2003 Time: 4:51:21 PM
 */
public class MembershipDAOImpl implements MembershipHandler, MembershipEventListenerHandler
{

   private static final String queryFindMembershipByUserGroupAndType =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl " + "where m.userName = ? "
         + "  and m.groupId = ? " + "  and m.membershipType = ? ";

   private static final String queryFindMembershipsByUserAndGroup =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl " + "where m.userName = ? "
         + "  and m.groupId = ? ";

   private static final String queryFindMembershipsByGroup =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl " + "where m.groupId = ? ";

   private static final String queryFindMembership =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl " + "where m.id = ? ";

   private static final String queryFindMembershipsByUser =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl " + "where m.userName = ? ";

   private HibernateService service_;

   private List listeners_;

   /**
    * Organization service.
    */
   protected final OrganizationService orgService;

   public MembershipDAOImpl(HibernateService service, OrganizationService orgService)
   {
      this.service_ = service;
      this.orgService = orgService;
      this.listeners_ = new ListenerStack(5);
   }

   public void addMembershipEventListener(MembershipEventListener listener)
   {
      listeners_.add(listener);
   }

   final public Membership createMembershipInstance()
   {
      return new MembershipImpl();
   }

   public void createMembership(Membership m, boolean broadcast) throws Exception
   {
      if (orgService.getMembershipTypeHandler().findMembershipType(m.getMembershipType()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId()
                  + " because membership type " + m.getMembershipType() + " is not exists.");
      }

      if (broadcast)
         preSave(m, true);
      Session session = service_.openSession();
      session.save(m);
      if (broadcast)
         postSave(m, true);
      session.flush();
   }

   public void linkMembership(User user, Group g, MembershipType mt, boolean broadcast) throws Exception
   {
      if (user == null)
      {
         throw new InvalidNameException("Can not create membership record because user is null");
      }

      if (g == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because group is null");
      }

      if (mt == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because membership type is null");
      }

      MembershipImpl membership = new MembershipImpl();
      membership.setUserName(user.getUserName());
      membership.setMembershipType(mt.getName());
      membership.setGroupId(g.getId());
      membership.setId(IdentifierUtil.generateUUID(membership));

      if (findMembershipByUserGroupAndType(user.getUserName(), g.getId(), mt.getName()) != null)
         return;

      createMembership(membership, broadcast);
   }

   public void saveMembership(Membership m, boolean broadcast) throws Exception
   {
      if (broadcast)
         preSave(m, false);
      Session session = service_.openSession();
      session.update(m);
      if (broadcast)
         postSave(m, false);
      session.flush();
   }

   public Membership removeMembership(String id, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();

      Membership m = (Membership)service_.findOne(session, queryFindMembership, id);
      if (m != null)
      {
         if (broadcast)
            preDelete(m);
         session = service_.openSession();
         session.delete(m);
         if (broadcast)
            postDelete(m);
         session.flush();
      }
      return m;
   }

   public Collection removeMembershipByUser(String username, boolean broadcast) throws Exception
   {
      Collection collection = findMembershipsByUser(username);
      Iterator iter = collection.iterator();
      while (iter.hasNext())
      {
         Membership m = (Membership)iter.next();
         if (m != null)
         {
            if (broadcast)
               preDelete(m);
            Session session = service_.openSession();
            session.delete(m);
            if (broadcast)
               postDelete(m);
            session.flush();
         }
      }
      return collection;
   }

   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {
      Session session = service_.openSession();
      // Object[] args = new Object[] { userName, groupId , type};
      // Type[] types = new Type[] { Hibernate.STRING, Hibernate.STRING,
      // Hibernate.STRING };
      List memberships =
         session.createQuery(queryFindMembershipByUserGroupAndType).setString(0, userName).setString(1, groupId)
            .setString(2, type).list();
      if (memberships.size() == 0)
      {
         return null;
      }
      else if (memberships.size() == 1)
      {
         return (Membership)memberships.get(0);
      }
      else
      {
         throw new Exception("Expect 0 or 1 membership but found" + memberships.size());
      }
   }

   public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      Session session = service_.openSession();
      // Object[] args = new Object[] { userName, groupId };
      // Type[] types = new Type[] { Hibernate.STRING, Hibernate.STRING };
      List memberships =
         session.createQuery(queryFindMembershipsByUserAndGroup).setString(0, userName).setString(1, groupId).list();
      return memberships;
   }

   public Collection findMembershipsByUser(String userName) throws Exception
   {
      Session session = service_.openSession();
      List memberships = session.createQuery(queryFindMembershipsByUser).setString(0, userName).list();
      return memberships;
   }

   static void removeMembershipEntriesOfUser(String userName, Session session) throws Exception
   {
      List entries = session.createQuery(queryFindMembershipsByUser).setString(0, userName).list();
      for (int i = 0; i < entries.size(); i++)
         session.delete(entries.get(i));
   }

   static void removeMembershipEntriesOfGroup(Group group, Session session) throws Exception
   {
      List entries = session.createQuery(queryFindMembershipsByGroup).setString(0, group.getId()).list();
      for (int i = 0; i < entries.size(); i++)
         session.delete(entries.get(i));
   }

   Collection findMembershipsByUser(String userName, Session session) throws Exception
   {
      return session.createQuery(queryFindMembershipsByUser).setString(0, userName).list();
   }

   public Collection findMembershipsByGroup(Group group) throws Exception
   {
      Session session = service_.openSession();
      List memberships = session.createQuery(queryFindMembershipsByGroup).setString(0, group.getId()).list();
      return memberships;
   }

   public Collection findMembershipsByGroupId(String groupId) throws Exception
   {
      Session session = service_.openSession();
      // List memberships = session.find( queryFindMembershipsByGroup, groupId,
      // Hibernate.STRING );
      List memberships = session.createQuery(queryFindMembershipsByGroup).setString(0, groupId).list();
      return memberships;
   }

   public Membership findMembership(String id) throws Exception
   {
      Session session = service_.openSession();
      List memberships = session.createQuery(queryFindMembership).setString(0, id).list();
      if (memberships.size() == 0)
      {
         return null;
      }
      else if (memberships.size() == 1)
      {
         return (Membership)memberships.get(0);
      }
      else
      {
         throw new Exception("Expect 0 or 1 membership but found" + memberships.size());
      }
      // Membership membership =
      // (Membership) session.createQuery(queryFindMembership).setString(0,
      // id).list() ;
      // return membership;
   }

   private void preSave(Membership membership, boolean isNew) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.preSave(membership, isNew);
      }
   }

   private void postSave(Membership membership, boolean isNew) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.postSave(membership, isNew);
      }
   }

   private void preDelete(Membership membership) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.preDelete(membership);
      }
   }

   private void postDelete(Membership membership) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.postDelete(membership);
      }
   }

   /**
    * {@inheritDoc}
    */
   public List<MembershipEventListener> getMembershipListeners()
   {
      return Collections.unmodifiableList(listeners_);
   }
}
