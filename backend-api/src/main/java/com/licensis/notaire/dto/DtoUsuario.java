package com.licensis.notaire.dto;
// Generated 19/04/2012 16:59:26 by Hibernate Tools 3.2.1.GA

/**
 * DTO que representa un Usuario.
 */
public class DtoUsuario
{

    private Integer idUsuario;
    private DtoPersona personas;
    private String nombre;
    private String contrasenia;
    private boolean estado;
    private String tipo;
    private String registroAuditoria;
    private Integer version;
    private boolean valido;

    public boolean isValido()
    {
        return valido;
    }

    public void setValido(boolean valido)
    {
        this.valido = valido;
    }

    public DtoUsuario()
    {
    }

    public Integer getIdUsuario()
    {
        return this.idUsuario;
    }

    public void setIdUsuario(Integer idUsuario)
    {
        this.idUsuario = idUsuario;
    }

    public DtoPersona getPersonas()
    {
        return this.personas;
    }

    public void setPersonas(DtoPersona personas)
    {
        this.personas = personas;
    }

    public String getNombre()
    {
        return this.nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getContrasenia()
    {
        return this.contrasenia;
    }

    public void setContrasenia(String contrasenia)
    {
        this.contrasenia = contrasenia;
    }

    public boolean isEstado()
    {
        return this.estado;
    }

    public void setEstado(boolean estado)
    {
        this.estado = estado;
    }

    public String getTipo()
    {
        return this.tipo;
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

    public String getRegistroAuditorias()
    {
        String registroAuditoria;

        registroAuditoria = this.getNombre() + " - " + this.getTipo();

        return registroAuditoria;
    }

    //Controlo la version del objeto
    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }
}
