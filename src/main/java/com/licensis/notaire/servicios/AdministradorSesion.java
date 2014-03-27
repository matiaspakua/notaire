/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.servicios;

import com.licensis.notaire.dto.DtoUsuario;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.licensis.notaire.jpa.ConstantesPersistencia;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.negocio.Usuario;

/**
 * Clase que se encarga de mantener datos e informacion correspondiente al usuario y sesion activas
 * actualmente, junto con el conjunto de metodos necesarios para poder acceder a dicha informacion.
 *
 * @author matias
 */
public class AdministradorSesion
{

    private static AdministradorSesion instancia = null;
    private DtoUsuario usuarioLogueado = null;
    private DtoUsuario sesionUsuario;

    /**
     * Constructor sin argumentos para AdministradorSesion.
     */
    private AdministradorSesion() {
    }

    /**
     * Get estatico para obtener la instancia actual del administrador de sesion (singleton).
     *
     * @return
     */
    public static AdministradorSesion getInstancia() {
        if (instancia == null)
        {
            instancia = new AdministradorSesion();
        }
        return instancia;
    }

    /**
     * Metodo que asigna el usuario recien loguado. Este metodo permite, al administrado de sesion,
     * mantener la referencia hacia el usuario que se acaba de loguear.
     *
     * @param miDtoUsuario El nuevo usuario logueado.
     */
    public void sesionIniciada(DtoUsuario miDtoUsuario) {

        //DtoUsuario miUsuario = new DtoUsuario();

        this.setSesionUsuario(miDtoUsuario);
    }

    /**
     * Metodo que permite validar un usuario que intenta loguarse al sistema. Para la validacion, se
     * utiliza el nombre de usuario y contraseña indicados en la pantalla de login, luego se intenta
     * obtener una instancia de usuario (almacenado en la base de datos) y finalmente se verifica,
     * que en caso de existir la instancia de usuario, el nombre y la contraseña coincidan. </p> Por
     * otro lado, cabe mencionar que la contraseña es encripada (y almacenada de esa forma) en la
     * base de datos.
     *
     * @param miDtoUsuario Un DTO usuario para ser validado.
     * @return Un DTO usuario con el atributo esValido en verdadero si el usuario fue validado
     * correctamente, falso en caso contrario.
     */
    public DtoUsuario validarUsuario(DtoUsuario miDtoUsuario) {

        ArrayList<Usuario> listaUsuarios = new ArrayList<>();
        Boolean flag = false; //se utiliza para saber si existe el usuario
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
            }
            else
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

    /**
     * Metodo que permite obtener la instancia (un DTO) del usuario loguado en el sistema
     * actualmente (junto con la referencia hacia la instancia de Persona correspondiente).
     *
     * @return miUsuario Un DTO usuario que representa el usuario loguado actualmente.
     */
    public Usuario getSesionUsuario() {
        Usuario miUsuario = new Usuario();
        
        if (this.sesionUsuario != null)
        {        
            miUsuario.setAtributos(sesionUsuario);
        }
        return miUsuario;
    }

    public void setSesionUsuario(DtoUsuario sesionUsuario) {
        this.sesionUsuario = sesionUsuario;
    }
}
