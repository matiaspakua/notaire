package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.ValidDto;
import java.util.Date;

public class DtoHistory implements ValidDto {
    private Integer idHistory;
    private DtoDeedManagement deedManagement;
    private DtoManagementStatus managementStatus;
    private Date date;
    private String notes;
    private Integer version;

    public DtoHistory() {}
    public DtoHistory(DtoDeedManagement deedManagement, DtoManagementStatus managementStatus, Date date) {
        this.deedManagement = deedManagement;
        this.managementStatus = managementStatus;
        this.date = date;
    }
    public DtoHistory(DtoDeedManagement deedManagement, DtoManagementStatus managementStatus, Date date, String notes) {
        this.deedManagement = deedManagement;
        this.managementStatus = managementStatus;
        this.date = date;
        this.notes = notes;
    }

    public Integer getIdHistory() { return idHistory; }
    public void setIdHistory(Integer idHistory) { this.idHistory = idHistory; }
    public DtoDeedManagement getDeedManagement() { return deedManagement; }
    public void setDeedManagement(DtoDeedManagement deedManagement) { this.deedManagement = deedManagement; }
    public DtoManagementStatus getManagementStatus() { return managementStatus; }
    public void setManagementStatus(DtoManagementStatus managementStatus) { this.managementStatus = managementStatus; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
