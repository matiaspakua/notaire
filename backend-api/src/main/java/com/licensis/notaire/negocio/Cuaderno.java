package com.licensis.notaire.negocio;

import com.licensis.notaire.jpa.ConstantesPersistencia;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;

/**
 * A cuaderno groups exactly ten consecutive folios of the same registro
 * notarial, numbered correlatively per registro/año, so the protocol
 * carátula (CU80) can be emitted.
 */
@Entity
@Table(name = "cuadernos")
@NamedQueries({
    @NamedQuery(name = "Cuaderno.findAll", query = "SELECT c FROM Cuaderno c"),
    @NamedQuery(name = "Cuaderno.findByAnioAndEscribano",
            query = "SELECT c FROM Cuaderno c WHERE c.anio = :anio AND c.fkIdPersonaEscribano = :escribano")
})
public class Cuaderno implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_cuaderno")
    private Integer idCuaderno;

    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;

    @Basic(optional = false)
    @Column(name = "anio")
    private int anio;

    @Column(name = "observaciones")
    private String observaciones;

    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id_persona")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList"})
    private Persona fkIdPersonaEscribano;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = ConstantesPersistencia.VERSION_INICIAL;

    public Integer getIdCuaderno() {
        return idCuaderno;
    }

    public void setIdCuaderno(Integer idCuaderno) {
        this.idCuaderno = idCuaderno;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Persona getFkIdPersonaEscribano() {
        return fkIdPersonaEscribano;
    }

    public void setFkIdPersonaEscribano(Persona fkIdPersonaEscribano) {
        this.fkIdPersonaEscribano = fkIdPersonaEscribano;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return idCuaderno != null ? idCuaderno.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Cuaderno other)) {
            return false;
        }
        return idCuaderno != null && idCuaderno.equals(other.idCuaderno);
    }

    @Override
    public String toString() {
        return "Cuaderno[ idCuaderno=" + idCuaderno + " ][ numero=" + numero + " ][ anio=" + anio + " ]";
    }
}
