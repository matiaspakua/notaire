/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoProcedure;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.Date;
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
@Table(name = "escrituras")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Escritura.findAll", query = "SELECT e FROM Deed e"),
            @NamedQuery(name = "Escritura.findByIdEscritura", query = "SELECT e FROM Deed e WHERE e.idDeed = :idEscritura"),
            @NamedQuery(name = "Escritura.findByNumero", query = "SELECT e FROM Deed e WHERE e.number = :numero"),
            @NamedQuery(name = "Escritura.findByFechaEscrituracion", query = "SELECT e FROM Deed e WHERE e.dateDeedrecording = :fechaEscrituracion"),
            @NamedQuery(name = "Escritura.findByFechaInscripcion", query = "SELECT e FROM Deed e WHERE e.dateRegistration = :fechaInscripcion")
        })
public class Deed implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "fecha_escrituracion")
    @Temporal(TemporalType.DATE)
    private Date dateDeedrecording;
    @Column(name = "fecha_inscripcion")
    @Temporal(TemporalType.DATE)
    private Date dateRegistration;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_escritura")
    private Integer idDeed;
    @Basic(optional = false)
    @Column(name = "numero")
    private int number;
    @Column(name = "cuerpo")
    private String body;
    @Basic(optional = false)
    @Column(name = "estado")
    private String status = BusinessConstants.DeedSINFIRMAR;
    @Column(name = "matricula_inscripcion")
    private String registrationEntryNumber;
    @Column(name = "observaciones")
    private String notes;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "fkIdDeed", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"fkIdEscritura"})
    private List<Folio> folioList = null;
    /** Write-only input: id of the folio to link on creation. Not persisted on this entity. */
    @jakarta.persistence.Transient
    private Integer idFolio;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "fkIdDeed", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"fkIdEscritura"})
    private List<Procedure> procedureList = null;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdDeed", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"fkIdEscritura"})
    private List<Testimony> testimonyList = null;

    public Deed()
    {
    }

    public Deed(Integer idDeed)
    {
        this.idDeed = idDeed;
    }

    public Deed(Integer idDeed, int number, Date dateDeedrecording, String body, String status)
    {
        this.idDeed = idDeed;
        this.number = number;
        this.dateDeedrecording = dateDeedrecording;
        this.body = body;
        this.status = status;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idDeed;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idDeed == null || idDeed.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdDeed()
    {
        return idDeed;
    }

    public void setIdDeed(Integer idDeed)
    {
        this.idDeed = idDeed;
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

    public Date getDateDeedrecording()
    {
        return dateDeedrecording;
    }

    public void setDateDeedrecording(Date dateDeedrecording)
    {
        this.dateDeedrecording = dateDeedrecording;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getRegistrationEntryNumber()
    {
        return registrationEntryNumber;
    }

    public void setRegistrationEntryNumber(String registrationEntryNumber)
    {
        this.registrationEntryNumber = registrationEntryNumber;
    }

    public Date getDateRegistration()
    {
        return dateRegistration;
    }

    public void setDateRegistration(Date dateRegistration)
    {
        this.dateRegistration = dateRegistration;
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

    @XmlTransient
    @JsonIgnore
    public List<Procedure> getProcedureList()
    {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList)
    {
        this.procedureList = procedureList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Testimony> getTestimonyList()
    {
        return testimonyList;
    }

    public void setTestimonyList(List<Testimony> testimonyList)
    {
        this.testimonyList = testimonyList;
    }

    public void setAtributos(DtoDeed miDtoDeed)
    {
        if (miDtoDeed != null)
        {
            if (idDeed != null)
            {
                idDeed = miDtoDeed.getIdDeed();
            }

            number = miDtoDeed.getNumber();
            dateDeedrecording = miDtoDeed.getDateDeedrecording();

            if (miDtoDeed.getFolios() != null)
            {
                folioList = new ArrayList<>();
                for (Iterator<DtoFolio> it = miDtoDeed.getFolios().iterator(); it.hasNext();)
                {
                    DtoFolio dtoFolio = it.next();
                    Folio miFolio = new Folio();

                    miFolio.setAtributos(dtoFolio);

                    folioList.add(miFolio);
                }
            }

            if (miDtoDeed.getProcedures() != null)
            {
                procedureList = new ArrayList<>();
                for (Iterator<DtoProcedure> it = miDtoDeed.getProcedures().iterator(); it.hasNext();)
                {
                    DtoProcedure dtoProcedure = it.next();
                    Procedure miProcedure = new Procedure();

                    miProcedure.setIdProcedure(dtoProcedure.getIdProcedure());
                    procedureList.add(miProcedure);
                }
            }
            body = miDtoDeed.getBody();
            status = miDtoDeed.getStatus();
            version = miDtoDeed.getVersion();
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoDeed getDto()
    {
        DtoDeed miDtoDeed = new DtoDeed();

        miDtoDeed.setIdDeed(idDeed);
        miDtoDeed.setDateDeedrecording(dateDeedrecording);
        miDtoDeed.setNumber(number);
        miDtoDeed.setBody(body);
        miDtoDeed.setStatus(status);
        miDtoDeed.setRegistrationEntryNumber(registrationEntryNumber);
        miDtoDeed.setDateRegistration(dateRegistration);

        miDtoDeed.setVersion(version);

        if (folioList != null && !folioList.isEmpty())
        {

            for (Iterator<Folio> it = folioList.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                DtoFolio miDtoFolio = folio.getDto();

                miDtoDeed.getFolios().add(miDtoFolio);
            }
        } else
        {
            miDtoDeed.setFolios(null);
        }

        if (procedureList != null && !procedureList.isEmpty())
        {
            for (Iterator<Procedure> it = procedureList.iterator(); it.hasNext();)
            {
                Procedure procedure = it.next();
                DtoProcedure miDtoProcedure = procedure.getDto();

                miDtoDeed.getProcedures().add(miDtoProcedure);
            }
        } else
        {
            miDtoDeed.setProcedures(null);
        }

        return miDtoDeed;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idDeed != null ? idDeed.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Deed))
        {
            return false;
        }
        Deed other = (Deed) object;
        if ((this.idDeed == null && other.idDeed != null) || (this.idDeed != null && !this.idDeed.equals(other.idDeed)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Escritura[ idEscritura=" + idDeed + " ]"
                + "[ numero=" + number
                + ", fecha escrituracion: " + this.dateDeedrecording
                + ", fecha inscripcion: " + this.dateRegistration
                + ", Estado: " + this.status + " ]";
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
