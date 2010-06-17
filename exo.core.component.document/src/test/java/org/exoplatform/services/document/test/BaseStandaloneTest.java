/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.document.test;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.test.BasicTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: BaseStandaloneTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class BaseStandaloneTest extends BasicTestCase
{

   public PortalContainer pcontainer;

   protected MimeTypeResolver mimetypeResolver = new MimeTypeResolver();

   public void setUp() throws Exception
   {
      super.setUp();
      pcontainer = PortalContainer.getInstance();
   }

   public Object getComponentInstanceOfType(Class componentType)
   {
      return pcontainer.getComponentInstanceOfType(componentType);
   }

   /**
    * Its a wrapper to cheat security.
    */
   public File createTempFile(String prefix, String suffix) throws IOException
   {
      return File.createTempFile(prefix, suffix);
   }

   /**
    * Its a wrapper to cheat security.
    */
   public boolean createNewFile(File f) throws IOException
   {
      return f.createNewFile();
   }

   /**
    * Its a wrapper to cheat security.
    */
   public InputStream getInputStream(File f) throws IOException
   {
      return new FileInputStream(f);
   }

   /**
    * Its a wrapper to cheat security.
    */
   public boolean deleteFile(File f) throws IOException
   {
      return f.delete();
   }

}
