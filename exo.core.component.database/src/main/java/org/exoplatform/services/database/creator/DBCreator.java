/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.database.creator;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: $
 */
public interface DBCreator
{

   /**
    * Database template.
    */
   public static final String DATABASE_TEMPLATE = "${database}";

   /**
    * User name template.
    */
   public static final String USERNAME_TEMPLATE = "${username}";

   /**
    * Password template.
    */
   public static final String PASSWORD_TEMPLATE = "${password}";

   /**
    * Create database using predefined SQL DDL script for new user which are given via configuration.
    * New database name, user name and password defined in script via templates as ${database}, ${username}
    * and ${password} respectively.
    * 
    * @param dbName
    *          new database name
    * @throws DBCreationException
    *          if any error occurs 
    */
   void create(String dbName) throws DBCreationException;
}
