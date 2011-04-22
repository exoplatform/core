package org.exoplatform.services.security.j2ee;

import org.exoplatform.services.security.jaas.DefaultLoginModule;
import org.exoplatform.services.security.jaas.RolePrincipal;
import org.exoplatform.services.security.jaas.UserPrincipal;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.login.LoginException;

public class JettyLoginModule extends DefaultLoginModule
{
   
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

}
