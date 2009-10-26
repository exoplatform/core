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
package org.exoplatform.services.security;

import java.util.Set;

/**
 * Created by The eXo Platform SAS .<br/> Strategy of extraction J2EE roles from
 * given group names
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */

public interface RolesExtractor
{

   /**
    * Extracts J2EE roles from userId and|or groups the user belongs to both
    * parameters may be null
    * 
    * @param userId
    * @param groups
    * @return
    */
   Set<String> extractRoles(String userId, Set<MembershipEntry> memberships);
}
