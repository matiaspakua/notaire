/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.Collection;
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
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un estado de gestion (en un momento del tiempo dado).
 *
 * @author User
 */
@Entity
@Table(name = "estados_de_gestion")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "EstadoDeGestion.findAll", query = "SELECT e FROM ManagementStatus e"),
        @NamedQuery(name = "EstadoDeGestion.findByIdEstadoGestion", query = "SELECT e FROM ManagementStatus e WHERE e.idManagementStatus = :idEstadoGestion")
})
public class ManagementStatus implements Serializable, Persistable<Integer> {

    @OneToMany(mappedBy = "fkIdManagementStatus")
    private Collection<DeedManagement> deedManagementCollection;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_estado_gestion")
    private Integer idManagementStatus;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Column(name = "observaciones")
    private String notes;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdManagementStatus", fetch = FetchType.EAGER)
    private java.util.Set<History> historyList;

    /**
     * Constructor por default para estado de gestion. Asigna al ID el valor de
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public ManagementStatus() {
        this.idManagementStatus = BusinessConstants.ID_OBJETO_NO_VALIDO;
    }

    public ManagementStatus(Integer idManagementStatus) {
        this.idManagementStatus = idManagementStatus;
    }

    public ManagementStatus(Integer idManagementStatus, String name) {
        this.idManagementStatus = idManagementStatus;
        this.name = name;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idManagementStatus;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idManagementStatus == null || idManagementStatus.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdManagementStatus() {
        return idManagementStatus;
    }

    public void setIdManagementStatus(Integer idManagementStatus) {
        this.idManagementStatus = idManagementStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @XmlTransient
    @JsonIgnore
    public java.util.Set<History> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(java.util.Set<History> historyList) {
        this.historyList = historyList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idManagementStatus != null ? idManagementStatus.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ManagementStatus)) {
            return false;
        }
        ManagementStatus other = (ManagementStatus) object;
        if ((this.idManagementStatus == null && other.idManagementStatus != null)
                || (this.idManagementStatus != null && !this.idManagementStatus.equals(other.idManagementStatus))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EstadoDeGestion[ idEstadoGestion=" + idManagementStatus + " ]"
                + "[ nombre=" + name + " ]";
    }

    public void setAtributo(DtoManagementStatus miDto) throws DtoInvalidoException {
        if (miDto.isValido() == Boolean.TRUE) {
            this.setIdManagementStatus(miDto.getIdManagementStatus());
            this.setName(miDto.getName());
            this.setNotes(miDto.getNotes());
            this.version = miDto.getVersion();
        } else {
            throw new DtoInvalidoException("Dto invalido");
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoManagementStatus getDto() throws NullPointerException {
        DtoManagementStatus miDto = new DtoManagementStatus();

        miDto.setIdManagementStatus(this.getIdManagementStatus());
        miDto.setName(this.getName());
        miDto.setNotes(this.getNotes());
        miDto.setVersion(this.getVersion());

        return miDto;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<DeedManagement> getDeedManagementCollection() {
        return deedManagementCollection;
    }

    public void setDeedManagementCollection(Collection<DeedManagement> deedManagementCollection) {
        this.deedManagementCollection = deedManagementCollection;
    }
}
