package com.licensis.notaire.dto;

import java.util.Date;

public class DtoMinutaInscripcion {

    private Integer idMinutaInscripcion;
    private int numero;
    private Float precioOperacion;
    private String estado;
    private Date fechaGeneracion;
    private Date fechaPresentacion;
    private String numeroEntradaRegistral;
    private Date fechaRecepcion;
    private String numeroInscripcionDefinitivo;
    private String observacionesRegistro;
    private Date fechaSubsanacion;
    private Integer idEscritura;

    public DtoMinutaInscripcion() {
    }

    public DtoMinutaInscripcion(Integer idMinutaInscripcion, int numero, Float precioOperacion, String estado,
            Date fechaGeneracion, Date fechaPresentacion, String numeroEntradaRegistral, Date fechaRecepcion,
            String numeroInscripcionDefinitivo, String observacionesRegistro, Date fechaSubsanacion,
            Integer idEscritura) {
        this.idMinutaInscripcion = idMinutaInscripcion;
        this.numero = numero;
        this.precioOperacion = precioOperacion;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
        this.fechaPresentacion = fechaPresentacion;
        this.numeroEntradaRegistral = numeroEntradaRegistral;
        this.fechaRecepcion = fechaRecepcion;
        this.numeroInscripcionDefinitivo = numeroInscripcionDefinitivo;
        this.observacionesRegistro = observacionesRegistro;
        this.fechaSubsanacion = fechaSubsanacion;
        this.idEscritura = idEscritura;
    }

    public Integer getIdMinutaInscripcion() {
        return idMinutaInscripcion;
    }

    public void setIdMinutaInscripcion(Integer idMinutaInscripcion) {
        this.idMinutaInscripcion = idMinutaInscripcion;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Float getPrecioOperacion() {
        return precioOperacion;
    }

    public void setPrecioOperacion(Float precioOperacion) {
        this.precioOperacion = precioOperacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Date getFechaPresentacion() {
        return fechaPresentacion;
    }

    public void setFechaPresentacion(Date fechaPresentacion) {
        this.fechaPresentacion = fechaPresentacion;
    }

    public String getNumeroEntradaRegistral() {
        return numeroEntradaRegistral;
    }

    public void setNumeroEntradaRegistral(String numeroEntradaRegistral) {
        this.numeroEntradaRegistral = numeroEntradaRegistral;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getNumeroInscripcionDefinitivo() {
        return numeroInscripcionDefinitivo;
    }

    public void setNumeroInscripcionDefinitivo(String numeroInscripcionDefinitivo) {
        this.numeroInscripcionDefinitivo = numeroInscripcionDefinitivo;
    }

    public String getObservacionesRegistro() {
        return observacionesRegistro;
    }

    public void setObservacionesRegistro(String observacionesRegistro) {
        this.observacionesRegistro = observacionesRegistro;
    }

    public Date getFechaSubsanacion() {
        return fechaSubsanacion;
    }

    public void setFechaSubsanacion(Date fechaSubsanacion) {
        this.fechaSubsanacion = fechaSubsanacion;
    }

    public Integer getIdEscritura() {
        return idEscritura;
    }

    public void setIdEscritura(Integer idEscritura) {
        this.idEscritura = idEscritura;
    }
}
