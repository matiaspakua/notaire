package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoUsuario;

/**
 * Clase Usuario simplificada para el frontend.
 * Versión sin dependencias JPA, solo para compatibilidad con código existente.
 *
 * @author juanca
 */
public class Usuario {

    private Integer idUsuario;
    private String nombre;
    private String contrasenia;
    private boolean estado;
    private String tipo;
    private int version;
    private Persona fkIdPersona;

    public Usuario() {
    }

    public Usuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Usuario(Integer idUsuario, String nombre, String contrasenia, boolean estado, String tipo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.contrasenia = contrasenia;
        this.estado = estado;
        this.tipo = tipo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Persona getFkIdPersona() {
        return fkIdPersona;
    }

    public void setFkIdPersona(Persona fkIdPersona) {
        this.fkIdPersona = fkIdPersona;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Establece los atributos del usuario desde un DTO
     */
    public void setAtributos(DtoUsuario dtoUsuario) {
        // Ref persona de Usuario
        if (dtoUsuario.getPersonas() != null) {
            Persona miPersona = new Persona();
            miPersona.setAtributos(dtoUsuario.getPersonas());
            setFkIdPersona(miPersona);
        }

        // Atributos usuario
        setContrasenia(dtoUsuario.getContrasenia());
        setEstado(dtoUsuario.isEstado());
        setIdUsuario(dtoUsuario.getIdUsuario());
        setNombre(dtoUsuario.getNombre());
        setTipo(dtoUsuario.getTipo());

        if (dtoUsuario.getVersion() != null) {
            // Controlo Version Objeto
            setVersion(dtoUsuario.getVersion());
        }
    }

    /**
     * Convierte el usuario a DTO
     */
    public DtoUsuario getDto() {
        DtoUsuario miDto = new DtoUsuario();
        try {
            miDto.setContrasenia(contrasenia);
            miDto.setEstado(estado);
            miDto.setIdUsuario(idUsuario);
            miDto.setNombre(nombre);

            if (this.getFkIdPersona() != null) {
                DtoPersona miDtoPersona = this.getFkIdPersona().getDto();
                miDto.setPersonas(miDtoPersona);
            }

            miDto.setTipo(tipo);

            // Controlo la version del objeto
            miDto.setVersion(version);

        } catch (NullPointerException e) {
            System.out.println("Error Metodo : getDtoUsuario");
        }
        return miDto;
    }
}
