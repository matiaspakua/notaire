package com.licensis.notaire.dto;

public class DtoProcedureFolder {
    private Integer idFolder;
    private int number;
    private String status;
    private String waitReason;
    private Integer idManagement;
    private Integer idProcedure;

    public DtoProcedureFolder() {
    }

    public DtoProcedureFolder(Integer idFolder, int number, String status, String waitReason,
            Integer idManagement, Integer idProcedure) {
        this.idFolder = idFolder;
        this.number = number;
        this.status = status;
        this.waitReason = waitReason;
        this.idManagement = idManagement;
        this.idProcedure = idProcedure;
    }

    public Integer getIdFolder() {
        return idFolder;
    }

    public void setIdFolder(Integer idFolder) {
        this.idFolder = idFolder;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWaitReason() {
        return waitReason;
    }

    public void setWaitReason(String waitReason) {
        this.waitReason = waitReason;
    }

    public Integer getIdManagement() {
        return idManagement;
    }

    public void setIdManagement(Integer idManagement) {
        this.idManagement = idManagement;
    }

    public Integer getIdProcedure() {
        return idProcedure;
    }

    public void setIdProcedure(Integer idProcedure) {
        this.idProcedure = idProcedure;
    }
}
