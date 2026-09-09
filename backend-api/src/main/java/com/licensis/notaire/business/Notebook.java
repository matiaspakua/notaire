package com.licensis.notaire.business;

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
import org.springframework.data.domain.Persistable;

/**
 * A cuaderno groups exactly ten consecutive folios of the same registro
 * notarial, numbered correlatively per registro/año, so the protocol
 * carátula (CU80) can be emitted.
 */
@Entity
@Table(name = "cuadernos")
@NamedQueries({
    @NamedQuery(name = "Cuaderno.findAll", query = "SELECT c FROM Notebook c"),
    @NamedQuery(name = "Cuaderno.findByAnioAndEscribano",
            query = "SELECT c FROM Notebook c WHERE c.year = :anio AND c.fkIdNotaryPerson = :escribano")
})
public class Notebook implements Serializable, Persistable<Integer> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_cuaderno")
    private Integer idNotebook;

    @Basic(optional = false)
    @Column(name = "numero")
    private int number;

    @Basic(optional = false)
    @Column(name = "anio")
    private int year;

    @Column(name = "observaciones")
    private String notes;

    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList"})
    private Person fkIdNotaryPerson;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = ConstantesPersistencia.VersionINICIAL;
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idNotebook;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idNotebook == null || idNotebook.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdNotebook() {
        return idNotebook;
    }

    public void setIdNotebook(Integer idNotebook) {
        this.idNotebook = idNotebook;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Person getFkIdNotaryPerson() {
        return fkIdNotaryPerson;
    }

    public void setFkIdNotaryPerson(Person fkIdNotaryPerson) {
        this.fkIdNotaryPerson = fkIdNotaryPerson;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return idNotebook != null ? idNotebook.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Notebook other)) {
            return false;
        }
        return idNotebook != null && idNotebook.equals(other.idNotebook);
    }

    @Override
    public String toString() {
        return "Cuaderno[ idCuaderno=" + idNotebook + " ][ numero=" + number + " ][ anio=" + year + " ]";
    }
}
