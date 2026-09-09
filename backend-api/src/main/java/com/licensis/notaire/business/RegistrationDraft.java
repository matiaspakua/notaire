package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoRegistrationDraft;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.Date;

/**
 * Minuta de Inscripción de una escritura sobre un inmueble ante el Registro
 * de la Propiedad Inmueble, y su circuito registral (CU82): Generada,
 * Presentada, Observada, Inscripta.
 */
@Entity
@Table(name = "minutas_inscripcion")
public class RegistrationDraft implements Serializable, Persistable<Integer> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_minuta_inscripcion")
    private Integer idRegistrationDraft;

    @Basic(optional = false)
    @Column(name = "numero")
    private int number;

    @Column(name = "precio_operacion")
    private Float operationPrice;

    @Basic(optional = false)
    @Column(name = "estado")
    private String status;

    @Basic(optional = false)
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_generacion")
    private Date dateGeneration;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_presentacion")
    private Date dateSubmission;

    @Column(name = "numero_entrada_registral")
    private String registryEntryNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_recepcion")
    private Date dateReception;

    @Column(name = "numero_inscripcion_definitivo")
    private String finalRegistrationNumber;

    @Column(name = "observaciones_registro")
    private String registryNotes;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_subsanacion")
    private Date dateCorrection;

    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura", unique = true)
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Deed fkIdDeed;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idRegistrationDraft;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idRegistrationDraft == null || idRegistrationDraft.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
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

    public Deed getFkIdDeed() {
        return fkIdDeed;
    }

    public void setFkIdDeed(Deed fkIdDeed) {
        this.fkIdDeed = fkIdDeed;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return idRegistrationDraft != null ? idRegistrationDraft.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RegistrationDraft other)) {
            return false;
        }
        return idRegistrationDraft != null && idRegistrationDraft.equals(other.idRegistrationDraft);
    }

    @Override
    public String toString() {
        return "MinutaInscripcion[ idMinutaInscripcion=" + idRegistrationDraft + " ][ numero=" + number
                + " ][ estado=" + status + " ]";
    }

    public DtoRegistrationDraft getDto() {
        return new DtoRegistrationDraft(idRegistrationDraft, number, operationPrice, status, dateGeneration,
                dateSubmission, registryEntryNumber, dateReception, finalRegistrationNumber,
                registryNotes, dateCorrection, fkIdDeed != null ? fkIdDeed.getIdDeed() : null);
    }
}
