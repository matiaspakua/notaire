package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.Date;

public class DtoHistory implements DtoValido {
    private Integer idHistory;
    private DtoDeedManagement gestionesDeEscrituras;
    private DtoManagementStatus estadosDeManagement;
    private Date date;
    private String notes;
    private Integer version;

    public DtoHistory() {}
    public DtoHistory(DtoDeedManagement gestionesDeEscrituras, DtoManagementStatus estadosDeManagement, Date date) {
        this.gestionesDeEscrituras = gestionesDeEscrituras;
        this.estadosDeManagement = estadosDeManagement;
        this.date = date;
    }
    public DtoHistory(DtoDeedManagement gestionesDeEscrituras, DtoManagementStatus estadosDeManagement, Date date, String notes) {
        this.gestionesDeEscrituras = gestionesDeEscrituras;
        this.estadosDeManagement = estadosDeManagement;
        this.date = date;
        this.notes = notes;
    }

    public Integer getIdHistory() { return idHistory; }
    public void setIdHistory(Integer idHistory) { this.idHistory = idHistory; }
    public DtoDeedManagement getGestionesDeEscrituras() { return gestionesDeEscrituras; }
    public void setGestionesDeEscrituras(DtoDeedManagement gestionesDeEscrituras) { this.gestionesDeEscrituras = gestionesDeEscrituras; }
    public DtoManagementStatus getEstadosDeManagement() { return estadosDeManagement; }
    public void setEstadosDeManagement(DtoManagementStatus estadosDeManagement) { this.estadosDeManagement = estadosDeManagement; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
