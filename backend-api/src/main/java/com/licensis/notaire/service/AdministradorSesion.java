package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoUsuario;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.negocio.Usuario;

public class AdministradorSesion
{

    private static AdministradorSesion instancia = null;
    private DtoUsuario usuarioLogueado = null;
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

    public void sesionIniciada(DtoUsuario miDtoUsuario)
    {
        this.setSesionUsuario(miDtoUsuario);
    }

    public DtoUsuario validarUsuario(DtoUsuario miDtoUsuario)
    {

        ArrayList<Usuario> listaUsuarios = new ArrayList<>();
        Boolean flag = false;
        String passWordUsuario;
        String passWordIngresado;

        miDtoUsuario.setValido(false);

        try
        {
            IPersistenciaJpa miJpaUsuario = AdministradorJpa.getInstancia().obtenerJpa(UsuarioJpaController.class.getName());

            listaUsuarios = (ArrayList<Usuario>) ((UsuarioJpaController) miJpaUsuario).buscarUsuarios();

            if (!listaUsuarios.isEmpty() && (listaUsuarios != null))
            {
                for (int i = 0; i < listaUsuarios.size(); i++)
                {
                    if (listaUsuarios.get(i).getNombre().equals(miDtoUsuario.getNombre()))
                    {

                        passWordUsuario = listaUsuarios.get(i).getContrasenia();
                        passWordIngresado = ControllerNegocio.getInstancia().encriptaEnMD5(miDtoUsuario.getContrasenia());

                        if (passWordUsuario.equals(passWordIngresado) && listaUsuarios.get(i).getEstado())
                        {
                            flag = true;
                            miDtoUsuario = listaUsuarios.get(i).getDto();
                            miDtoUsuario.setValido(true);
                            i = listaUsuarios.size();
                            break;
                        }

                    }
                }
            } else
            {
                throw new NullPointerException("La lista de usuarios es nula");
            }

        }
        catch (NonexistentJpaException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return miDtoUsuario;

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
