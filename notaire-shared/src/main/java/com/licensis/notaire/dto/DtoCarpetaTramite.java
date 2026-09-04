package com.licensis.notaire.dto;

public class DtoCarpetaTramite {
    private Integer idCarpeta;
    private int numero;
    private String estado;
    private String motivoEspera;
    private Integer idGestion;
    private Integer idTramite;

    public DtoCarpetaTramite() {
    }

    public DtoCarpetaTramite(Integer idCarpeta, int numero, String estado, String motivoEspera,
            Integer idGestion, Integer idTramite) {
        this.idCarpeta = idCarpeta;
        this.numero = numero;
        this.estado = estado;
        this.motivoEspera = motivoEspera;
        this.idGestion = idGestion;
        this.idTramite = idTramite;
    }

    public Integer getIdCarpeta() {
        return idCarpeta;
    }

    public void setIdCarpeta(Integer idCarpeta) {
        this.idCarpeta = idCarpeta;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoEspera() {
        return motivoEspera;
    }

    public void setMotivoEspera(String motivoEspera) {
        this.motivoEspera = motivoEspera;
    }

    public Integer getIdGestion() {
        return idGestion;
    }

    public void setIdGestion(Integer idGestion) {
        this.idGestion = idGestion;
    }

    public Integer getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(Integer idTramite) {
        this.idTramite = idTramite;
    }
}
