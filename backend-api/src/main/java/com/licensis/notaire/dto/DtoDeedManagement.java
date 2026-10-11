package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.ValidDto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DtoDeedManagement implements ValidDto {
    private Integer idManagement;
    private DtoPerson personNotary;
    private DtoPerson referenceClient;
    private List<DtoPerson> involvedClients;
    private int number;
    private Date dateStart;
    private String heading;
    private String notes;
    private Integer archiveNumber;
    private Integer binderNumber;
    private List<DtoProcedure> associatedProcedures;
    private List<DtoHistory> recordHistory;
    private DtoManagementStatus status;
    private int version;

    public DtoDeedManagement() {
        this.involvedClients = new ArrayList<>();
        this.associatedProcedures = new ArrayList<>();
    }

    public DtoDeedManagement(DtoPerson person, int number, Date dateStart, String heading) {
        this.personNotary = person;
        this.number = number;
        this.dateStart = dateStart;
        this.heading = heading;
        this.involvedClients = new ArrayList<>();
        this.associatedProcedures = new ArrayList<>();
    }

    public Integer getIdManagement() { return idManagement; }
    public void setIdManagement(Integer idManagement) { this.idManagement = idManagement; }
    public DtoPerson getPersonNotary() { return personNotary; }
    public void setPersonNotary(DtoPerson personNotary) { this.personNotary = personNotary; }
    public DtoPerson getReferenceClient() { return referenceClient; }
    public void setReferenceClient(DtoPerson referenceClient) { this.referenceClient = referenceClient; }
    public List<DtoPerson> getInvolvedClients() { return involvedClients; }
    public void setInvolvedClients(List<DtoPerson> involvedClients) { if (involvedClients != null) this.involvedClients.addAll(involvedClients); }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public Date getDateStart() { return dateStart; }
    public void setDateStart(Date dateStart) { this.dateStart = dateStart; }
    public String getHeading() { return heading; }
    public void setHeading(String heading) { this.heading = heading; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getArchiveNumber() { return archiveNumber; }
    public void setArchiveNumber(Integer archiveNumber) { this.archiveNumber = archiveNumber; }
    public Integer getBinderNumber() { return binderNumber; }
    public void setBinderNumber(Integer binderNumber) { this.binderNumber = binderNumber; }
    public List<DtoProcedure> getAssociatedProcedures() { return associatedProcedures; }
    public void setAssociatedProcedures(List<DtoProcedure> associatedProcedures) { if (associatedProcedures != null) this.associatedProcedures.addAll(associatedProcedures); }
    public List<DtoHistory> getRecordHistory() { return recordHistory; }
    public void setRecordHistory(List<DtoHistory> recordHistory) { this.recordHistory = recordHistory; }
    public DtoManagementStatus getStatus() { return status; }
    public void setStatus(DtoManagementStatus status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
