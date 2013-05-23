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

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;
import org.apache.mina.util.AvailablePortFinder;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * @author <a href="mailto:dmi3.kuleshov@gmail.com">Dmitry Kuleshov</a>
 */
public class DummyLDAPServiceImpl implements LDAPService, Startable
{
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.ldap.LDAPServiceImpl");

   private Map<String, Object> env = new HashMap<String, Object>();

   private int port = -1;

   /** The directory service */
   private DirectoryService service;

   /** The LDAP server */
   private LdapServer server;

   public DummyLDAPServiceImpl() throws Exception
   {
      File workingDirectory = new File("target/working-server");
      workingDirectory.mkdirs();

      doDelete(workingDirectory);

      // Initialize the LDAP service
      service = new DefaultDirectoryService();
      service.setWorkingDirectory(workingDirectory);

      // first load the schema
      initSchemaPartition();

      // then the system partition
      // this is a MANDATORY partition
      Partition systemPartition = addPartition( "system", ServerDNConstants.SYSTEM_DN );
      service.setSystemPartition( systemPartition );

      // Disable the ChangeLog system
      service.getChangeLog().setEnabled( false );

      // Create a new partition
      Partition partition = addPartition("eXoTestPartition", "dc=exoplatform,dc=org");

      // Index some attributes on the partition
      addIndex(partition, "objectClass", "ou", "uid" );

      service.setShutdownHookEnabled(false);

      service.startup();

      // Inject the eXo root entry if it does not already exist
      if ( !service.getAdminSession().exists( partition.getSuffixDn() ) )
      {
          DN dnExo = new DN( "dc=exoplatform,dc=org" );
          ServerEntry entryExo = service.newEntry( dnExo );
          entryExo.add( "objectClass", "top", "domain", "extensibleObject" );
          entryExo.add( "dc", "exoplatform" );
          service.getAdminSession().add( entryExo );
      }

      port = AvailablePortFinder.getNextAvailable(1024);
      server = new LdapServer();
      server.setTransports( new TcpTransport(port));
      server.setDirectoryService(service);
      server.start();

      // server launched and configured

      // configuration of client side
      env.put(DirectoryService.JNDI_KEY, service);
      env.put(Context.PROVIDER_URL, "");
      env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
      env.put(Context.SECURITY_CREDENTIALS, "secret");
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());

