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
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.commons.utils.IdentifierUtil;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.database.DBObjectMapper;
import org.exoplatform.services.database.DBObjectQuery;
import org.exoplatform.services.database.DBPageList;
import org.exoplatform.services.database.ExoDatasource;
import org.exoplatform.services.database.StandardSQLDAO;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 * Created by The eXo Platform SAS Apr 7, 2007
 */
public class MembershipDAOImpl extends StandardSQLDAO<MembershipImpl> implements MembershipHandler
{

   protected static Log log = ExoLogger.getLogger("exo.core.component.organization.jdbc.MembershipDAOImpl");

   protected final OrganizationService service;

   protected ListenerService listenerService_;

   public MembershipDAOImpl(ListenerService lService, ExoDatasource datasource, DBObjectMapper<MembershipImpl> mapper,
      OrganizationService service)
   {
      super(datasource, mapper, MembershipImpl.class);
      this.service = service;
      this.listenerService_ = lService;
   }

   public Membership createMembershipInstance()
   {
      return new MembershipImpl();
   }

   /**
    * {@inheritDoc}
    */
   public void createMembership(Membership membership, boolean broadcast) throws Exception
   {
      if (service.getMembershipTypeHandler().findMembershipType(membership.getMembershipType()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + membership.getId()
            + " because membership type " + membership.getMembershipType() + " not exists.");
      }
      
      if (service.getGroupHandler().findGroupById(membership.getGroupId()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + membership.getId() + ", because group "
            + membership.getGroupId() + " does not exist.");
      }

      if (service.getUserHandler().findUserByName(membership.getUserName()) == null)
      {
         throw new InvalidNameException("Can not create membership record " + membership.getId() + ", because user "
            + membership.getGroupId() + " does not exist.");
      }

      // check if we already have membership record
      if (findMembershipByUserGroupAndType(membership.getUserName(), membership.getGroupId(),
         membership.getMembershipType()) != null)
      {
         return;
      }

      MembershipImpl membershipImpl = (MembershipImpl)membership;
      if (broadcast)
      {
         listenerService_.broadcast("organization.membership.preSave", this, membershipImpl);
      }

      membershipImpl.setId(IdentifierUtil.generateUUID(membership));
      super.save(membershipImpl);

      if (broadcast)
      {
         listenerService_.broadcast("organization.membership.postSave", this, membershipImpl);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void linkMembership(User user, Group group, MembershipType mt, boolean broadcast) throws Exception
   {
      if (user == null)
      {
         throw new InvalidNameException("Can not create membership record because group is null");
      }

      if (group == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because group is null");
      }

      if (mt == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because membership type is null");
      }

      if (log.isDebugEnabled())
      {
         log.debug("LINK MEMBER SHIP (" + user.getUserName() + ", " + group.getId() + " , " + mt.getName() + ");");
      }

      MembershipImpl membership = new MembershipImpl();
      membership.setUserName(user.getUserName());
      membership.setMembershipType(mt.getName());
      membership.setGroupId(group.getId());
      createMembership(membership, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembership(String id) throws Exception
   {
      if (id == null)
         return null;
      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("MEMBERSHIP_ID", id);
      Membership membership = loadUnique(query.toQuery());

      if (membership == null)
      {
         throw new InvalidNameException("Can't find membership with id " + id);
      }

      return membership;
   }

   /**
    * {@inheritDoc}
    */
   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {

      if (userName == null || groupId == null || type == null)
         return null;
      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("USER_NAME", userName);
      query.addLIKE("GROUP_ID", groupId);
      query.addLIKE("MEMBERSHIP_TYPE", type);
      Membership member = loadUnique(query.toQuery());
      if (log.isDebugEnabled())
         log.debug("FIND MEMBERSHIP BY USER " + userName + ", GROUP " + groupId + ", TYPE " + type + " - "
            + (member != null));
      return member;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findMembershipsByGroup(Group group) throws Exception
   {
      if (group == null)
         return null;
      List<MembershipImpl> list = new ArrayList<MembershipImpl>();
      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("GROUP_ID", group.getId());
      loadInstances(query.toQuery(), list);
      return list;
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception
   {
      if (group == null)
      {
         return null;
      }

      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("GROUP_ID", group.getId());

      return new JDBCListAccess<Membership>(this, query.toQuery(), query.toCountQuery());
   }

   /**
    * {@inheritDoc}
    */
   public Collection findMembershipsByUser(String userName) throws Exception
   {
      if (userName == null)
         return null;
      List<MembershipImpl> list = new ArrayList<MembershipImpl>();
      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("USER_NAME", userName);
      loadInstances(query.toQuery(), list);
      if (log.isDebugEnabled())
         log.debug("FIND MEMBERSHIP BY USER " + userName + " Size = " + list.size());
      return list;
   }

   /**
    * {@inheritDoc}
    */
   public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      if (userName == null || groupId == null)
         return null;
      List<MembershipImpl> list = new ArrayList<MembershipImpl>();
      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("USER_NAME", userName);
      query.addLIKE("GROUP_ID", groupId);
      loadInstances(query.toQuery(), list);
      if (log.isDebugEnabled())
         log.debug("FIND MEMBERSHIP BY USER " + userName + ", GROUP " + groupId + " Size = " + list.size());
      return list;
   }

   /**
    * {@inheritDoc}
    */
   public Membership removeMembership(String id, boolean broadcast) throws Exception
   {
      DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      query.addLIKE("MEMBERSHIP_ID", id);
      Connection connection = eXoDS_.getConnection();
      try
      {
         MembershipImpl membershipImpl = super.loadUnique(connection, query.toQuery());
         if (membershipImpl == null)
            return null;
         if (broadcast)
            listenerService_.broadcast("organization.membership.preDelete", this, membershipImpl);
         String sql = eXoDS_.getQueryBuilder().createRemoveQuery(type_, membershipImpl.getDBObjectId());
         super.execute(connection, sql, (MembershipImpl)null);
         if (broadcast)
            listenerService_.broadcast("organization.membership.postDelete", this, membershipImpl);
         return membershipImpl;
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Collection removeMembershipByUser(String username, boolean broadcast) throws Exception
   {
      // DBObjectQuery<MembershipImpl> query = new
      // DBObjectQuery<MembershipImpl>(MembershipImpl.class);
      // query.addLIKE("userName", username);
      List<Membership> members = (List<Membership>)findMembershipsByUser(username);
      for (Membership member : members)
      {
         removeMembership(member.getId(), true);
      }
      return members;
   }

   /**
    * {@inheritDoc}
    */
   public Collection removeMemberships(DBObjectQuery<MembershipImpl> query, boolean broadcast) throws Exception
   {
      DBPageList<MembershipImpl> pageList = new DBPageList<MembershipImpl>(20, this, query);
      List<MembershipImpl> list = pageList.getAll();
      Connection connection = eXoDS_.getConnection();
      try
      {
         for (MembershipImpl membershipImpl : list)
         {
            if (broadcast)
               listenerService_.broadcast("organization.membership.preDelete", this, membershipImpl);
            if (membershipImpl == null)
               return null;
            String sql = eXoDS_.getQueryBuilder().createRemoveQuery(type_, membershipImpl.getDBObjectId());
            super.execute(connection, sql, (MembershipImpl)null);
            if (broadcast)
               listenerService_.broadcast("organization.membership.postDelete", this, membershipImpl);
         }
         return list;
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void addMembershipEventListener(MembershipEventListener listener)
   {
      throw new UnsupportedOperationException("This method is not supported anymore, please use the new api");
   }

   /**
    * {@inheritDoc}
    */
   public void removeMembershipEventListener(MembershipEventListener listener)
   {
      throw new UnsupportedOperationException("This method is not supported anymore, please use the new api");
   }
}
