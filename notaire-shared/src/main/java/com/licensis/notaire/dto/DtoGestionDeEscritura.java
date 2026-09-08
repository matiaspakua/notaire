package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DtoGestionDeEscritura implements DtoValido {
    private Integer idGestion;
    private DtoPerson personaEscribano;
    private DtoPerson clienteReferencia;
    private List<DtoPerson> listaClientesInvolucrados;
    private int numero;
    private Date fechaInicio;
    private String encabezado;
    private String observaciones;
    private Integer numeroArchivo;
    private Integer numeroBibliorato;
    private List<DtoTramite> listaTramitesAsociados;
    private List<DtoHistorial> registroHistorial;
    private DtoEstadoDeGestion estado;
    private int version;

    public DtoGestionDeEscritura() {
        this.listaClientesInvolucrados = new ArrayList<>();
        this.listaTramitesAsociados = new ArrayList<>();
    }

    public DtoGestionDeEscritura(DtoPerson persona, int numero, Date fechaInicio, String encabezado) {
        this.personaEscribano = persona;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.encabezado = encabezado;
        this.listaClientesInvolucrados = new ArrayList<>();
        this.listaTramitesAsociados = new ArrayList<>();
    }

    public Integer getIdGestion() { return idGestion; }
    public void setIdGestion(Integer idGestion) { this.idGestion = idGestion; }
    public DtoPerson getPersonaEscribano() { return personaEscribano; }
    public void setPersonaEscribano(DtoPerson personaEscribano) { this.personaEscribano = personaEscribano; }
    public DtoPerson getClienteReferencia() { return clienteReferencia; }
    public void setClienteReferencia(DtoPerson clienteReferencia) { this.clienteReferencia = clienteReferencia; }
    public List<DtoPerson> getListaClientesInvolucrados() { return listaClientesInvolucrados; }
    public void setListaClientesInvolucrados(List<DtoPerson> listaClientesInvolucrados) { if (listaClientesInvolucrados != null) this.listaClientesInvolucrados.addAll(listaClientesInvolucrados); }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getEncabezado() { return encabezado; }
    public void setEncabezado(String encabezado) { this.encabezado = encabezado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Integer getNumeroArchivo() { return numeroArchivo; }
    public void setNumeroArchivo(Integer numeroArchivo) { this.numeroArchivo = numeroArchivo; }
    public Integer getNumeroBibliorato() { return numeroBibliorato; }
    public void setNumeroBibliorato(Integer numeroBibliorato) { this.numeroBibliorato = numeroBibliorato; }
    public List<DtoTramite> getListaTramitesAsociados() { return listaTramitesAsociados; }
    public void setListaTramitesAsociados(List<DtoTramite> listaTramitesAsociados) { if (listaTramitesAsociados != null) this.listaTramitesAsociados.addAll(listaTramitesAsociados); }
    public List<DtoHistorial> getRegistroHistorial() { return registroHistorial; }
    public void setRegistroHistorial(List<DtoHistorial> registroHistorial) { this.registroHistorial = registroHistorial; }
    public DtoEstadoDeGestion getEstado() { return estado; }
    public void setEstado(DtoEstadoDeGestion estado) { this.estado = estado; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
