package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.Date;

/**
 * DTO que representa una Persona (versión simplificada para frontend)
 */
public class DtoPersona implements DtoValido
{

    private Integer idPersona;
    private String nombre;
    private String apellido;
    private String nacionalidad;
    private String cuit;
    private String sexo;
    private Date fechaNacimiento;
    private String estadoCivil;
    private Integer numeroNupcias;
    private String ocupacion;
    private String domicilio;
    private String telefono;
    private Integer registroEscribano;
    private boolean esCliente;
    private String numeroidentificacion;
    private String eMail;
    private Integer version;

    public DtoPersona()
    {
    }

    public DtoPersona(String nombre, String apellido, boolean esCliente)
    {
        this.nombre = nombre;
        this.apellido = apellido;
        this.esCliente = esCliente;
    }

    public Integer getIdPersona()
    {
        return this.idPersona;
    }

    public void setIdPersona(Integer idPersona)
    {
        this.idPersona = idPersona;
    }

    public String getNombre()
    {
        return this.nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getApellido()
    {
        return this.apellido;
    }

    public void setApellido(String apellido)
    {
        this.apellido = apellido;
    }

    public String getNacionalidad()
    {
        return this.nacionalidad;
    }

    public void setNacionalidad(String nacionalidad)
    {
        this.nacionalidad = nacionalidad;
    }

    public String getCuit()
    {
        return this.cuit;
    }

    public void setCuit(String cuit)
    {
        this.cuit = cuit;
    }

    public String getSexo()
    {
        return this.sexo;
    }

    public void setSexo(String sexo)
    {
        this.sexo = sexo;
    }

    public Date getFechaNacimiento()
    {
        return this.fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento)
    {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEstadoCivil()
    {
        return this.estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil)
    {
        this.estadoCivil = estadoCivil;
    }

    public Integer getNumeroNupcias()
    {
        return this.numeroNupcias;
    }

    public void setNumeroNupcias(Integer numeroNupcias)
    {
        this.numeroNupcias = numeroNupcias;
    }

    public String getOcupacion()
    {
        return this.ocupacion;
    }

    public void setOcupacion(String ocupacion)
    {
        this.ocupacion = ocupacion;
    }

    public String getDomicilio()
    {
        return this.domicilio;
    }

    public void setDomicilio(String domicilio)
    {
        this.domicilio = domicilio;
    }

    public String getTelefono()
    {
        return this.telefono;
    }

    public void setTelefono(String telefono)
    {
        this.telefono = telefono;
    }

    public Integer getRegistroEscribano()
    {
        return this.registroEscribano;
    }

    public void setRegistroEscribano(Integer registroEscribano)
    {
        this.registroEscribano = registroEscribano;
    }

    public boolean getEsCliente()
    {
        return this.esCliente;
    }

    public boolean isEsCliente()
    {
        return this.esCliente;
    }

    public DtoTipoIdentificacion getDtoTipoIdentificacion()
    {
        return dtoTipoIdentificacion;
    }

    public void setDtoTipoIdentificacion(DtoTipoIdentificacion dtoTipoIdentificacion)
    {
        this.dtoTipoIdentificacion = dtoTipoIdentificacion;
    }

    public void setEsCliente(boolean esCliente)
    {
        this.esCliente = esCliente;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion)
    {
        this.numeroidentificacion = numeroIdentificacion;
    }

    public String getNumeroIdentificacion()
    {
        return this.numeroidentificacion;
    }

    public void setEmail(String eMail)
    {
        this.eMail = eMail;
    }

    public String getEmail()
    {
        return this.eMail;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }
}

