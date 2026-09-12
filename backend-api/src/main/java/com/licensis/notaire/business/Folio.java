/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.jpa.ConstantesPersistencia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * <p>
 * REGLA DE NEGOCIO:
 * <p>
 *
 * <lo> <li> Estados posibles de un Folio: -Nuevo -Utilizado -Errose </li> </lo>
 *
 *
 * @author Tefi
 */
@Entity
@Table(name = "folios")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Folio.findAll", query = "SELECT f FROM Folio f"),
            @NamedQuery(name = "Folio.findByIdFolio", query = "SELECT f FROM Folio f WHERE f.idFolio = :idFolio"),
            @NamedQuery(name = "Folio.findByNumero", query = "SELECT f FROM Folio f WHERE f.number = :numero"),
            @NamedQuery(name = "Folio.findByAnio", query = "SELECT f FROM Folio f WHERE f.year = :anio"),
            @NamedQuery(name = "Folio.findByAnioAndRegistro", query = "SELECT f FROM Folio f WHERE f.year = :anio AND f.fkIdNotaryPerson.notaryRegistrationNumber =:registro")
        })
public class Folio implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = ConstantesPersistencia.VersionINICIAL;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "folio")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folio", "copia"})
    private Collection<FolioCopies> folioCopiesCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_folio")
    private Integer idFolio;
    @Basic(optional = false)
    @Column(name = "numero")
    private int number;
    @Basic(optional = false)
    @Column(name = "anio")
    private int year;
    @Basic(optional = false)
    @Column(name = "estado")
    private String status;
    @Column(name = "observaciones")
    private String notes;
    @JoinTable(name = "folios_copias", joinColumns =
    {
        @JoinColumn(name = "fk_id_folio", referencedColumnName = "id_folio")
    }, inverseJoinColumns =
    {
        @JoinColumn(name = "fk_id_copia", referencedColumnName = "id_copia")
    })
    @ManyToMany(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList", "foliosCopiasCollection"})
    private List<Copy> copyList;
    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList"})
    private Person fkIdNotaryPerson;
    @JoinColumn(name = "fk_id_tipo_folio", referencedColumnName = "id_tipo_folio")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList"})
    private FolioType fkIdFolioType;
    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura")
    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList", "tramiteList", "testimonioList"})
    private Deed fkIdDeed;
    @JoinColumn(name = "fk_id_cuaderno", referencedColumnName = "id_cuaderno")
    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList"})
    private Notebook fkIdNotebook;

    public Folio()
    {
        this.copyList = new ArrayList<>();
        this.folioCopiesCollection = new ArrayList<>();

    }

    public Folio(Integer idFolio)
    {
        this.idFolio = idFolio;
    }

    public Folio(Integer idFolio, int number, int year, String status)
    {
        this.idFolio = idFolio;
        this.number = number;
        this.year = year;
        this.status = status;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idFolio;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idFolio == null || idFolio.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdFolio()
    {
        return idFolio;
    }

    public void setIdFolio(Integer idFolio)
    {
        this.idFolio = idFolio;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    @XmlTransient
    @JsonIgnore
    public List<Copy> getCopyList()
    {
        return copyList;
    }

    public void setCopyList(List<Copy> copyList)
    {
        this.copyList = copyList;
    }

    public Person getFkIdNotaryPerson()
    {
        return fkIdNotaryPerson;
    }

    public void setFkIdNotaryPerson(Person fkIdNotaryPerson)
    {
        this.fkIdNotaryPerson = fkIdNotaryPerson;
    }

    public FolioType getFkIdFolioType()
    {
        return fkIdFolioType;
    }

    public void setFkIdFolioType(FolioType fkIdFolioType)
    {
        this.fkIdFolioType = fkIdFolioType;
    }

    public Deed getFkIdDeed()
    {
        return fkIdDeed;
    }

    public void setFkIdDeed(Deed fkIdDeed)
    {
        this.fkIdDeed = fkIdDeed;
    }

    public Notebook getFkIdNotebook()
    {
        return fkIdNotebook;
    }

    public void setFkIdNotebook(Notebook fkIdNotebook)
    {
        this.fkIdNotebook = fkIdNotebook;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idFolio != null ? idFolio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Folio))
        {
            return false;
        }
        Folio other = (Folio) object;
        if ((this.idFolio == null && other.idFolio != null) || (this.idFolio != null && !this.idFolio.equals(other.idFolio)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Folio[ idFolio=" + idFolio + " ]"
                + "[ numero=" + number + " ]"
                + "[ anio=" + year + " ]";
    }

    public void setAtributos(DtoFolio unDtoFolio)
    {
        if (unDtoFolio.isValido())
        {
            this.setIdFolio(unDtoFolio.getIdFolio());
            this.setNumber(unDtoFolio.getNumber());
            this.setYear(unDtoFolio.getYear());
            this.setStatus(unDtoFolio.getStatus());
            this.setNotes(unDtoFolio.getNotes());
            this.setVersion(unDtoFolio.getVersion());
            if (unDtoFolio.getDeed() != null)
            {
                this.setFkIdDeed(new Deed(unDtoFolio.getDeed().getIdDeed()));
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoFolio getDto()
    {
        DtoFolio miDtoFolio = new DtoFolio();

        miDtoFolio.setIdFolio(this.idFolio);
        miDtoFolio.setNumber(this.number);
        miDtoFolio.setStatus(this.status);
        miDtoFolio.setNotes(this.notes);
        miDtoFolio.setYear(this.year);
        miDtoFolio.setVersion(this.getVersion());

        if (this.getFkIdNotaryPerson() != null)
        {
            DtoPerson miPerson = new DtoPerson();
            miPerson.setId(fkIdNotaryPerson.getPersonId());
            miPerson.setNotaryRegistrationNumber(fkIdNotaryPerson.getNotaryRegistrationNumber());

            miDtoFolio.setPersonNotary(miPerson);
        }

        miDtoFolio.setTiposDeFolio(this.fkIdFolioType.getDto());

        if (this.fkIdDeed != null)
        {
            // Not fkIdEscritura.getDto(): that walks Escritura's lazy folioList, which
            // includes this same Folio, and would recurse back into Folio.getDto().
            DtoDeed miDtoDeed = new DtoDeed();
            miDtoDeed.setIdDeed(this.fkIdDeed.getIdDeed());
            miDtoDeed.setNumber(this.fkIdDeed.getNumber());
            miDtoDeed.setStatus(this.fkIdDeed.getStatus());
            miDtoFolio.setDeed(miDtoDeed);
        }

        return miDtoFolio;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<FolioCopies> getFolioCopiesCollection()
    {
        return folioCopiesCollection;
    }

    public void setFolioCopiesCollection(Collection<FolioCopies> folioCopiesCollection)
    {
        this.folioCopiesCollection = folioCopiesCollection;
    }
}
