package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

public class DtoTipoDeTramite implements DtoValido {
    private Integer idTipoTramite;
    private String nombre;
    private String observaciones;
    private Boolean habilitado;
    private Integer version;

    public DtoTipoDeTramite() {}

    public Integer getIdTipoTramite() { return idTipoTramite; }
    public void setIdTipoTramite(Integer idTipoTramite) { this.idTipoTramite = idTipoTramite; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getHabilitado() { return habilitado; }
    public void setHabilitado(Boolean habilitado) { this.habilitado = habilitado; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
