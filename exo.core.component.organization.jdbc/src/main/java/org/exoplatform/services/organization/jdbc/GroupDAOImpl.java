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

import org.exoplatform.commons.exception.UniqueObjectException;
import org.exoplatform.services.database.DBObjectMapper;
import org.exoplatform.services.database.DBObjectQuery;
import org.exoplatform.services.database.DBPageList;
import org.exoplatform.services.database.ExoDatasource;
import org.exoplatform.services.database.StandardSQLDAO;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 * Created by The eXo Platform SAS Apr 7, 2007
 */
public class GroupDAOImpl extends StandardSQLDAO<GroupImpl> implements GroupHandler
{

   protected static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.jdbc.GroupDAOImpl");

   protected ListenerService listenerService_;

   protected final OrganizationService orgService;

   public GroupDAOImpl(OrganizationService orgSerivce, ListenerService lService, ExoDatasource datasource,
      DBObjectMapper<GroupImpl> mapper)
   {
      super(datasource, mapper, GroupImpl.class);

      this.orgService = orgSerivce;
      this.listenerService_ = lService;
   }

   public Group createGroupInstance()
   {
      return new GroupImpl();
   }

   public void createGroup(Group group, boolean broadcast) throws Exception
   {
      addChild(null, group, broadcast);
   }

