package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.ValidDto;
import java.util.ArrayList;
import java.util.List;

public class DtoProcedure implements ValidDto {
    private Integer idProcedure;
    private DtoDeedManagement deedManagement;
    private DtoProperty property;
    private DtoDeed deed;
    private DtoProcedureType procedureType;
    private DtoBudget budget;
    private String notes;
    private List<DtoPerson> people;
    private List<DtoBudget> budgets;
    private List<DtoSubmittedDocument> submittedDocuments;
    private List<DtoDocumentType> requiredDocuments;
    private List<DtoSubmittedDocument> documentsNotSubmitted;

    public DtoProcedure() {
        this.idProcedure = INITIAL_DTO_ID;
        this.property = new DtoProperty();
        this.people = new ArrayList<>();
        this.budgets = new ArrayList<>();
        this.submittedDocuments = new ArrayList<>();
        this.requiredDocuments = new ArrayList<>();
        this.documentsNotSubmitted = new ArrayList<>();
    }

    public DtoProcedure(DtoProcedureType procedureType, DtoBudget budget) {
        this();
        this.procedureType = procedureType;
        this.budget = budget;
    }

    public Integer getIdProcedure() { return idProcedure; }
    public void setIdProcedure(Integer idProcedure) { this.idProcedure = idProcedure; }
    public DtoDeedManagement getManagement() { return deedManagement; }
    public void setDeedManagement(DtoDeedManagement g) { this.deedManagement = g; }
    public DtoProperty getProperty() { return property; }
    public void setProperty(DtoProperty property) { this.property = property; }
    public DtoDeed getDeed() { return deed; }
    public void setDeed(DtoDeed deed) { this.deed = deed; }
    public DtoProcedureType getProcedureType() { return procedureType; }
    public void setProcedureType(DtoProcedureType t) { this.procedureType = t; }
    public DtoBudget getBudget() { return budget; }
    public void setBudget(DtoBudget budget) { this.budget = budget; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<DtoSubmittedDocument> getSubmittedDocuments() { return submittedDocuments; }
    public void setSubmittedDocuments(List<DtoSubmittedDocument> list) {
        if (list != null) for (DtoSubmittedDocument d : list) this.submittedDocuments.add(d);
    }
    public List<DtoPerson> getPeople() { return people; }
    public void setPeople(List<DtoPerson> people) { this.people = people != null ? people : new ArrayList<>(); }
    public List<DtoBudget> getBudgets() { return budgets; }
    public void setBudgets(List<DtoBudget> budgets) { this.budgets = budgets != null ? budgets : new ArrayList<>(); }
    public List<DtoDocumentType> getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(List<DtoDocumentType> list) { this.requiredDocuments = list != null ? list : new ArrayList<>(); }
    public List<DtoSubmittedDocument> getDocumentsNotSubmitted() { return documentsNotSubmitted; }
    public void setDocumentsNotSubmitted(List<DtoSubmittedDocument> list) { this.documentsNotSubmitted = list != null ? list : new ArrayList<>(); }
}
