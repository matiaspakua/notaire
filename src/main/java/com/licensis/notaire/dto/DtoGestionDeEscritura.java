package com.licensis.notaire.dto;
// Generated 19/04/2012 16:59:26 by Hibernate Tools 3.2.1.GA

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.licensis.notaire.servicios.AdministradorValidaciones;

/**
 * DtoGestionDeEscritura
 */
public class DtoGestionDeEscritura implements DtoValido
{

    private Integer idGestion;
    private DtoPersona personaEscribano;
    private DtoPersona clienteReferencia;
    private List<DtoPersona> listaClientesInvolucrados;
    private int numero;
    private Date fechaInicio;
    private String encabezado;
    private String observaciones;
    private Integer numeroArchivo;
    private Integer numeroBibliorato;
    private List<DtoTramite> listaTramitesAsociados;
    private List<DtoHistorial> registroHistorial;
    private DtoEstadoDeGestion estado;

    public DtoGestionDeEscritura() {
        this.listaClientesInvolucrados = new ArrayList<>();
        this.listaTramitesAsociados = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    private int version;

    public DtoEstadoDeGestion getEstado() {
        return estado;
    }

    public void setEstado(DtoEstadoDeGestion estado) {
        this.estado = estado;
    }

    public DtoGestionDeEscritura(DtoPersona persona, int numero, Date fechaInicio, String encabezado) {
        this.personaEscribano = persona;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.encabezado = encabezado;
    }

    public DtoGestionDeEscritura(DtoPersona persona, int numero, Date fechaInicio, String encabezado, String observaciones, Integer numeroArchivo, Integer numeroBibliorato, List<DtoTramite> tramites, List<DtoHistorial> historial) {
        this.personaEscribano = persona;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.encabezado = encabezado;
        this.observaciones = observaciones;
        this.numeroArchivo = numeroArchivo;
        this.numeroBibliorato = numeroBibliorato;
        this.listaTramitesAsociados = tramites;
        this.registroHistorial = historial;
    }

    public DtoPersona getClienteReferencia() {
        return clienteReferencia;
    }

    public void setClienteReferencia(DtoPersona clienteReferencia) {
        this.clienteReferencia = clienteReferencia;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Integer getIdGestion() {
        return idGestion;
    }

    public void setIdGestion(Integer idGestion) {
        this.idGestion = idGestion;
    }

    public List<DtoPersona> getListaClientesInvolucrados() {
        return listaClientesInvolucrados;
    }

    public void setListaClientesInvolucrados(List<DtoPersona> listaClientesInvolucrados) {
        this.listaClientesInvolucrados.addAll(listaClientesInvolucrados);
    }

    public List<DtoTramite> getListaTramitesAsociados() {
        return listaTramitesAsociados;
    }

    public void setListaTramitesAsociados(List<DtoTramite> listaTramitesAsociados) {
        this.listaTramitesAsociados.addAll(listaTramitesAsociados);
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Integer getNumeroArchivo() {
        return numeroArchivo;
    }

    public void setNumeroArchivo(Integer numeroArchivo) {
        this.numeroArchivo = numeroArchivo;
    }

    public Integer getNumeroBibliorato() {
        return numeroBibliorato;
    }

    public void setNumeroBibliorato(Integer numeroBibliorato) {
        this.numeroBibliorato = numeroBibliorato;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public DtoPersona getPersonaEscribano() {
        return personaEscribano;
    }

    public void setPersonaEscribano(DtoPersona personaEscribano) {
        this.personaEscribano = personaEscribano;
    }

    public List<DtoHistorial> getRegistroHistorial() {
        return registroHistorial;
    }

    public void setRegistroHistorial(List<DtoHistorial> registroHistorial) {
        this.registroHistorial = registroHistorial;
    }

    @Override
    public Boolean isValido() {
        Boolean resultado = Boolean.TRUE;

        AdministradorValidaciones validador = AdministradorValidaciones.getInstancia();

        if (!validador.validarNumero(this.getNumero()))
        {
            return false;
        }
        if (this.getEncabezado().isEmpty())
        {
            return false;
        }
        if (!this.getClienteReferencia().isValido())
        {
            return false;
        }
        if (this.getListaTramitesAsociados().isEmpty())
        {
            return false;
        }
        if (!this.getPersonaEscribano().isValido())
        {
            return false;
        }
        if (this.getListaTramitesAsociados().isEmpty())
        {
            return false;
        }

        return resultado;
    }
}
