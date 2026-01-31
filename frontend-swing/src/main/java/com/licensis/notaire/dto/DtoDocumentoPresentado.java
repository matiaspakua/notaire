package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.Date;

public class DtoDocumentoPresentado implements DtoValido {
    private int version;
    private Integer idDocumentoPresentado;
    private DtoTramite fkTramite;
    private DtoTipoDeDocumento fkTipoDeDocumento;
    private String nombre;
    private Integer numeroCarton;
    private Date fechaIngreso;
    private Date fechaSalida;
    private boolean preparado;
    private boolean vence;
    private Date fechaVencimiento;
    private Integer diasVencimiento;
    private Float importeAPagar;
    private Date fechaPago;
    private boolean liberado;
    private Date fechaLiberado;
    private boolean observado;
    private String observaciones;
    private boolean reingresado;
    private String quienEntrega;
    private boolean entregado;

    public DtoDocumentoPresentado() {}

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Integer getIdDocumentoPresentado() { return idDocumentoPresentado; }
    public void setIdDocumentoPresentado(Integer idDocumentoPresentado) { this.idDocumentoPresentado = idDocumentoPresentado; }
    public DtoTramite getFkTramite() { return fkTramite; }
    public void setFkTramite(DtoTramite fkTramite) { this.fkTramite = fkTramite; }
    public DtoTipoDeDocumento getFkTipoDeDocumento() { return fkTipoDeDocumento; }
    public void setFkTipoDeDocumento(DtoTipoDeDocumento fkTipoDeDocumento) { this.fkTipoDeDocumento = fkTipoDeDocumento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getNumeroCarton() { return numeroCarton; }
    public void setNumeroCarton(Integer numeroCarton) { this.numeroCarton = numeroCarton; }
    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public Date getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(Date fechaSalida) { this.fechaSalida = fechaSalida; }
    public boolean isPreparado() { return preparado; }
    public void setPreparado(boolean preparado) { this.preparado = preparado; }
    public boolean isVence() { return vence; }
    public void setVence(boolean vence) { this.vence = vence; }
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Integer getDiasVencimiento() { return diasVencimiento; }
    public void setDiasVencimiento(Integer diasVencimiento) { this.diasVencimiento = diasVencimiento; }
    public Float getImporteAPagar() { return importeAPagar; }
    public void setImporteAPagar(Float importeAPagar) { this.importeAPagar = importeAPagar; }
    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }
    public boolean isLiberado() { return liberado; }
    public void setLiberado(boolean liberado) { this.liberado = liberado; }
    public Date getFechaLiberado() { return fechaLiberado; }
    public void setFechaLiberado(Date fechaLiberado) { this.fechaLiberado = fechaLiberado; }
    public boolean isObservado() { return observado; }
    public void setObservado(boolean observado) { this.observado = observado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isReingresado() { return reingresado; }
    public void setReingresado(boolean reingresado) { this.reingresado = reingresado; }
    public String getQuienEntrega() { return quienEntrega; }
    public void setQuienEntrega(String quienEntrega) { this.quienEntrega = quienEntrega; }
    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }
}
