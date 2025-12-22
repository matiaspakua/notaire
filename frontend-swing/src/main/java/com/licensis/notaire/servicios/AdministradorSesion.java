package com.licensis.notaire.servicios;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.negocio.Usuario;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que se encarga de mantener datos e informacion correspondiente al
 * usuario y sesion activas actualmente, junto con el conjunto de metodos necesarios 
 * para poder acceder a dicha informacion.
 * 
 * Migrado a usar REST API en lugar de JPA directo.
 *
 * @author matias
 */
public class AdministradorSesion {

    private static AdministradorSesion instancia = null;
    private DtoUsuario usuarioLogueado = null;
    private DtoUsuario sesionUsuario;

    /**
     * Constructor sin argumentos para AdministradorSesion.
     */
    private AdministradorSesion() {
    }

    /**
     * Get estatico para obtener la instancia actual del administrador de sesion
     * (singleton).
     *
     * @return instancia de AdministradorSesion
     */
    public static AdministradorSesion getInstancia() {
        if (instancia == null) {
            instancia = new AdministradorSesion();
        }
        return instancia;
    }

    /**
     * Metodo que asigna el usuario recien logueado. Este metodo permite, al
     * administrado de sesion, mantener la referencia hacia el usuario que se acaba de loguear.
     *
     * @param miDtoUsuario El nuevo usuario logueado.
     */
    public void sesionIniciada(DtoUsuario miDtoUsuario) {
        this.setSesionUsuario(miDtoUsuario);
    }

    /**
     * Metodo que permite validar un usuario que intenta loguearse al sistema. Para
     * la validacion, se utiliza el nombre de usuario y contraseña indicados en la pantalla de login,
     * luego se intenta obtener una instancia de usuario (almacenado en la base de datos) y
     * finalmente se verifica, que en caso de existir la instancia de usuario, el nombre y la contraseña
     * coincidan.
     * 
     * La contraseña es encripada (y almacenada de esa forma) en la base de datos.
     *
     * @param miDtoUsuario Un DTO usuario para ser validado.
     * @return Un DTO usuario con el atributo esValido en verdadero si el usuario
     *         fue validado correctamente, falso en caso contrario.
     */
    public DtoUsuario validarUsuario(DtoUsuario miDtoUsuario) {
        Boolean flag = false; // se utiliza para saber si existe el usuario
        String passWordUsuario;
        String passWordIngresado;

        miDtoUsuario.setValido(false);

        try {
            // Obtener lista de usuarios desde la API REST
            GenericRestClient usuarioClient = AdministradorJpa.getInstancia().getUsuarioJpa();
            List<GenericDto> listaUsuariosDto = usuarioClient.findAll();

            if (listaUsuariosDto != null && !listaUsuariosDto.isEmpty()) {
                for (GenericDto usuarioDto : listaUsuariosDto) {
                    String nombreUsuario = usuarioDto.getString("nombre");
                    
                    if (nombreUsuario != null && nombreUsuario.equals(miDtoUsuario.getNombre())) {
                        passWordUsuario = usuarioDto.getString("contrasenia");
                        passWordIngresado = ControllerNegocio.getInstancia()
                                .encriptaEnMD5(miDtoUsuario.getContrasenia());

                        Boolean estado = usuarioDto.getBoolean("estado");
                        if (passWordUsuario != null && passWordUsuario.equals(passWordIngresado) 
                                && estado != null && estado) {
                            flag = true;
                            
                            // Convertir GenericDto a DtoUsuario
                            miDtoUsuario = convertirGenericDtoADtoUsuario(usuarioDto);
                            miDtoUsuario.setValido(true);
                            break;
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(AdministradorSesion.class.getName()).log(Level.SEVERE, 
                    "Error al validar usuario desde API REST", ex);
        }
        return miDtoUsuario;
    }

    /**
     * Convierte un GenericDto a DtoUsuario
     */
    private DtoUsuario convertirGenericDtoADtoUsuario(GenericDto usuarioDto) {
        DtoUsuario dto = new DtoUsuario();
        dto.setIdUsuario(usuarioDto.getInt("idUsuario"));
        dto.setNombre(usuarioDto.getString("nombre"));
        dto.setContrasenia(usuarioDto.getString("contrasenia"));
        Boolean estado = usuarioDto.getBoolean("estado");
        if (estado != null) {
            dto.setEstado(estado);
        }
        dto.setTipo(usuarioDto.getString("tipo"));
        dto.setVersion(usuarioDto.getInt("version"));
        
        // Si hay información de persona, convertirla también
        Object personaObj = usuarioDto.get("personas");
        if (personaObj != null && personaObj instanceof GenericDto) {
            GenericDto personaDto = (GenericDto) personaObj;
            com.licensis.notaire.dto.DtoPersona dtoPersona = new com.licensis.notaire.dto.DtoPersona();
            dtoPersona.setIdPersona(personaDto.getInt("idPersona"));
            dtoPersona.setNombre(personaDto.getString("nombre"));
            dtoPersona.setApellido(personaDto.getString("apellido"));
            dto.setPersonas(dtoPersona);
        }
        
        return dto;
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
