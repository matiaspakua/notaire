package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.User;

public class SessionAdministrator
{

    private static SessionAdministrator instancia = null;
    private DtoUser userSession;

    private SessionAdministrator()
    {
    }

    public static SessionAdministrator getInstancia()
    {
        if (instancia == null)
        {
            instancia = new SessionAdministrator();
        }
        return instancia;
    }

    public User getUserSession()
    {
        User miUser = new User();

        if (this.userSession != null)
        {
            miUser.setAtributos(userSession);
        }
        return miUser;
    }

    public void setUserSession(DtoUser userSession)
    {
        this.userSession = userSession;
    }
}
