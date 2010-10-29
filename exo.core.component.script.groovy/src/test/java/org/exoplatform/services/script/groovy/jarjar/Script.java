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
package org.exoplatform.services.script.groovy.jarjar;

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.codehaus.groovy.control.CompilationFailedException;
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.SecurityHelper;

import java.io.IOException;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Script
{

   /** . */
   private final String name;

   public Script(String name)
   {
      this.name = name;
   }

   public Object execute(Mapping mapping)
   {

      //
      final JarJarClassLoader loader =
         JarJarClassLoader.createJarJarClassLoaderInPrivilegedMode(Thread.currentThread().getContextClassLoader());

      //
      mapping.configure(loader);

      // Obtain script class
      URL url = PrivilegedSystemHelper.getResource("jarjar/" + name);
      Assert.assertNotNull(url);
      GroovyCodeSource gcs;
      try
      {
         gcs = new GroovyCodeSource(url);
      }
      catch (IOException e)
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      }

      Class testClass;
      try
      {
         final GroovyCodeSource gGcs = gcs;
         testClass = SecurityHelper.doPriviledgedExceptionAction(new PrivilegedExceptionAction<Class>()
         {
            public Class run() throws Exception
            {
               return loader.parseClass(gGcs);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof CompilationFailedException)
         {
            throw (CompilationFailedException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }

      // Instantiate script
      GroovyObject testObject;
      try
      {
         testObject = (GroovyObject)testClass.newInstance();
      }
      catch (Exception e)
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      }

      // Invoke finally
      return testObject.invokeMethod("run", new Object[0]);
   }
}
