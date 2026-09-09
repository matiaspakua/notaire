/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.dto.DtoTestimony;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.Date;
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "movimientos_testimonio")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "MovimientoTestimonio.findAll", query = "SELECT m FROM TestimonyMovement m"),
            @NamedQuery(name = "MovimientoTestimonio.findByIdMovimientoTestimonio", query = "SELECT m FROM TestimonyMovement m WHERE m.idTestimonyMovement = :idMovimientoTestimonio"),
            @NamedQuery(name = "MovimientoTestimonio.findByFechaIngreso", query = "SELECT m FROM TestimonyMovement m WHERE m.dateEntry = :fechaIngreso"),
            @NamedQuery(name = "MovimientoTestimonio.findByFechaSalida", query = "SELECT m FROM TestimonyMovement m WHERE m.dateExit = :fechaSalida"),
            @NamedQuery(name = "MovimientoTestimonio.findByFechaInscripcion", query = "SELECT m FROM TestimonyMovement m WHERE m.dateRegistration = :fechaInscripcion"),
            @NamedQuery(name = "MovimientoTestimonio.findByInscripta", query = "SELECT m FROM TestimonyMovement m WHERE m.registered = :inscripta"),
            @NamedQuery(name = "MovimientoTestimonio.findByTestimonio", query = "SELECT m FROM TestimonyMovement m WHERE m.fkIdTestimony.idTestimony = :idTestimonio"),
            @NamedQuery(name = "MovimientoTestimonio.findByNumeroCarton", query = "SELECT m FROM TestimonyMovement m WHERE m.cardNumber = :numeroCarton")
        })
public class TestimonyMovement implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "fecha_ingreso")
    @Temporal(TemporalType.DATE)
    private Date dateEntry;
    @Column(name = "fecha_salida")
    @Temporal(TemporalType.DATE)
    private Date dateExit;
    @Column(name = "fecha_inscripcion")
    @Temporal(TemporalType.DATE)
    private Date dateRegistration;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_movimiento_testimonio")
    private Integer idTestimonyMovement;
    @Basic(optional = false)
    @Column(name = "inscripta")
    private boolean registered;
    @Basic(optional = false)
    @Column(name = "numero_carton")
    private int cardNumber;
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_testimonio", referencedColumnName = "id_testimonio")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Testimony fkIdTestimony;

    public TestimonyMovement()
    {
    }

    public TestimonyMovement(Integer idTestimonyMovement)
    {
        this.idTestimonyMovement = idTestimonyMovement;
    }

    public TestimonyMovement(Integer idTestimonyMovement, Date dateEntry, boolean registered, int cardNumber)
    {
        this.idTestimonyMovement = idTestimonyMovement;
        this.dateEntry = dateEntry;
        this.registered = registered;
        this.cardNumber = cardNumber;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idTestimonyMovement;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idTestimonyMovement == null || idTestimonyMovement.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdTestimonyMovement()
    {
        return idTestimonyMovement;
    }

    public void setIdTestimonyMovement(Integer idTestimonyMovement)
    {
        this.idTestimonyMovement = idTestimonyMovement;
    }

    public Date getDateEntry()
    {
        return dateEntry;
    }

    public void setDateEntry(Date dateEntry)
    {
        this.dateEntry = dateEntry;
    }

    public Date getDateExit()
    {
        return dateExit;
    }

    public void setDateExit(Date dateExit)
    {
        this.dateExit = dateExit;
    }

    public Date getDateRegistration()
    {
        return dateRegistration;
    }

    public void setDateRegistration(Date dateRegistration)
    {
        this.dateRegistration = dateRegistration;
    }

    public boolean getRegistered()
    {
        return registered;
    }

    public void setRegistered(boolean registered)
    {
        this.registered = registered;
    }

    public int getCardNumber()
    {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber)
    {
        this.cardNumber = cardNumber;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Testimony getTestimony()
    {
        return fkIdTestimony;
    }

    public void setTestimony(Testimony fkIdTestimony)
    {
        this.fkIdTestimony = fkIdTestimony;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idTestimonyMovement != null ? idTestimonyMovement.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TestimonyMovement))
        {
            return false;
        }
        TestimonyMovement other = (TestimonyMovement) object;
        if ((this.idTestimonyMovement == null && other.idTestimonyMovement != null) || (this.idTestimonyMovement != null && !this.idTestimonyMovement.equals(other.idTestimonyMovement)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MovimientoTestimonio[ idMovimientoTestimonio=" + idTestimonyMovement + " ]"
                + "[ idTestimonio=" + fkIdTestimony.getIdTestimony() + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoTestimonyMovement getDto()
    {
        DtoTestimonyMovement miDto = new DtoTestimonyMovement();

        miDto.setIdTestimonyMovement(idTestimonyMovement);
        miDto.setDateEntry(dateEntry);
        miDto.setDateRegistration(dateRegistration);
        miDto.setDateExit(dateExit);
        miDto.setRegistered(registered);
        miDto.setCardNumber(cardNumber);
        miDto.setNotes(notes);
        miDto.setVersion(version);

        DtoTestimony miDtoTestimony = new DtoTestimony();
        miDtoTestimony.setIdTestimony(fkIdTestimony.getIdTestimony());
        miDto.setTestimony(miDtoTestimony);

        return miDto;
    }

    public void setAtributos(DtoTestimonyMovement miDto)
    {

        if (miDto.getIdTestimonyMovement() != null)
        {
            idTestimonyMovement = miDto.getIdTestimonyMovement();
        }

        dateEntry = miDto.getDateEntry();
        dateRegistration = miDto.getDateRegistration();
        dateExit = miDto.getDateExit();
        registered = miDto.isRegistered();
        cardNumber = miDto.getCardNumber();
        notes = miDto.getNotes();
        version = miDto.getVersion();

        if (miDto.getTestimony() != null)
        {
            fkIdTestimony = new Testimony(miDto.getTestimony().getIdTestimony());
        }
    }
}
