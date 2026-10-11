package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.User;

public class SessionAdministrator
{

    private static SessionAdministrator instance = null;
    private DtoUser userSession;

    private SessionAdministrator()
    {
    }

    public static SessionAdministrator getInstance()
    {
        if (instance == null)
        {
            instance = new SessionAdministrator();
        }
        return instance;
    }

    public User getUserSession()
    {
        User user = new User();

        if (this.userSession != null)
        {
            user.setAttributes(userSession);
        }
        return user;
    }

    public void setUserSession(DtoUser userSession)
    {
        this.userSession = userSession;
    }
}
