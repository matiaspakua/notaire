package com.licensis.notaire.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.util.Objects;

/**
 * CU27/CU39 - Costo (fijo o variable) esperado de un tipo de documento dentro
 * de la plantilla de presupuesto de un tipo de trámite.
 */
@Entity
@Table(name = "plantilla_costos_documento")
public class DocumentCostTemplate implements Serializable, Persistable<DocumentCostTemplatePK> {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DocumentCostTemplatePK documentCostTemplatePK;

    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ProcedureType procedureType;

    @JoinColumn(name = "fk_id_tipo_documento", referencedColumnName = "id_tipo_documento", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private DocumentType documentType;

    @Column(name = "monto_fijo")
    private Float fixedAmount;

    @Column(name = "porcentaje_variable")
    private Float variablePercentage;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Transient
    private boolean isNewEntity = true;

    // Sets by Spring Data JPA's isNew() default heuristic for entities whose @EmbeddedId
    // is client-assigned (never null), so id-nullness cannot signal "new" the way it does
    // for @GeneratedValue entities. A transient flag flipped by these lifecycle callbacks
    // is the correct, standard Spring Data pattern for this case.
    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public DocumentCostTemplatePK getId() {
        return documentCostTemplatePK;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return isNewEntity;
    }


    public DocumentCostTemplate() {
    }

    public DocumentCostTemplate(int fkIdProcedureType, int fkIdDocumentType) {
        this.documentCostTemplatePK = new DocumentCostTemplatePK(fkIdProcedureType, fkIdDocumentType);
    }

    public DocumentCostTemplatePK getDocumentCostTemplatePK() {
        return documentCostTemplatePK;
    }

    public void setDocumentCostTemplatePK(DocumentCostTemplatePK documentCostTemplatePK) {
        this.documentCostTemplatePK = documentCostTemplatePK;
    }

    public ProcedureType getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(ProcedureType procedureType) {
        this.procedureType = procedureType;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Float getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(Float fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public Float getVariablePercentage() {
        return variablePercentage;
    }

    public void setVariablePercentage(Float variablePercentage) {
        this.variablePercentage = variablePercentage;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(documentCostTemplatePK);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DocumentCostTemplate other)) {
            return false;
        }
        return Objects.equals(this.documentCostTemplatePK, other.documentCostTemplatePK);
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "PlantillaCostoDocumento[ plantillaCostoDocumentoPK=" + documentCostTemplatePK + " ]";
    }
}
