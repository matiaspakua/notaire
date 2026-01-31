package com.licensis.notaire.servicios;

import com.licensis.notaire.api.client.ApiConfig;
import com.licensis.notaire.api.client.RestClient;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.Usuario;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que se encarga de mantener datos e informacion correspondiente al
 * usuario y sesion activas actualmente.
 * Usa REST API (POST /usuarios/login) para validar usuario.
 *
 * @author matias
 */
public class AdministradorSesion {

    private static AdministradorSesion instancia = null;
    private DtoUsuario sesionUsuario;
    private static final Logger LOG = Logger.getLogger(AdministradorSesion.class.getName());

    private AdministradorSesion() {
    }

    public static AdministradorSesion getInstancia() {
        if (instancia == null) {
            instancia = new AdministradorSesion();
        }
        return instancia;
    }

    public void sesionIniciada(DtoUsuario miDtoUsuario) {
        this.setSesionUsuario(miDtoUsuario);
    }

    /**
     * Valida usuario contra el backend (POST /api/v1/usuarios/login).
     *
     * @param miDtoUsuario DTO con nombre y contrasenia.
     * @return DtoUsuario con valido=true y datos del usuario si es correcto; valido=false si no.
     */
    public DtoUsuario validarUsuario(DtoUsuario miDtoUsuario) {
        miDtoUsuario.setValido(false);
        try {
            ApiConfig.loadFromProperties();
            DtoUsuario response = RestClient.login(miDtoUsuario);
            if (response != null && response.isValido()) {
                miDtoUsuario = response;
                miDtoUsuario.setValido(true);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error al validar usuario desde API REST", ex);
        }
        return miDtoUsuario;
    }

    /**
     * Metodo que permite obtener la instancia (un DTO) del usuario logueado en el
     * sistema actualmente (junto con la referencia hacia la instancia de Persona
     * correspondiente).
     *
     * @return miUsuario Un objeto Usuario que representa el usuario logueado
     *         actualmente.
     */
    public Usuario getSesionUsuario() {
        Usuario miUsuario = new Usuario();

        if (this.sesionUsuario != null) {
            miUsuario.setAtributos(sesionUsuario);
        }
        return miUsuario;
    }

    public void setSesionUsuario(DtoUsuario sesionUsuario) {
        this.sesionUsuario = sesionUsuario;
    }
}
