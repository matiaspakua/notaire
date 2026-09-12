package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.ArrayList;
import java.util.List;

public class DtoProcedure implements DtoValido {
    private Integer idProcedure;
    private DtoDeedManagement managementDeEscrituras;
    private DtoProperty property;
    private DtoDeed deed;
    private DtoProcedureType procedureType;
    private DtoBudget budget;
    private String notes;
    private List<DtoPerson> listaPersons;
    private List<DtoBudget> listaPresupuestos;
    private List<DtoSubmittedDocument> listaDocumentsPresentados;
    private List<DtoDocumentType> listaDocumentsNecesarios;
    private List<DtoSubmittedDocument> listaDocumentsNoPrecentados;

    public DtoProcedure() {
        this.idProcedure = ID_DTO_INICIALIZADO;
        this.property = new DtoProperty();
        this.listaPersons = new ArrayList<>();
        this.listaPresupuestos = new ArrayList<>();
        this.listaDocumentsPresentados = new ArrayList<>();
        this.listaDocumentsNecesarios = new ArrayList<>();
        this.listaDocumentsNoPrecentados = new ArrayList<>();
    }

    public DtoProcedure(DtoProcedureType tiposDeProcedure, DtoBudget presupuestos) {
        this();
        this.procedureType = tiposDeProcedure;
        this.budget = presupuestos;
    }

    public Integer getIdProcedure() { return idProcedure; }
    public void setIdProcedure(Integer idProcedure) { this.idProcedure = idProcedure; }
    public DtoDeedManagement getManagement() { return managementDeEscrituras; }
    public DtoDeedManagement getManagementDeEscrituras() { return managementDeEscrituras; }
    public void setDeedManagement(DtoDeedManagement g) { this.managementDeEscrituras = g; }
    public void setManagementDeEscrituras(DtoDeedManagement g) { this.managementDeEscrituras = g; }
    public DtoProperty getProperty() { return property; }
    public void setProperty(DtoProperty property) { this.property = property; }
    public DtoDeed getDeed() { return deed; }
    public void setDeed(DtoDeed deed) { this.deed = deed; }
    public DtoProcedureType getProcedureType() { return procedureType; }
    public void setTiposDeProcedure(DtoProcedureType t) { this.procedureType = t; }
    public DtoBudget getBudget() { return budget; }
    public void setBudget(DtoBudget budget) { this.budget = budget; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<DtoSubmittedDocument> getListaDocumentsManagement() { return listaDocumentsPresentados; }
    public List<DtoSubmittedDocument> getListaDocumentsPresentados() { return listaDocumentsPresentados; }
    public void setListaDocumentsPresentados(List<DtoSubmittedDocument> list) {
        if (list != null) for (DtoSubmittedDocument d : list) this.listaDocumentsPresentados.add(d);
    }
    public List<DtoPerson> getListaPersons() { return listaPersons; }
    public void setListaPersons(List<DtoPerson> listaPersons) { this.listaPersons = listaPersons != null ? listaPersons : new ArrayList<>(); }
    public List<DtoBudget> getListaPresupuestos() { return listaPresupuestos; }
    public void setListaPresupuestos(List<DtoBudget> listaPresupuestos) { this.listaPresupuestos = listaPresupuestos != null ? listaPresupuestos : new ArrayList<>(); }
    public List<DtoDocumentType> getListaDocumentsNecesarios() { return listaDocumentsNecesarios; }
    public void setListaDocumentsNecesarios(List<DtoDocumentType> list) { this.listaDocumentsNecesarios = list != null ? list : new ArrayList<>(); }
    public List<DtoSubmittedDocument> getListaDocumentsNoPrecentados() { return listaDocumentsNoPrecentados; }
    public void setListaDocumentsNoPrecentados(List<DtoSubmittedDocument> list) { this.listaDocumentsNoPrecentados = list != null ? list : new ArrayList<>(); }
}
