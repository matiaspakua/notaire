package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

public class DtoTipoIdentificacion implements DtoValido {
    private Integer idTipoIdentificacion;
    private String nombre;
    private String caracteres;
    private Integer version;

    public DtoTipoIdentificacion() {}

    public Integer getIdTipoIdentificacion() { return idTipoIdentificacion; }
    public void setIdTipoIdentificacion(Integer idTipoIdentificacion) { this.idTipoIdentificacion = idTipoIdentificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCaracteres() { return caracteres; }
    public void setCaracteres(String caracteres) { this.caracteres = caracteres; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
