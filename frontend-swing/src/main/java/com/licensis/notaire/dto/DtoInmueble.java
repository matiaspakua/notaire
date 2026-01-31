package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

public class DtoInmueble implements DtoValido {
    private Integer idInmueble;
    private String nomenclatura;
    private String matricula;
    private Integer valuacionAnio;
    private Float valuacionFiscal;
    private String domicilio;
    private String localidad;
    private Integer version;

    public DtoInmueble() {}

    public Integer getIdInmueble() { return idInmueble; }
    public void setIdInmueble(Integer idInmueble) { this.idInmueble = idInmueble; }
    public String getNomenclatura() { return nomenclatura; }
    public void setNomenclatura(String nomenclatura) { this.nomenclatura = nomenclatura; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Integer getValuacionAnio() { return valuacionAnio; }
    public void setValuacionAnio(Integer valuacionAnio) { this.valuacionAnio = valuacionAnio; }
    public Float getValuacionFiscal() { return valuacionFiscal; }
    public void setValuacionFiscal(Float valuacionFiscal) { this.valuacionFiscal = valuacionFiscal; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
