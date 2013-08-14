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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipEventListenerHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.services.security.PermissionConstants;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Aug 22, 2003 Time: 4:51:21 PM
 */
public class MembershipDAOImpl implements MembershipHandler, MembershipEventListenerHandler
{

   private static final String queryFindMembershipByUserGroupAndType =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl where "
         + "m.userName = :username and m.groupId = :groupid and m.membershipType = :membershiptype ";

   private static final String queryFindMembershipByType =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl where "
         + "m.membershipType = :membershiptype ";

   private static final String queryFindMembershipsByUserAndGroup =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl where "
         + "m.userName = :username and m.groupId = :groupid ";

   private static final String queryFindMembershipsByGroup =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl where m.groupId = :groupid ";

   private static final String queryFindMembership =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl where m.id = :id ";

   private static final String queryFindMembershipsByUser =
      "from m in class org.exoplatform.services.organization.impl.MembershipImpl where m.userName = :username ";

   private HibernateService service_;

   private List<MembershipEventListener> listeners_;

   /**
    * Organization service.
    */
   protected final OrganizationService orgService;

   public MembershipDAOImpl(HibernateService service, OrganizationService orgService)
   {
      this.service_ = service;
      this.orgService = orgService;
      this.listeners_ = new ArrayList<MembershipEventListener>(3);
   }

   /**
    * {@inheritDoc}
    */
   public void addMembershipEventListener(MembershipEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners_.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeMembershipEventListener(MembershipEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners_.remove(listener);
   }

   /**
    * {@inheritDoc}
    */
   final public Membership createMembershipInstance()
   {
      return new MembershipImpl();
   }

   /**
    * {@inheritDoc}
    */
   public void createMembership(Membership m, boolean broadcast) throws Exception
   {
      if (!m.getMembershipType().equals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.getName())
         && orgService.getMembershipTypeHandler().findMembershipType(m.getMembershipType()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId() + ", because membership"
            + "type " + m.getMembershipType() + " does not exist.");
      }

      if (orgService.getGroupHandler().findGroupById(m.getGroupId()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId() + ", because group "
            + m.getGroupId() + " does not exist.");
      }

      if (orgService.getUserHandler().findUserByName(m.getUserName()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + m.getId() + ", because user "
            + m.getGroupId() + " does not exist.");
      }

      // check if we already have membership record
      if (findMembershipByUserGroupAndType(m.getUserName(), m.getGroupId(), m.getMembershipType()) != null)
      {
         return;
      }

      if (broadcast)
      {
         preSave(m, true);
      }

      Session session = service_.openSession();
      session.save(m);
      session.flush();

      if (broadcast)
      {
         postSave(m, true);
      }
   }

   /**
    * {@inheritDoc}
    */
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
      createMembership(membership, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public void saveMembership(Membership m, boolean broadcast) throws Exception
   {
      if (broadcast)
         preSave(m, false);

      Session session = service_.openSession();
      session.update(m);
      session.flush();

      if (broadcast)
         postSave(m, false);

   }

   /**
    * {@inheritDoc}
    */
   public Membership removeMembership(String id, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();

      Membership m = (Membership)service_.findOne(session, queryFindMembership, id);
      if (m != null)
      {
         if (broadcast)
            preDelete(m);

         session.delete(m);
         session.flush();

         if (broadcast)
            postDelete(m);
      }
      return m;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Membership> removeMembershipByUser(String username, boolean broadcast) throws Exception
   {
      Collection<Membership> collection = findMembershipsByUser(username);
      Iterator<?> iter = collection.iterator();
      while (iter.hasNext())
      {
         Membership m = (Membership)iter.next();
         if (m != null)
         {
            if (broadcast)
               preDelete(m);

            Session session = service_.openSession();
            session.delete(m);
            session.flush();

            if (broadcast)
               postDelete(m);

         }
      }
      return collection;
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {
      Session session = service_.openSession();
      List<?> memberships =
         session.createQuery(queryFindMembershipByUserGroupAndType).setString("username", userName)
            .setString("groupid", groupId).setString("membershiptype", type).list();
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

   /**
    * {@inheritDoc}
    */
   public Collection<Membership> findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      Session session = service_.openSession();
      @SuppressWarnings("unchecked")
      List<Membership> memberships =
         session.createQuery(queryFindMembershipsByUserAndGroup).setString("username", userName)
            .setString("groupid", groupId).list();
      return memberships;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Membership> findMembershipsByUser(String userName) throws Exception
   {
      Session session = service_.openSession();
      @SuppressWarnings("unchecked")
      List<Membership> memberships = session.createQuery(queryFindMembershipsByUser).setString("username", userName).list();
      return memberships;
   }

   void removeMembershipEntriesOfUser(String userName, Session session) throws Exception
   {
      List<?> entries = session.createQuery(queryFindMembershipsByUser).setString("username", userName).list();
      for (int i = 0; i < entries.size(); i++)
      {
         session.delete(entries.get(i));
      }
   }

   void removeMembershipEntriesOfGroup(Group group, Session session) throws Exception
   {
      List<?> entries = session.createQuery(queryFindMembershipsByGroup).setString("groupid", group.getId()).list();
      for (int i = 0; i < entries.size(); i++)
      {
         session.delete(entries.get(i));
      }
   }

   void removeMembershipEntriesOfMembershipType(MembershipType mt, Session session) throws Exception
   {
      List<?> entries = session.createQuery(queryFindMembershipByType).setString("membershiptype", mt.getName()).list();
      for (int i = 0; i < entries.size(); i++)
      {
         session.delete(entries.get(i));
      }
   }

   Collection<?> findMembershipsByUser(String userName, Session session) throws Exception
   {
      return session.createQuery(queryFindMembershipsByUser).setString("username", userName).list();
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Membership> findMembershipsByGroup(Group group) throws Exception
   {
      Session session = service_.openSession();
      @SuppressWarnings("unchecked")
      List<Membership> memberships = session.createQuery(queryFindMembershipsByGroup).setString("groupid", group.getId()).list();
      return memberships;
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception
   {
      String findQuery =
         "select m from m in class org.exoplatform.services.organization.impl.MembershipImpl where m.groupId = '"
            + group.getId() + "'";

      String countQuery =
         "select count(m) from m in class org.exoplatform.services.organization.impl.MembershipImpl where m.groupId = '"
            + group.getId() + "'";

      return new HibernateListAccess<Membership>(service_, findQuery, countQuery);
   }

   /**
    * {@inheritDoc}
    */
   public Collection<?> findMembershipsByGroupId(String groupId) throws Exception
   {
      Session session = service_.openSession();
      List<?> memberships = session.createQuery(queryFindMembershipsByGroup).setString("groupid", groupId).list();
      return memberships;
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembership(String id) throws Exception
   {
      Session session = service_.openSession();
      List<?> memberships = session.createQuery(queryFindMembership).setString("id", id).list();
      if (memberships.size() == 0)
      {
         throw new Exception("No membership with id: " + id + "found.");
      }
      else if (memberships.size() == 1)
      {
         return (Membership)memberships.get(0);
      }
      else
      {
         throw new Exception("Found more than 1 membership: " + memberships.size());
      }
   }

   private void preSave(Membership membership, boolean isNew) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = listeners_.get(i);
         listener.preSave(membership, isNew);
      }
   }

   private void postSave(Membership membership, boolean isNew) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = listeners_.get(i);
         listener.postSave(membership, isNew);
      }
   }

   private void preDelete(Membership membership) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = listeners_.get(i);
         listener.preDelete(membership);
      }
   }

   private void postDelete(Membership membership) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = listeners_.get(i);
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
