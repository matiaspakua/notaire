package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.User;

public class AdministradorSession
{

    private static AdministradorSession instancia = null;
    private DtoUser userSession;

    private AdministradorSession()
    {
    }

    public static AdministradorSession getInstancia()
    {
        if (instancia == null)
        {
            instancia = new AdministradorSession();
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
