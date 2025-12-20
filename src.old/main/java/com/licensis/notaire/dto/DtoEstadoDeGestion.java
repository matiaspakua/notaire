package com.licensis.notaire.dto;
// Generated 19/04/2012 16:59:26 by Hibernate Tools 3.2.1.GA

import com.licensis.notaire.dto.interfaces.DtoValido;
import com.licensis.notaire.jpa.ConstantesPersistencia;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import java.util.HashSet;
import java.util.Set;

/**
 * DtoEstadoDeGestion
 */
public class DtoEstadoDeGestion implements DtoValido
{

    private Integer idEstadoGestion;
    private String nombre;
    private String observaciones;
    private Set historials = new HashSet(0);
    private Integer version;

    public DtoEstadoDeGestion()
    {
        this.version = ConstantesPersistencia.VERSION_INICIAL;
    }

    public DtoEstadoDeGestion(String nombre)
    {
        this.nombre = nombre;
    }

    public DtoEstadoDeGestion(String nombre, String observaciones, Set historials)
    {
        this.nombre = nombre;
        this.observaciones = observaciones;
        this.historials = historials;
    }

    public Integer getIdEstadoGestion()
    {
        return this.idEstadoGestion;
    }

    public void setIdEstadoGestion(Integer idEstadoGestion)
    {
        this.idEstadoGestion = idEstadoGestion;
    }

    public String getNombre()
    {
        return this.nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getObservaciones()
    {
        return this.observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public Set getHistorials()
    {
        return this.historials;
    }

    public void setHistorials(Set historials)
    {
        this.historials = historials;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    @Override
    public Boolean isValido()
    {
        boolean resultado = true;

        if (AdministradorValidaciones.getInstancia().validarCampoVacio(this.getNombre()))
        {
            return false;
        }
        if (!AdministradorValidaciones.getInstancia().validarCampoSoloTexto(this.getNombre()))
        {
            return false;
        }

        return resultado;
    }
}
