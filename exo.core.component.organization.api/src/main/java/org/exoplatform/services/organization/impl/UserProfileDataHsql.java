package org.exoplatform.services.organization.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.exoplatform.commons.utils.SecurityHelper;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:obouras@exoplatform.com">Omar Bouras</a>
 * @version ${Revision}
 * @date 02/10/15
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "EXO_USER_PROFILE")
public class UserProfileDataHsql
{
    static transient private XStream xstream_;

    @Id
    private String userName;

    @Column(length = 65536)
    @Type(type = "org.exoplatform.services.database.impl.TextType")
    private String profile;

    public UserProfileDataHsql()
    {
    }

    public UserProfileDataHsql(String userName)
    {
        StringBuffer b = new StringBuffer();
        b.append("<user-profile>\n").append("  <userName>").append(userName).append("</userName>\n");
        b.append("</user-profile>\n");
        this.userName = userName;
        this.profile = b.toString();
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String s)
    {
        this.userName = s;
    }

    public String getProfile()
    {
        return profile;
    }

    public void setProfile(String s)
    {
        profile = s;
    }

    public org.exoplatform.services.organization.UserProfile getUserProfile()
    {
        final XStream xstream = getXStream();
        UserProfileImpl up = SecurityHelper.doPrivilegedAction(new PrivilegedAction<UserProfileImpl>() {
            public UserProfileImpl run() {
                return (UserProfileImpl) xstream.fromXML(profile);
            }
        });
        return up;
    }

    public void setUserProfile(org.exoplatform.services.organization.UserProfile up)
    {
        if (up == null)
        {
            profile = "";
            return;
        }
        final UserProfileImpl impl = (UserProfileImpl)up;
        userName = up.getUserName();
        final XStream xstream = getXStream();
        profile = SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
        {
            public String run()
            {
                return xstream.toXML(impl);
            }
        });
    }

    static private XStream getXStream()
    {
        if (xstream_ == null)
        {
            xstream_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<XStream>()
            {
                public XStream run()
                {
                    return new XStream(new XppDriver());
                }
            });
            xstream_.alias("user-profile", UserProfileImpl.class);
        }
        return xstream_;
    }
}

