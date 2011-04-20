package org.exoplatform.services.security.j2ee;

import org.eclipse.jetty.plus.jaas.callback.ObjectCallback;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.services.security.jaas.DefaultLoginModule;
import org.exoplatform.services.security.jaas.RolePrincipal;
import org.exoplatform.services.security.jaas.UserPrincipal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

public class JettyLoginModule extends DefaultLoginModule
{
   /**
    * To retrieve an object instance containing needed password context.
    */
   private Callback[] objectCallback = {new ObjectCallback()};
   
   @Override
   public boolean commit() throws LoginException
   {

      if (super.commit())
      {

         Set<Principal> principals = subject.getPrincipals();

         for (String role : identity.getRoles())
            principals.add(new RolePrincipal(role));

         // username principal
         principals.add(new UserPrincipal(identity.getUserId()));

         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public boolean login() throws LoginException
   {
      if (log.isDebugEnabled())
      {
         log.debug("In login of JettyLoginModule.");
      }
      try
      {
         if (sharedState.containsKey("exo.security.identity"))
         {
            if (log.isDebugEnabled())
            {
               log.debug("Use Identity from previous LoginModule");
            }
            identity = (Identity)sharedState.get("exo.security.identity");
         }
         else
         {
            if (!digestAuthenticationIsUsed())
            {
               return super.login();
            }

            if (log.isDebugEnabled())
            {
               log.debug("Try create identity");
            }

            Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);

            if (authenticator == null)
            {
               throw new LoginException("No Authenticator component found, check your configuration");
            }

            String userId = authenticator.validateUser(getCredentials());

            identity = authenticator.createIdentity(userId);
            sharedState.put("javax.security.auth.login.name", userId);
            // TODO use PasswordCredential wrapper 
            subject.getPrivateCredentials().add(getPassword());
            subject.getPublicCredentials().add(getUsername());
         }
         return true;

      }
      catch (final Throwable e)
      {
         if (log.isDebugEnabled())
         {
            log.debug(e.getMessage(), e);
         }

         throw new LoginException(e.getMessage());
      }
   }

   /**
    * An utility method to handle object callback and also to checks if 
    * digest authentication is used during login operation.
    * @return true if digest authentication is used, otherwise - false
    * @throws IOException
    * @throws UnsupportedCallbackException
    */
   private boolean digestAuthenticationIsUsed() throws IOException, UnsupportedCallbackException
   {
      callbackHandler.handle(objectCallback);
      // in case we have a digest authentication
      // objectCallback should contain a structured instance
      // in case we have a basic authentication
      // objectCallback should contain only a string with a password
      return !(((ObjectCallback)objectCallback[0]).getObject() instanceof String);
   }

   /**
    * An utility method to get Credentials from object callback instance. 
    * It uses reflection mechanism to get access to Digest inner class of 
    * DigestAuthenticator, which is provided by object callback as it 
    * contains all needed information for password hashing.
    * @return Credential
    * @throws NoSuchFieldException 
    * @throws SecurityException 
    * @throws IllegalAccessException 
    * @throws IllegalArgumentException 
    * @throws Exception
    */
   private Credential[] getCredentials()
   {
      Map<String, String> passwordContext = new HashMap<String, String>();
      Set<String> contextElements = new HashSet<String>();
      // object to contain DigestAuthenticator$Digest instance to get
      // needed data from instance's fields
      Object objectFromCallback = ((ObjectCallback)objectCallback[0]).getObject();
      String username = null;
      String password = null;
      // to keep DigestAuthenticator$Digest representation
      Class<?> digestAuthenticatorClazz = DigestAuthenticator.class.getDeclaredClasses()[0];

      contextElements.add("cnonce");
      contextElements.add("method");
      contextElements.add("nc");
      contextElements.add("nonce");
      contextElements.add("qop");
      contextElements.add("realm");
      contextElements.add("uri");

      try
      {
         // here we're going to retrieve needed information from Digest class fields
         Iterator<String> elementIterator = contextElements.iterator();
         String element;
         Field field;
         while (elementIterator.hasNext())
         {
            element = elementIterator.next();
            field = digestAuthenticatorClazz.getDeclaredField(element);
            // need to set true as all needed fields are in private class, thus are private
            field.setAccessible(true);
            passwordContext.put(element, (String)field.get(objectFromCallback));
         }

         // get username
         field = digestAuthenticatorClazz.getDeclaredField("username");
         field.setAccessible(true);
         username = (String)field.get(objectFromCallback);

         // get password
         field = digestAuthenticatorClazz.getDeclaredField("response");
         field.setAccessible(true);
         password = (String)field.get(objectFromCallback);
      }
      catch (Exception e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Could not get credentials.", e);
         }
      }

      if (username == null || password == null)
      {
         return null;
      }

      return new Credential[]{new UsernameCredential(username), new PasswordCredential(password, passwordContext)};
   }

   private UsernameCredential getUsername()
   {
      String username = null;
      Class<?> digestAuthenticatorClazz = DigestAuthenticator.class.getDeclaredClasses()[0];
      try
      {
         Field field = digestAuthenticatorClazz.getDeclaredField("username");
         field.setAccessible(true);
         username = (String)field.get((((ObjectCallback)objectCallback[0]).getObject()));
      }
      catch (Exception e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Could not get username.", e);
         }
      }

      return new UsernameCredential(username);
   }

   private String getPassword() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException
   {
      String password = null;
      Class<?> digestAuthenticatorClazz = DigestAuthenticator.class.getDeclaredClasses()[0];
      try
      {
      Field field = digestAuthenticatorClazz.getDeclaredField("response");
      field.setAccessible(true);
      password = (String)field.get((((ObjectCallback)objectCallback[0]).getObject()));
      }
      catch (Exception e)
      {
         if (log.isErrorEnabled())
         {
            log.error("Could not get password.", e);
         }
      }

      return password;
   }

}
