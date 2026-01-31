package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.ArrayList;
import java.util.List;

public class DtoTramite implements DtoValido {
    private Integer idTramite;
    private DtoGestionDeEscritura gestionDeEscrituras;
    private DtoInmueble inmueble;
    private DtoEscritura escritura;
    private DtoTipoDeTramite tipoDeTramite;
    private DtoPresupuesto presupuesto;
    private String observaciones;
    private List<DtoPersona> listaPersonas;
    private List<DtoPresupuesto> listaPresupuestos;
    private List<DtoDocumentoPresentado> listaDocumentosPresentados;
    private List<DtoTipoDeDocumento> listaDocumentosNecesarios;
    private List<DtoDocumentoPresentado> listaDocumentosNoPrecentados;

    public DtoTramite() {
        this.idTramite = ID_DTO_INICIALIZADO;
        this.inmueble = new DtoInmueble();
        this.listaPersonas = new ArrayList<>();
        this.listaPresupuestos = new ArrayList<>();
        this.listaDocumentosPresentados = new ArrayList<>();
        this.listaDocumentosNecesarios = new ArrayList<>();
        this.listaDocumentosNoPrecentados = new ArrayList<>();
    }

    public DtoTramite(DtoTipoDeTramite tiposDeTramite, DtoPresupuesto presupuestos) {
        this();
        this.tipoDeTramite = tiposDeTramite;
        this.presupuesto = presupuestos;
    }

    public Integer getIdTramite() { return idTramite; }
    public void setIdTramite(Integer idTramite) { this.idTramite = idTramite; }
    public DtoGestionDeEscritura getGestion() { return gestionDeEscrituras; }
    public DtoGestionDeEscritura getGestionDeEscrituras() { return gestionDeEscrituras; }
    public void setGestionDeEscritura(DtoGestionDeEscritura g) { this.gestionDeEscrituras = g; }
    public void setGestionDeEscrituras(DtoGestionDeEscritura g) { this.gestionDeEscrituras = g; }
    public DtoInmueble getInmueble() { return inmueble; }
    public void setInmueble(DtoInmueble inmueble) { this.inmueble = inmueble; }
    public DtoEscritura getEscritura() { return escritura; }
    public void setEscritura(DtoEscritura escritura) { this.escritura = escritura; }
    public DtoTipoDeTramite getTipoDeTramite() { return tipoDeTramite; }
    public void setTiposDeTramite(DtoTipoDeTramite t) { this.tipoDeTramite = t; }
    public DtoPresupuesto getPresupuesto() { return presupuesto; }
    public void setPresupuesto(DtoPresupuesto presupuesto) { this.presupuesto = presupuesto; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<DtoDocumentoPresentado> getListaDocumentosGestion() { return listaDocumentosPresentados; }
    public List<DtoDocumentoPresentado> getListaDocumentosPresentados() { return listaDocumentosPresentados; }
    public void setListaDocumentosPresentados(List<DtoDocumentoPresentado> list) {
        if (list != null) for (DtoDocumentoPresentado d : list) this.listaDocumentosPresentados.add(d);
    }
    public List<DtoPersona> getListaPersonas() { return listaPersonas; }
    public void setListaPersonas(List<DtoPersona> listaPersonas) { this.listaPersonas = listaPersonas != null ? listaPersonas : new ArrayList<>(); }
    public List<DtoPresupuesto> getListaPresupuestos() { return listaPresupuestos; }
    public void setListaPresupuestos(List<DtoPresupuesto> listaPresupuestos) { this.listaPresupuestos = listaPresupuestos != null ? listaPresupuestos : new ArrayList<>(); }
    public List<DtoTipoDeDocumento> getListaDocumentosNecesarios() { return listaDocumentosNecesarios; }
    public void setListaDocumentosNecesarios(List<DtoTipoDeDocumento> list) { this.listaDocumentosNecesarios = list != null ? list : new ArrayList<>(); }
    public List<DtoDocumentoPresentado> getListaDocumentosNoPrecentados() { return listaDocumentosNoPrecentados; }
    public void setListaDocumentosNoPrecentados(List<DtoDocumentoPresentado> list) { this.listaDocumentosNoPrecentados = list != null ? list : new ArrayList<>(); }
}
