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
package org.exoplatform.services.organization.ldap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.exoplatform.services.organization.CacheHandler;
import org.exoplatform.services.organization.hibernate.UserProfileDAOImpl;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 14, 2005. @version andrew00x $
 */
public class OrganizationServiceImpl extends BaseOrganizationService
{

   /**
    * @param params see {@link InitParams}
    * @param ldapService see {@link LDAPService}
    * @param hservice see {@link HibernateService}
    * @param cservice see {@link CacheService}
    * @throws Exception if any errors occurs
    */
   public OrganizationServiceImpl(InitParams params, LDAPService ldapService, HibernateService hservice,
      CacheService cservice) throws Exception
   {

      LDAPAttributeMapping ldapAttrMapping =
         (LDAPAttributeMapping)params.getObjectParam("ldap.attribute.mapping").getObject();

      CacheHandler cacheHandler = new CacheHandler(cservice);

      if (ldapService.getServerType() == LDAPService.ACTIVE_DIRECTORY_SERVER)
      {
         userDAO_ = new ADUserDAOImpl(ldapAttrMapping, ldapService, cacheHandler);
         //      ADSearchBySID adSearch = new ADSearchBySID(ldapAttrMapping, ldapService);
         ADSearchBySID adSearch = new ADSearchBySID(ldapAttrMapping);
         groupDAO_ = new ADGroupDAOImpl(ldapAttrMapping, ldapService, adSearch, cacheHandler);
         membershipDAO_ = new ADMembershipDAOImpl(ldapAttrMapping, ldapService, adSearch, this, cacheHandler);
      }
      else
      {
         //      ValueParam param = params.getValueParam("ldap.userDN.key");
         //      ldapAttrMapping.userDNKey = param.getValue();
         userDAO_ = new UserDAOImpl(ldapAttrMapping, ldapService, cacheHandler, this);
         groupDAO_ = new GroupDAOImpl(ldapAttrMapping, ldapService, cacheHandler);
         membershipDAO_ = new MembershipDAOImpl(ldapAttrMapping, ldapService, this, cacheHandler);
      }
      // userProfileHandler_ = new UserProfileHandlerImpl(ldapAttrMapping, ldapService) ;
      userProfileDAO_ = new UserProfileDAOImpl(hservice, cservice, userDAO_);
      membershipTypeDAO_ = new MembershipTypeDAOImpl(ldapAttrMapping, ldapService, cacheHandler);

      ValueParam param = params.getValueParam("ldap.userDN.key");
      if (param != null)
         ldapAttrMapping.userDNKey = param.getValue();

      param = params.getValueParam("ldap.groupDN.key");
      if (param != null)
         ldapAttrMapping.groupDNKey = param.getValue();
   }

}
