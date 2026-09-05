package com.licensis.notaire.negocio;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PlantillaCostoDocumentoPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_tramite")
    private int fkIdTipoTramite;

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_documento")
    private int fkIdTipoDocumento;

    public PlantillaCostoDocumentoPK() {
    }

    public PlantillaCostoDocumentoPK(int fkIdTipoTramite, int fkIdTipoDocumento) {
        this.fkIdTipoTramite = fkIdTipoTramite;
        this.fkIdTipoDocumento = fkIdTipoDocumento;
    }

    public int getFkIdTipoTramite() {
        return fkIdTipoTramite;
    }

    public void setFkIdTipoTramite(int fkIdTipoTramite) {
        this.fkIdTipoTramite = fkIdTipoTramite;
    }

    public int getFkIdTipoDocumento() {
        return fkIdTipoDocumento;
    }

    public void setFkIdTipoDocumento(int fkIdTipoDocumento) {
        this.fkIdTipoDocumento = fkIdTipoDocumento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fkIdTipoTramite, fkIdTipoDocumento);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PlantillaCostoDocumentoPK other)) {
            return false;
        }
        return this.fkIdTipoTramite == other.fkIdTipoTramite
                && this.fkIdTipoDocumento == other.fkIdTipoDocumento;
    }

    @Override
    public String toString() {
        return "negocio.PlantillaCostoDocumentoPK[ fkIdTipoTramite=" + fkIdTipoTramite
                + ", fkIdTipoDocumento=" + fkIdTipoDocumento + " ]";
    }
}
