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
package org.exoplatform.services.xml.transform;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.xml.BaseTest;
import org.exoplatform.services.xml.transform.trax.TRAXTemplatesService;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class TestTemplates extends BaseTest
{

   private TRAXTemplatesService traxTemplatesService;

   public void setUp() throws Exception
   {
      StandaloneContainer.setConfigurationPath(Thread.currentThread().getContextClassLoader().getResource(
         "conf/standalone/test-configuration.xml").getPath());
      StandaloneContainer container = StandaloneContainer.getInstance();
      traxTemplatesService = (TRAXTemplatesService)container.getComponentInstanceOfType(TRAXTemplatesService.class);
      assertNotNull("traxTemplatesService", traxTemplatesService);
   }

   public void testTemplates()
   {
      assertNotNull(traxTemplatesService.getTemplates("xslt1"));
   }

}
