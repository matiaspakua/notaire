package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

public class DtoPresupuesto implements DtoValido {
    private Integer idPresupuesto;
    private Integer version;

    public DtoPresupuesto() {}

    public Integer getIdPresupuesto() { return idPresupuesto; }
    public void setIdPresupuesto(Integer idPresupuesto) { this.idPresupuesto = idPresupuesto; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
