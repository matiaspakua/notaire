package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.Date;

public class DtoHistorial implements DtoValido {
    private Integer idHistorial;
    private DtoGestionDeEscritura gestionesDeEscrituras;
    private DtoEstadoDeGestion estadosDeGestion;
    private Date fecha;
    private String observaciones;
    private Integer version;

    public DtoHistorial() {}
    public DtoHistorial(DtoGestionDeEscritura gestionesDeEscrituras, DtoEstadoDeGestion estadosDeGestion, Date fecha) {
        this.gestionesDeEscrituras = gestionesDeEscrituras;
        this.estadosDeGestion = estadosDeGestion;
        this.fecha = fecha;
    }
    public DtoHistorial(DtoGestionDeEscritura gestionesDeEscrituras, DtoEstadoDeGestion estadosDeGestion, Date fecha, String observaciones) {
        this.gestionesDeEscrituras = gestionesDeEscrituras;
        this.estadosDeGestion = estadosDeGestion;
        this.fecha = fecha;
        this.observaciones = observaciones;
    }

    public Integer getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Integer idHistorial) { this.idHistorial = idHistorial; }
    public DtoGestionDeEscritura getGestionesDeEscrituras() { return gestionesDeEscrituras; }
    public void setGestionesDeEscrituras(DtoGestionDeEscritura gestionesDeEscrituras) { this.gestionesDeEscrituras = gestionesDeEscrituras; }
    public DtoEstadoDeGestion getEstadosDeGestion() { return estadosDeGestion; }
    public void setEstadosDeGestion(DtoEstadoDeGestion estadosDeGestion) { this.estadosDeGestion = estadosDeGestion; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
