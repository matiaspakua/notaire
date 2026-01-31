package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.Date;

public class DtoSuplencia implements DtoValido {
    private Integer idSuplencia;
    private DtoPersona suplente;
    private DtoPersona suplantado;
    private Date fechaDesde;
    private Date fechaHasta;
    private Integer version;

    public DtoSuplencia() {}

    public Integer getIdSuplencia() { return idSuplencia; }
    public void setIdSuplencia(Integer idSuplencia) { this.idSuplencia = idSuplencia; }
    public DtoPersona getSuplente() { return suplente; }
    public void setSuplente(DtoPersona suplente) { this.suplente = suplente; }
    public DtoPersona getSuplantado() { return suplantado; }
    public void setSuplantado(DtoPersona suplantado) { this.suplantado = suplantado; }
    public Date getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(Date fechaDesde) { this.fechaDesde = fechaDesde; }
    public Date getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(Date fechaHasta) { this.fechaHasta = fechaHasta; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
