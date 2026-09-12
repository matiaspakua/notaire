package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DtoDeedManagement implements DtoValido {
    private Integer idManagement;
    private DtoPerson personNotary;
    private DtoPerson clientReferencia;
    private List<DtoPerson> listaClientesInvolucrados;
    private int number;
    private Date dateStart;
    private String encabezado;
    private String notes;
    private Integer numberArchivo;
    private Integer numberBibliorato;
    private List<DtoProcedure> listaProceduresAsociados;
    private List<DtoHistory> recordHistory;
    private DtoManagementStatus status;
    private int version;

    public DtoDeedManagement() {
        this.listaClientesInvolucrados = new ArrayList<>();
        this.listaProceduresAsociados = new ArrayList<>();
    }

    public DtoDeedManagement(DtoPerson person, int number, Date dateStart, String encabezado) {
        this.personNotary = person;
        this.number = number;
        this.dateStart = dateStart;
        this.encabezado = encabezado;
        this.listaClientesInvolucrados = new ArrayList<>();
        this.listaProceduresAsociados = new ArrayList<>();
    }

    public Integer getIdManagement() { return idManagement; }
    public void setIdManagement(Integer idManagement) { this.idManagement = idManagement; }
    public DtoPerson getPersonNotary() { return personNotary; }
    public void setPersonNotary(DtoPerson personNotary) { this.personNotary = personNotary; }
    public DtoPerson getClientReferencia() { return clientReferencia; }
    public void setClientReferencia(DtoPerson clientReferencia) { this.clientReferencia = clientReferencia; }
    public List<DtoPerson> getListaClientesInvolucrados() { return listaClientesInvolucrados; }
    public void setListaClientesInvolucrados(List<DtoPerson> listaClientesInvolucrados) { if (listaClientesInvolucrados != null) this.listaClientesInvolucrados.addAll(listaClientesInvolucrados); }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public Date getDateStart() { return dateStart; }
    public void setDateStart(Date dateStart) { this.dateStart = dateStart; }
    public String getEncabezado() { return encabezado; }
    public void setEncabezado(String encabezado) { this.encabezado = encabezado; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getNumberArchivo() { return numberArchivo; }
    public void setNumberArchivo(Integer numberArchivo) { this.numberArchivo = numberArchivo; }
    public Integer getNumberBibliorato() { return numberBibliorato; }
    public void setNumberBibliorato(Integer numberBibliorato) { this.numberBibliorato = numberBibliorato; }
    public List<DtoProcedure> getListaProceduresAsociados() { return listaProceduresAsociados; }
    public void setListaProceduresAsociados(List<DtoProcedure> listaProceduresAsociados) { if (listaProceduresAsociados != null) this.listaProceduresAsociados.addAll(listaProceduresAsociados); }
    public List<DtoHistory> getRecordHistory() { return recordHistory; }
    public void setRecordHistory(List<DtoHistory> recordHistory) { this.recordHistory = recordHistory; }
    public DtoManagementStatus getStatus() { return status; }
    public void setStatus(DtoManagementStatus status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
