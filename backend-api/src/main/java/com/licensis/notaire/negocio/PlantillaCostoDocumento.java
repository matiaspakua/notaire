package com.licensis.notaire.negocio;

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
import java.util.Objects;

/**
 * CU27/CU39 - Costo (fijo o variable) esperado de un tipo de documento dentro
 * de la plantilla de presupuesto de un tipo de trámite.
 */
@Entity
@Table(name = "plantilla_costos_documento")
public class PlantillaCostoDocumento implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PlantillaCostoDocumentoPK plantillaCostoDocumentoPK;

    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoDeTramite tipoDeTramite;

    @JoinColumn(name = "fk_id_tipo_documento", referencedColumnName = "id_tipo_documento", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoDeDocumento tipoDeDocumento;

    @Column(name = "monto_fijo")
    private Float montoFijo;

    @Column(name = "porcentaje_variable")
    private Float porcentajeVariable;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;

    public PlantillaCostoDocumento() {
    }

    public PlantillaCostoDocumento(int fkIdTipoTramite, int fkIdTipoDocumento) {
        this.plantillaCostoDocumentoPK = new PlantillaCostoDocumentoPK(fkIdTipoTramite, fkIdTipoDocumento);
    }

    public PlantillaCostoDocumentoPK getPlantillaCostoDocumentoPK() {
        return plantillaCostoDocumentoPK;
    }

    public void setPlantillaCostoDocumentoPK(PlantillaCostoDocumentoPK plantillaCostoDocumentoPK) {
        this.plantillaCostoDocumentoPK = plantillaCostoDocumentoPK;
    }

    public TipoDeTramite getTipoDeTramite() {
        return tipoDeTramite;
    }

    public void setTipoDeTramite(TipoDeTramite tipoDeTramite) {
        this.tipoDeTramite = tipoDeTramite;
    }

    public TipoDeDocumento getTipoDeDocumento() {
        return tipoDeDocumento;
    }

    public void setTipoDeDocumento(TipoDeDocumento tipoDeDocumento) {
        this.tipoDeDocumento = tipoDeDocumento;
    }

    public Float getMontoFijo() {
        return montoFijo;
    }

    public void setMontoFijo(Float montoFijo) {
        this.montoFijo = montoFijo;
    }

    public Float getPorcentajeVariable() {
        return porcentajeVariable;
    }

    public void setPorcentajeVariable(Float porcentajeVariable) {
        this.porcentajeVariable = porcentajeVariable;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(plantillaCostoDocumentoPK);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PlantillaCostoDocumento other)) {
            return false;
        }
        return Objects.equals(this.plantillaCostoDocumentoPK, other.plantillaCostoDocumentoPK);
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "PlantillaCostoDocumento[ plantillaCostoDocumentoPK=" + plantillaCostoDocumentoPK + " ]";
    }
}