      // Add the new schema needed for COR-293
      addNewSchema();
   }

   public LdapContext getLdapContext() throws NamingException
   {
      return new DummyLdapContext(new InitialLdapContext(new Hashtable<String, Object>(env), null));
   }

   public LdapContext getLdapContext(boolean renew) throws NamingException
   {
      return getLdapContext();
   }

   public void release(LdapContext ctx) throws NamingException
   {
      try
      {
         if (ctx != null)
         {
            ctx.close();
         }
      }
      catch (NamingException e)
      {
         LOG.warn("Exception occurred when tried to close context", e);
      }
   }

   public InitialContext getInitialContext() throws NamingException
   {
      Hashtable<String, Object> props = new Hashtable<String, Object>(env);
      props.put(Context.OBJECT_FACTORIES, "com.sun.jndi.ldap.obj.LdapGroupFactory");
      props.put(Context.STATE_FACTORIES, "com.sun.jndi.ldap.obj.LdapGroupFactory");
      return new DummyLdapContext(new InitialLdapContext(props, null));
   }

   public boolean authenticate(String userDN, String password) throws NamingException
   {
      Hashtable<String, Object> props = new Hashtable<String, Object>(env);
      props.put(Context.SECURITY_AUTHENTICATION, "simple");
      props.put(Context.SECURITY_PRINCIPAL, userDN);
      props.put(Context.SECURITY_CREDENTIALS, password);
      props.put("com.sun.jndi.ldap.connect.pool", "false");

      InitialContext ctx = null;
      try
      {
         ctx = new DummyLdapContext(new InitialLdapContext(props, null));
         return true;
      }
      catch (NamingException e)
      {
         LOG.debug("Error during initialization LDAP Context", e);
         return false;
      }
      finally
      {
         closeContext(ctx);
      }
   }

   public int getServerType()
   {
      return 0;
   }

   private void closeContext(Context ctx)
   {
      try
      {
         if (ctx != null)
         {
            ctx.close();
         }
      }
      catch (NamingException e)
      {
         LOG.warn("Exception occurred when tried to close context", e);
      }
   }

   protected void doDelete(File wkdir) throws IOException
   {
      if (wkdir.exists())
      {
         FileUtils.deleteDirectory(wkdir);
      }
      if (wkdir.exists())
      {
         throw new IOException("Failed to delete: " + wkdir);
      }
   }

   /**
    * Add a new partition to the server
    *
    * @param partitionId The partition Id
    * @param partitionDn The partition DN
    * @return The newly added partition
    * @throws Exception If the partition can't be added
    */
   private Partition addPartition( String partitionId, String partitionDn ) throws Exception
   {
       // Create a new partition named 'foo'.
       JdbmPartition partition = new JdbmPartition();
       partition.setId( partitionId );
       partition.setPartitionDir( new File( service.getWorkingDirectory(), partitionId ) );
       partition.setSuffix( partitionDn );
       service.addPartition( partition );

       return partition;
   }

   /**
    * Add a new set of index on the given attributes
    *
    * @param partition The partition on which we want to add index
    * @param attrs The list of attributes to index
    */
   private void addIndex( Partition partition, String... attrs )
   {
       // Index some attributes on the apache partition
       HashSet<Index<?, ServerEntry, Long>> indexedAttributes = new HashSet<Index<?, ServerEntry, Long>>();

       for ( String attribute : attrs )
       {
           indexedAttributes.add( new JdbmIndex<String, ServerEntry>( attribute ) );
       }

       ( ( JdbmPartition ) partition ).setIndexedAttributes( indexedAttributes );
   }

   /**
    * initialize the schema manager and add the schema partition to directory service
    *
    * @throws Exception if the schema LDIF files are not found on the classpath
    */
   private void initSchemaPartition() throws Exception
   {
       SchemaPartition schemaPartition = service.getSchemaService().getSchemaPartition();

       // Init the LdifPartition
       LdifPartition ldifPartition = new LdifPartition();
       String workingDirectory = service.getWorkingDirectory().getPath();
       ldifPartition.setWorkingDirectory( workingDirectory + "/schema" );

       // Extract the schema on disk (a brand new one) and load the registries
       File schemaRepository = new File( workingDirectory, "schema" );
       SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( new File( workingDirectory ) );
       extractor.extractOrCopy( true );

       schemaPartition.setWrappedPartition( ldifPartition );

       SchemaLoader loader = new LdifSchemaLoader( schemaRepository );
       SchemaManager schemaManager = new DefaultSchemaManager( loader );

       service.setSchemaManager( schemaManager );

       // We have to load the schema now, otherwise we won't be able
       // to initialize the Partitions, as we won't be able to parse 
       // and normalize their suffix DN
       schemaManager.loadAllEnabled();

       schemaPartition.setSchemaManager( schemaManager );

       List<Throwable> errors = schemaManager.getErrors();

       if ( errors.size() != 0 )
       {
           throw new Exception( "Schema load failed : " + errors );
       }
   }

   private void addNewSchema() throws NamingException
   {
      DirContext ctx = getLdapContext();
      try
      {
         Attributes atAttrs = new BasicAttributes(true);
         atAttrs.put("attributeTypes",
            "( 1.2.840.113556.1.4.8 NAME 'userAccountControl' DESC 'Flags that control the behavior of the user account' EQUALITY integerMatch SYNTAX '1.3.6.1.4.1.1466.115.121.1.27' SINGLE-VALUE )");
         ctx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, atAttrs);
         Attributes ocAttrs = new BasicAttributes(true);
         ocAttrs.put("objectClasses",
            "( 1.2.840.113556.1.5.9 NAME 'user' SUP inetOrgPerson STRUCTURAL MAY (userAccountControl) )");
         ctx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, ocAttrs);
      }
      finally
      {
         ctx.close();
      }
   }

   /**
    * @see org.picocontainer.Startable#start()
    */
   public void start()
   {
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   public void stop()
   {
      server.stop();
      try
      {
         service.shutdown();
      }
      catch (Exception e)
      {
         // ignore it
      }
   }
}
