package com.licensis.notaire.business;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DocumentCostTemplatePK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_tramite")
    private int fkIdProcedureType;

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_documento")
    private int fkIdDocumentType;

    public DocumentCostTemplatePK() {
    }

    public DocumentCostTemplatePK(int fkIdProcedureType, int fkIdDocumentType) {
        this.fkIdProcedureType = fkIdProcedureType;
        this.fkIdDocumentType = fkIdDocumentType;
    }

    public int getFkIdProcedureType() {
        return fkIdProcedureType;
    }

    public void setFkIdProcedureType(int fkIdProcedureType) {
        this.fkIdProcedureType = fkIdProcedureType;
    }

    public int getFkIdDocumentType() {
        return fkIdDocumentType;
    }

    public void setFkIdDocumentType(int fkIdDocumentType) {
        this.fkIdDocumentType = fkIdDocumentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fkIdProcedureType, fkIdDocumentType);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DocumentCostTemplatePK other)) {
            return false;
        }
        return this.fkIdProcedureType == other.fkIdProcedureType
                && this.fkIdDocumentType == other.fkIdDocumentType;
    }

    @Override
    public String toString() {
        return "negocio.PlantillaCostoDocumentoPK[ fkIdTipoTramite=" + fkIdProcedureType
                + ", fkIdTipoDocumento=" + fkIdDocumentType + " ]";
    }
}
