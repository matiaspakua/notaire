package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.Usuario;

public class AdministradorSesion
{

    private static AdministradorSesion instancia = null;
    private DtoUsuario sesionUsuario;

    private AdministradorSesion()
    {
    }

    public static AdministradorSesion getInstancia()
    {
        if (instancia == null)
        {
            instancia = new AdministradorSesion();
        }
        return instancia;
    }

    public Usuario getSesionUsuario()
    {
        Usuario miUsuario = new Usuario();

        if (this.sesionUsuario != null)
        {
            miUsuario.setAtributos(sesionUsuario);
        }
        return miUsuario;
    }

    public void setSesionUsuario(DtoUsuario sesionUsuario)
    {
        this.sesionUsuario = sesionUsuario;
    }
}
