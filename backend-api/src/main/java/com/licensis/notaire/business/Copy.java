/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoCopy;
import com.licensis.notaire.dto.DtoTestimony;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.Collection;
import java.util.Date;
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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "copias")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Copia.findAll", query = "SELECT c FROM Copy c"),
            @NamedQuery(name = "Copia.findByIdCopia", query = "SELECT c FROM Copy c WHERE c.idCopy = :idCopia"),
            @NamedQuery(name = "Copia.findByNumero", query = "SELECT c FROM Copy c WHERE c.number = :numero"),
            @NamedQuery(name = "Copia.findByFechaImpresion", query = "SELECT c FROM Copy c WHERE c.datePrinting = :fechaImpresion"),
            @NamedQuery(name = "Copia.findByTestimonio", query = "SELECT c FROM Copy c WHERE c.fkIdTestimony.idTestimony = :idTestimonio"),
            @NamedQuery(name = "Copia.findByFechaRetiro", query = "SELECT c FROM Copy c WHERE c.dateWithdrawal = :fechaRetiro")
        })
public class Copy implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "fecha_impresion")
    @Temporal(TemporalType.DATE)
    private Date datePrinting;
    @Column(name = "fecha_retiro")
    @Temporal(TemporalType.DATE)
    private Date dateWithdrawal;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "copy")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"copia"})
    private Collection<FolioCopies> folioCopiesCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_copia")
    private Integer idCopy;
    @Basic(optional = false)
    @Column(name = "numero")
    private int number;
    @Column(name = "observaciones")
    private String notes;
    @ManyToMany(mappedBy = "copyList", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"copiaList"})
    private List<Folio> folioList;
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Person fkIdPerson;
    @JoinColumn(name = "fk_id_testimonio", referencedColumnName = "id_testimonio")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"copiaList", "movimientoTestimonioList"})
    private Testimony fkIdTestimony;

    public Copy()
    {
    }

    public Copy(Integer idCopy)
    {
        this.idCopy = idCopy;
    }

    public Copy(Integer idCopy, int number, Date datePrinting)
    {
        this.idCopy = idCopy;
        this.number = number;
        this.datePrinting = datePrinting;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idCopy;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idCopy == null || idCopy.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdCopy()
    {
        return idCopy;
    }

    public void setIdCopy(Integer idCopy)
    {
        this.idCopy = idCopy;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public Date getDatePrinting()
    {
        return datePrinting;
    }

    public void setDatePrinting(Date datePrinting)
    {
        this.datePrinting = datePrinting;
    }

    public Date getDateWithdrawal()
    {
        return dateWithdrawal;
    }

    public void setDateWithdrawal(Date dateWithdrawal)
    {
        this.dateWithdrawal = dateWithdrawal;
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
    public List<Folio> getFolioList()
    {
        return folioList;
    }

    public void setFolioList(List<Folio> folioList)
    {
        this.folioList = folioList;
    }

    public Person getFkIdPerson()
    {
        return fkIdPerson;
    }

    public void setFkIdPerson(Person fkIdPerson)
    {
        this.fkIdPerson = fkIdPerson;
    }

    public Testimony getFkIdTestimony()
    {
        return fkIdTestimony;
    }

    public void setFkIdTestimony(Testimony fkIdTestimony)
    {
        this.fkIdTestimony = fkIdTestimony;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idCopy != null ? idCopy.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Copy))
        {
            return false;
        }
        Copy other = (Copy) object;
        if ((this.idCopy == null && other.idCopy != null) || (this.idCopy != null && !this.idCopy.equals(other.idCopy)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Copia[ idCopia=" + idCopy + " ]";
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

    public DtoCopy getDto()
    {
        DtoCopy miDto = new DtoCopy();

        miDto.setIdCopy(idCopy);
        miDto.setDatePrinting(datePrinting);
        miDto.setDateWithdrawal(dateWithdrawal);
        miDto.setNumber(number);
        miDto.setNotes(notes);

        if (fkIdTestimony != null)
        {
            DtoTestimony miDtoTestimony;
            miDtoTestimony = fkIdTestimony.getDto();
            miDto.setTestimony(miDtoTestimony);
        }

        miDto.setVersion(version);

        return miDto;
    }

    public void setAtributos(DtoCopy miDto)
    {

        if (miDto.getIdCopy() != null)
        {
            idCopy = miDto.getIdCopy();
        }

        version = miDto.getVersion();
        datePrinting = miDto.getDatePrinting();
        dateWithdrawal = miDto.getDateWithdrawal();
        number = miDto.getNumber();
        notes = miDto.getNotes();

        if (miDto.getTestimony() != null)
        {
            fkIdTestimony = new Testimony(miDto.getTestimony().getIdTestimony());
        }
    }
}
