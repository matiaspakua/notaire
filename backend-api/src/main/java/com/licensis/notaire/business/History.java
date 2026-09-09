/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoHistory;
import java.io.Serializable;
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
import org.springframework.data.domain.Persistable;

/**
 * Clase que representa el historial (registro de cambios de estado) de una gestion de escritura.
 * Cada instancia del historial de una misma gestion de escritura, representa un cambio de estado de
 * la gestion.
 *
 * @author juanca
 */
@Entity
@Table(name = "historial")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Historial.findAll", query = "SELECT h FROM History h"),
            @NamedQuery(name = "Historial.findByIdHistorial", query = "SELECT h FROM History h WHERE h.idHistory = :idHistorial"),
            @NamedQuery(name = "Historial.findByIdGestion", query = "SELECT h FROM History h WHERE h.fkIdManagement.idManagement = :idGestion"),
            @NamedQuery(name = "Historial.findByFecha", query = "SELECT h FROM History h WHERE h.date = :fecha")
        })
public class History implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_historial")
    private Integer idHistory;
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_estado_gestion", referencedColumnName = "id_estado_gestion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private ManagementStatus fkIdManagementStatus;
    @JoinColumn(name = "fk_id_gestion", referencedColumnName = "id_gestion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private DeedManagement fkIdManagement;

    /**
     * Constructor por default para historial. Asigna al ID el valor de
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public History()
    {
        this.idHistory = BusinessConstants.ID_OBJETO_NO_VALIDO;
    }

    public History(Integer idHistory)
    {
        this.idHistory = idHistory;
    }

    public History(Integer idHistory, Date date)
    {
        this.idHistory = idHistory;
        this.date = date;
    }

    public Integer getIdHistory()
    {
        return idHistory;
    }

    public void setIdHistory(Integer idHistory)
    {
        this.idHistory = idHistory;
    }

    @Override
    public Integer getId()
    {
        return idHistory;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 — indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew()
    {
        return idHistory == null || idHistory.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public ManagementStatus getFkIdManagementStatus()
    {
        return fkIdManagementStatus;
    }

    public void setFkIdManagementStatus(ManagementStatus fkIdManagementStatus)
    {
        this.fkIdManagementStatus = fkIdManagementStatus;
    }

    public DeedManagement getFkIdManagement()
    {
        return fkIdManagement;
    }

    public void setFkIdManagement(DeedManagement fkIdManagement)
    {
        this.fkIdManagement = fkIdManagement;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (getIdHistory() != null ? getIdHistory().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof History))
        {
            return false;
        }
        History other = (History) object;
        if ((this.getIdHistory() == null && other.getIdHistory() != null) || (this.getIdHistory() != null && !this.idHistory.equals(other.idHistory)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Historial[ idHistorial=" + getIdHistory() + " ]";
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoHistory getDto()
    {
        DtoHistory dto = new DtoHistory();

        dto.setIdHistory(this.idHistory);
        dto.setVersion(this.version);
        dto.setDate(this.date);
        dto.setGestionesDeEscrituras(this.getFkIdManagement().getDto());
        dto.setEstadosDeManagement(this.fkIdManagementStatus.getDto());
        dto.setNotes(this.notes);

        return dto;
    }

    public void setAtributos(DtoHistory dto)
    {
    }
}
