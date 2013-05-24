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
package org.exoplatform.services.organization;

/**
 * Created by The eXo Platform SAS<br>
 * If the other service or a third  party want to customize the initialization phase of the organization service.
 * they should make a customize class that implements this interface and
 * register their plugin code via xml configuration. An example of the
 * customization code is we have an organization service database intializer
 * that create the predifined the user , group and membership if the database is
 * empty. To Register the plugin code by the xml configuration: You need to
 * create a my.package.MyMembershipEventListener that implements this interface
 * and add a conf/portal/configuration.xml to the classpath. The
 * configuration.xml can be in a jar file. The file should contain the following
 * configuraiton:
 * 
 * <pre>
 * &lt;configuration&gt;
 *   [..]
 *   &lt;external-component-plugins&gt;
 *     &lt;target-component&gt;org.exoplatform.services.organization.OrganizationService&lt;/target-component&gt;
 *     &lt;component-plugin&gt;
 *        &lt;name&gt;my.customize.initializer.plugin&lt;/name&gt;
 *        &lt;set-method&gt;addListenerPlugin&lt;/set-method&gt;
 *        &lt;type&gt;my.package.MyInitializerPlugin&lt;/type&gt;
 *        &lt;description&gt;your listener description&lt;/description&gt;
 *      &lt;/component-plugin&gt;
 *  &lt;/external-component-plugins&gt;
 *  [...]
 * /configuration&gt;
 * </pre>
 * @author <a href="mailto:tuan08@users.sourceforge.net">Tuan Nguyen</a>
 * @LevelAPI Platform
 */
public interface OrganizationServiceInitializer
{
   /**
    *  The Organization Service Initializer to create users, groups and membership types.
    *
    * @param service OrganizationService is the service that allows to access the Organization model.
    */
   public void init(OrganizationService service) throws Exception;
}
