package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.HashSet;
import java.util.Set;

public class DtoEstadoDeGestion implements DtoValido {
    private Integer idEstadoGestion;
    private String nombre;
    private String observaciones;
    private Set historials = new HashSet(0);
    private Integer version;

    public DtoEstadoDeGestion() { this.version = DtoValido.VERSION_INICIAL; }
    public DtoEstadoDeGestion(String nombre) { this.nombre = nombre; }

    public Integer getIdEstadoGestion() { return idEstadoGestion; }
    public void setIdEstadoGestion(Integer idEstadoGestion) { this.idEstadoGestion = idEstadoGestion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Set getHistorials() { return historials; }
    public void setHistorials(Set historials) { this.historials = historials; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