   public void addChild(Group parent, Group child, boolean broadcast) throws Exception
   {

      GroupImpl childImpl = (GroupImpl)child;
      String groupId = "/" + child.getGroupName();
      Connection connection = eXoDS_.getConnection();
      childImpl.setParentId("/");
      DBObjectQuery<GroupImpl> query = new DBObjectQuery<GroupImpl>(GroupImpl.class);
      if (parent != null)
      {
         query.addEQ("GROUP_ID", parent.getId());
         Group parentGroup = super.loadUnique(connection, query.toQuery());
         if (parentGroup == null)
         {
            throw new InvalidNameException("Can't add group " + child.getId() + " since parent group " + parent.getId()
               + " not exists");
         }
         groupId = parentGroup.getId() + "/" + child.getGroupName();
         childImpl.setParentId(parentGroup.getId());
      }
      else if (child.getId() != null)
      {
         groupId = child.getId();
         childImpl.setParentId("/");
      }

      query.getParameters().clear();
      query.addEQ("GROUP_ID", groupId);
      Group o = super.loadUnique(connection, query.toQuery());
      if (o != null)
      {
         Object[] args = {child.getGroupName()};
         throw new UniqueObjectException("OrganizationService.unique-group-exception", args);
      }

      if (broadcast)
         listenerService_.broadcast("organization.group.preSave", this, childImpl);
      childImpl.setId(groupId);
      if (LOG.isDebugEnabled())
         LOG.debug("----------ADD GROUP " + child.getId() + " into Group" + child.getParentId());

      try
      {
         if (childImpl.getDBObjectId() == -1)
         {
            childImpl.setDBObjectId(eXoDS_.getIDGenerator().generateLongId(childImpl));
         }
         long id = childImpl.getDBObjectId();
         execute(connection, eXoDS_.getQueryBuilder().createInsertQuery(type_, id), childImpl);
         if (broadcast)
         {
            listenerService_.broadcast("organization.group.postSave", this, childImpl);
         }
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   public Group findGroupById(String groupId) throws Exception
   {
      DBObjectQuery<GroupImpl> query = new DBObjectQuery<GroupImpl>(GroupImpl.class);
      query.addEQ("GROUP_ID", groupId);
      Group g = super.loadUnique(query.toQuery());
      if (LOG.isDebugEnabled())
         LOG.debug("----------FIND GROUP BY ID: " + groupId + " _ " + (g != null));
      return g;
   }

   @SuppressWarnings("unchecked")
   public Collection<Group> findGroupByMembership(String userName, String membershipType) throws Exception
   {

      if (userName == null || membershipType == null)
         return null;
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      List<Membership> members = (List<Membership>)membershipHandler.findMembershipsByUser(userName);
      List<Group> groups = new ArrayList<Group>();
      for (Membership member : members)
      {
         if (!member.getMembershipType().equals(membershipType))
            continue;
         Group g = findGroupById(member.getGroupId());
         if (g != null)
            groups.add(g);
      }
      if (LOG.isDebugEnabled())
         LOG.debug("----------FIND GROUP BY USERNAME AND TYPE: " + userName + " - " + membershipType + " - ");
      return groups;
   }

   public Collection<GroupImpl> findGroups(Group parent) throws Exception
   {
      String parentId = "/";
      if (parent != null)
         parentId = parent.getId();
      DBObjectQuery<GroupImpl> query = new DBObjectQuery<GroupImpl>(GroupImpl.class);
      query.addEQ("PARENT_ID", parentId);
      DBPageList<GroupImpl> pageList = new DBPageList<GroupImpl>(20, this, query);
      if (LOG.isDebugEnabled())
      {
         LOG.debug("----------FIND GROUP BY PARENT: " + parent);
         LOG.debug(" Size = " + pageList.getAvailable());
      }
      return pageList.getAll();
   }

   @SuppressWarnings("unchecked")
   public Collection<Group> findGroupsOfUser(String user) throws Exception
   {
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      List<Membership> members = (List<Membership>)membershipHandler.findMembershipsByUser(user);
      List<Group> groups = new ArrayList<Group>();
      for (Membership member : members)
      {
         Group g = findGroupById(member.getGroupId());
         if (g != null && !hasGroup(groups, g))
            groups.add(g);
      }
      if (LOG.isDebugEnabled())
         LOG.debug("----------FIND GROUP BY USER: " + user + " - " + (groups != null));
      return groups;
   }

   private boolean hasGroup(List<Group> list, Group g)
   {
      for (Group ele : list)
      {
         if (ele.getId().endsWith(g.getId()))
            return true;
      }
      return false;
   }

   public Collection<GroupImpl> getAllGroups() throws Exception
   {
      DBObjectQuery<GroupImpl> query = new DBObjectQuery<GroupImpl>(GroupImpl.class);
      DBPageList<GroupImpl> pageList = new DBPageList<GroupImpl>(20, this, query);
      return pageList.getAll();
   }

   public void saveGroup(Group group, boolean broadcast) throws Exception
   {
      GroupImpl groupImpl = (GroupImpl)group;
      if (broadcast)
         listenerService_.broadcast(GroupHandler.PRE_UPDATE_GROUP_EVENT, this, groupImpl);
      super.update(groupImpl);
      if (broadcast)
         listenerService_.broadcast(GroupHandler.POST_UPDATE_GROUP_EVENT, this, groupImpl);
   }

   private boolean hasChildrenGroups(Group parent) throws Exception
   {
      String parentId = parent == null ? "/" : parent.getId();

      DBObjectQuery<GroupImpl> query = new DBObjectQuery<GroupImpl>(GroupImpl.class);
      query.addSelectCount("PARENT_ID");
      query.addEQ("PARENT_ID", parentId);
      DBPageList<GroupImpl> pageList = new DBPageList<GroupImpl>(20, this, query);

      return pageList.getTo() > 0;
   }

   public Group removeGroup(Group group, boolean broadcast) throws Exception
   {
      if (findGroupById(group.getId()) == null)
      {
         throw new InvalidNameException("Can't remove group since group with groupId " + group.getId()
            + " is not found");
      }

      GroupImpl groupImpl = (GroupImpl)group;

      if (hasChildrenGroups(group))
      {
         throw new IllegalStateException("Group " + group.getGroupName() + " has at least one child group");
      }

      if (broadcast)
         listenerService_.broadcast(GroupHandler.PRE_DELETE_GROUP_EVENT, this, groupImpl);
      super.remove(groupImpl);
      if (broadcast)
         listenerService_.broadcast(GroupHandler.POST_DELETE_GROUP_EVENT, this, groupImpl);
      return group;
   }

   public void addGroupEventListener(GroupEventListener listener)
   {
   }

   public void removeGroupEventListener(GroupEventListener listener)
   {
   }
}
