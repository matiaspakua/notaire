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
            LOG.info("AdministradorSesion instancia creada - singleton inicializado");
        }
        return instancia;
    }

    public void sesionIniciada(DtoUsuario miDtoUsuario) {
        LOG.info("Sesion iniciada para usuario: " + miDtoUsuario.getNombre());
        this.setSesionUsuario(miDtoUsuario);
    }

    /**
     * Valida usuario contra el backend (POST /api/v1/usuarios/login).
     *
     * @param miDtoUsuario DTO con nombre y contrasenia.
     * @return DtoUsuario con valido=true y datos del usuario si es correcto;
     *         valido=false si no.
     */
    public DtoUsuario validarUsuario(DtoUsuario miDtoUsuario) {
        miDtoUsuario.setValido(false);
        LOG.info("Iniciando validacion de usuario contra API REST - usuario: " + miDtoUsuario.getNombre());
        try {
            ApiConfig.loadFromProperties();
            LOG.fine("Configuracion de API cargada correctamente - enviando POST /api/v1/usuarios/login");
            DtoUsuario response = RestClient.login(miDtoUsuario);
            if (response != null && response.isValido()) {
                miDtoUsuario = response;
                miDtoUsuario.setValido(true);
                LOG.info("Validacion de usuario exitosa - usuario: " + miDtoUsuario.getNombre()
                        + ", tipo: " + miDtoUsuario.getTipo()
                        + ", tiene_persona: " + (miDtoUsuario.getPersonas() != null));
            } else {
                LOG.warning(
                        "Validacion de usuario fallida - respuesta del API indica credenciales invalidas para usuario: "
                                + miDtoUsuario.getNombre());
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error de comunicacion con API REST al validar usuario: "
                    + miDtoUsuario.getNombre() + " - " + ex.getMessage(), ex);
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
        LOG.info("Sesion de usuario actualizada - usuario: "
                + (sesionUsuario != null ? sesionUsuario.getNombre() : "null"));
        this.sesionUsuario = sesionUsuario;
    }
}
