/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.dto.DtoTestimony;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.Iterator;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "testimonios")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Testimonio.findAll", query = "SELECT t FROM Testimony t"),
            @NamedQuery(name = "Testimonio.findByIdTestimonio", query = "SELECT t FROM Testimony t WHERE t.idTestimony = :idTestimonio"),
            @NamedQuery(name = "Testimonio.findByNumero", query = "SELECT t FROM Testimony t WHERE t.number = :numero"),
            @NamedQuery(name = "Testimonio.findByEscritura", query = "SELECT t FROM Testimony t WHERE t.fkIdDeed.idDeed = :idEscritura"),
            @NamedQuery(name = "Testimonio.findByObservado", query = "SELECT t FROM Testimony t WHERE t.flagged = :observado")
        })
public class Testimony implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_testimonio")
    private Integer idTestimony;
    @Basic(optional = false)
    @Column(name = "numero")
    private int number;
    @Basic(optional = false)
    @Column(name = "observado")
    private boolean flagged;
    @Basic(optional = false)
    @Column(name = "verificado")
    private boolean verified;
    @Column(name = "observaciones")
    private String notes;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTestimony", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"fkIdTestimonio"})
    private List<TestimonyMovement> testimonyMovementList = new ArrayList<>();
    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"folioList", "tramiteList", "testimonioList"})
    private Deed fkIdDeed;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTestimony", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"fkIdTestimonio"})
    private List<Copy> copyList = new ArrayList<>();

    public Testimony()
    {
    }

    public Testimony(Integer idTestimony)
    {
        this.idTestimony = idTestimony;
    }

    public Testimony(Integer idTestimony, int number, boolean flagged)
    {
        this.idTestimony = idTestimony;
        this.number = number;
        this.flagged = flagged;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idTestimony;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idTestimony == null || idTestimony.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdTestimony()
    {
        return idTestimony;
    }

    public void setIdTestimony(Integer idTestimony)
    {
        this.idTestimony = idTestimony;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public boolean getFlagged()
    {
        return flagged;
    }

    public void setFlagged(boolean flagged)
    {
        this.flagged = flagged;
    }

    public boolean getVerified()
    {
        return verified;
    }

    public void setVerified(boolean verified)
    {
        this.verified = verified;
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
    public List<TestimonyMovement> getTestimonyMovementList()
    {
        return testimonyMovementList;
    }

    public void setTestimonyMovementList(List<TestimonyMovement> testimonyMovementList)
    {
        this.testimonyMovementList = testimonyMovementList;
    }

    public Deed getFkIdDeed()
    {
        return fkIdDeed;
    }

    public void setFkIdDeed(Deed fkIdDeed)
    {
        this.fkIdDeed = fkIdDeed;
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

    public void setAtributos(DtoTestimony miDto)
    {

        if (miDto.getIdTestimony() != null)
        {
            idTestimony = miDto.getIdTestimony();
        }

        number = miDto.getNumber();
        notes = miDto.getNotes();
        flagged = miDto.isFlagged();
        verified = miDto.isVerified();
        version = miDto.getVersion();

        if (miDto.getDeed() != null)
        {
            Deed miDeed = new Deed();
            miDeed.setIdDeed(miDto.getDeed().getIdDeed());

            fkIdDeed = miDeed;
        }

        if (miDto.getMovimientosTestimonios() != null && !miDto.getMovimientosTestimonios().isEmpty())
        {
            testimonyMovementList = new ArrayList<>();

            for (Iterator<DtoTestimonyMovement> it = miDto.getMovimientosTestimonios().iterator(); it.hasNext();)
            {
                DtoTestimonyMovement dtoTestimonyMovement = it.next();
                TestimonyMovement miTestimonyMovement = new TestimonyMovement();
                miTestimonyMovement.setAtributos(dtoTestimonyMovement);
                testimonyMovementList.add(miTestimonyMovement);
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoTestimony getDto()
    {
        DtoTestimony miDto = new DtoTestimony();

        miDto.setIdTestimony(idTestimony);
        miDto.setNumber(number);
        miDto.setNotes(notes);
        miDto.setFlagged(flagged);
        miDto.setVerified(verified);
        miDto.setVersion(version);

        if (fkIdDeed != null)
        {
            miDto.setDeed(fkIdDeed.getDto());
        }

        if (testimonyMovementList != null && !testimonyMovementList.isEmpty())
        {
            List<DtoTestimonyMovement> miLista = new ArrayList<>();

            for (Iterator<TestimonyMovement> it = testimonyMovementList.iterator(); it.hasNext();)
            {
                TestimonyMovement testimonyMovement = it.next();
                DtoTestimonyMovement miDtoTestimonyMovement = testimonyMovement.getDto();
                miLista.add(miDtoTestimonyMovement);
            }

            miDto.setMovimientosTestimonios(miLista);
        }

        return miDto;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idTestimony != null ? idTestimony.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Testimony))
        {
            return false;
        }
        Testimony other = (Testimony) object;
        if ((this.idTestimony == null && other.idTestimony != null) || (this.idTestimony != null && !this.idTestimony.equals(other.idTestimony)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        String deedId = fkIdDeed != null ? String.valueOf(fkIdDeed.getIdDeed()) : "null";
        return "Testimonio[ idTestimonio=" + idTestimony + " ]"
                + "[ numero=" + number + " ]"
                + "[ idEscritura=" + deedId + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
