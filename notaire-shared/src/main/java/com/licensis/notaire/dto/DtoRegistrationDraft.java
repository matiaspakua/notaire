package com.licensis.notaire.dto;

import java.util.Date;

public class DtoRegistrationDraft {

    private Integer idRegistrationDraft;
    private int number;
    private Float operationPrice;
    private String status;
    private Date dateGeneration;
    private Date dateSubmission;
    private String registryEntryNumber;
    private Date dateReception;
    private String finalRegistrationNumber;
    private String registryNotes;
    private Date dateCorrection;
    private Integer idDeed;

    public DtoRegistrationDraft() {
    }

    public DtoRegistrationDraft(Integer idRegistrationDraft, int number, Float operationPrice, String status,
            Date dateGeneration, Date dateSubmission, String registryEntryNumber, Date dateReception,
            String finalRegistrationNumber, String registryNotes, Date dateCorrection,
            Integer idDeed) {
        this.idRegistrationDraft = idRegistrationDraft;
        this.number = number;
        this.operationPrice = operationPrice;
        this.status = status;
        this.dateGeneration = dateGeneration;
        this.dateSubmission = dateSubmission;
        this.registryEntryNumber = registryEntryNumber;
        this.dateReception = dateReception;
        this.finalRegistrationNumber = finalRegistrationNumber;
        this.registryNotes = registryNotes;
        this.dateCorrection = dateCorrection;
        this.idDeed = idDeed;
    }

    public Integer getIdRegistrationDraft() {
        return idRegistrationDraft;
    }

    public void setIdRegistrationDraft(Integer idRegistrationDraft) {
        this.idRegistrationDraft = idRegistrationDraft;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Float getOperationPrice() {
        return operationPrice;
    }

    public void setOperationPrice(Float operationPrice) {
        this.operationPrice = operationPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(Date dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public Date getDateSubmission() {
        return dateSubmission;
    }

    public void setDateSubmission(Date dateSubmission) {
        this.dateSubmission = dateSubmission;
    }

    public String getRegistryEntryNumber() {
        return registryEntryNumber;
    }

    public void setRegistryEntryNumber(String registryEntryNumber) {
        this.registryEntryNumber = registryEntryNumber;
    }

    public Date getDateReception() {
        return dateReception;
    }

    public void setDateReception(Date dateReception) {
        this.dateReception = dateReception;
    }

    public String getFinalRegistrationNumber() {
        return finalRegistrationNumber;
    }

    public void setFinalRegistrationNumber(String finalRegistrationNumber) {
        this.finalRegistrationNumber = finalRegistrationNumber;
    }

    public String getRegistryNotes() {
        return registryNotes;
    }

    public void setRegistryNotes(String registryNotes) {
        this.registryNotes = registryNotes;
    }

    public Date getDateCorrection() {
        return dateCorrection;
    }

    public void setDateCorrection(Date dateCorrection) {
        this.dateCorrection = dateCorrection;
    }

    public Integer getIdDeed() {
        return idDeed;
    }

    public void setIdDeed(Integer idDeed) {
        this.idDeed = idDeed;
    }
}
