package org.exoplatform.services.organization.hibernate;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.impl.UserProfileDataHsql;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.services.security.PermissionConstants;
import org.hibernate.Session;

import javax.naming.InvalidNameException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:obouras@exoplatform.com">Omar Bouras</a>
 * @version ${Revision}
 * @date 02/10/15
 */
public class UserProfileDAOHsqlImpl implements UserProfileHandler, UserProfileEventListenerHandler {

    static private UserProfile NOT_FOUND = new UserProfileImpl();

    private static final String queryFindUserProfileByName =
            "from u in class org.exoplatform.services.organization.impl.UserProfileDataHsql where u.userName = :id";

    private static final String queryFindUserProfiles =
            "from u in class org.exoplatform.services.organization.impl.UserProfileDataHsql";

    private HibernateService service_;

    private ExoCache<Serializable, Object> cache_;

    private List<UserProfileEventListener> listeners_;

    private UserHandler userDAO;

    public UserProfileDAOHsqlImpl(HibernateService service, CacheService cservice, UserHandler userDAO) throws Exception
    {
        service_ = service;
        cache_ = cservice.getCacheInstance(getClass().getName());
        listeners_ = new ArrayList<UserProfileEventListener>(3);
        this.userDAO = userDAO;
    }

    /**
     * {@inheritDoc}
     */
    public void addUserProfileEventListener(UserProfileEventListener listener)
    {
        SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
        listeners_.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeUserProfileEventListener(UserProfileEventListener listener)
    {
        SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
        listeners_.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    final public UserProfile createUserProfileInstance()
    {
        return new UserProfileImpl();
    }

    /**
     * {@inheritDoc}
     */
    public UserProfile createUserProfileInstance(String userName)
    {
        return new UserProfileImpl(userName);
    }

    /**
     * {@inheritDoc}
     */
    public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception
    {
        String userName = profile.getUserName();
        Session session = service_.openSession();
        UserProfileDataHsql upd = (UserProfileDataHsql)service_.findOne(session, queryFindUserProfileByName, userName);

        User user = userDAO.findUserByName(userName);
        if (user == null)
        {
            throw new InvalidNameException("User " + userName + " not exists");
        }

        if (upd == null)
        {
            upd = new UserProfileDataHsql();
            upd.setUserProfile(profile);
            if (broadcast)
                preSave(profile, true);

            session.save(userName, upd);
            session.flush();
            cache_.put(userName, profile);

            if (broadcast)
                postSave(profile, true);
        }
        else
        {
            upd.setUserProfile(profile);
            if (broadcast)
                preSave(profile, false);

            session.update(upd);
            session.flush();
            cache_.put(userName, profile);

            if (broadcast)
                postSave(profile, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception
    {
        Session session = service_.openSession();
        try
        {
            UserProfileDataHsql upd = (UserProfileDataHsql)service_.findExactOne(session, queryFindUserProfileByName, userName);
            UserProfile profile = upd.getUserProfile();
            if (broadcast)
                preDelete(profile);

            session.delete(upd);
            session.flush();
            cache_.remove(userName);

            if (broadcast)
                postDelete(profile);

            return profile;
        }
        catch (Exception exp)
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public UserProfile findUserProfileByName(String userName) throws Exception
    {
        UserProfile up = (UserProfile)cache_.get(userName);
        if (up != null)
        {
            if (NOT_FOUND == up) //NOSONAR
            {
                return null;
            }
            return up;
        }
        Session session = service_.openSession();
        up = findUserProfileByName(userName, session);
        if (up != null)
            cache_.put(userName, up);
        else
            cache_.put(userName, NOT_FOUND);
        return up;
    }

    /**
     * {@inheritDoc}
     */
    public UserProfile findUserProfileByName(String userName, Session session) throws Exception
    {
        UserProfileDataHsql upd = (UserProfileDataHsql)service_.findOne(session, queryFindUserProfileByName, userName);
        if (upd != null)
        {
            return upd.getUserProfile();
        }
        return null;
    }

    void removeUserProfileEntry(String userName, Session session) throws Exception
    {
        Object user = session.createQuery(queryFindUserProfileByName).setString("id", userName).uniqueResult();
        if (user != null)
        {
            session.delete(user);
            cache_.remove(userName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<?> findUserProfiles() throws Exception
    {
        Session session = service_.openSession();
        return service_.findAll(session, queryFindUserProfiles);
    }

    private void preSave(UserProfile profile, boolean isNew) throws Exception
    {
        for (UserProfileEventListener listener : listeners_)
            listener.preSave(profile, isNew);
    }

    private void postSave(UserProfile profile, boolean isNew) throws Exception
    {
        for (UserProfileEventListener listener : listeners_)
            listener.postSave(profile, isNew);
    }

    private void preDelete(UserProfile profile) throws Exception
    {
        for (UserProfileEventListener listener : listeners_)
            listener.preDelete(profile);
    }

    private void postDelete(UserProfile profile) throws Exception
    {
        for (UserProfileEventListener listener : listeners_)
            listener.postDelete(profile);
    }

    /**
     * {@inheritDoc}
     */
    public List<UserProfileEventListener> getUserProfileListeners()
    {
        return Collections.unmodifiableList(listeners_);
    }

}
