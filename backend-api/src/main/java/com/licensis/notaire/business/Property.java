/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Basic;
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
 * Clase que representa a un inmueble.
 *
 * @author juanca
 */
@Entity
@Table(name = "inmuebles")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Inmueble.findAll", query = "SELECT i FROM Property i"),
        @NamedQuery(name = "Inmueble.findByIdInmueble", query = "SELECT i FROM Property i WHERE i.idProperty = :idInmueble"),
        @NamedQuery(name = "Inmueble.findByNomenclatura", query = "SELECT i FROM Property i WHERE i.cadastralDesignation = :nomenclatura")
})
public class Property implements Serializable, Persistable<Integer> {

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_inmueble")
    private Integer idProperty;
    @Basic(optional = false)
    @Column(name = "nomenclatura")
    private String cadastralDesignation;
    @Column(name = "valuacion_fiscal")
    private Float fiscalAppraisal;
    @Basic(optional = false)
    @Column(name = "domicilio")
    private String address;
    @Column(name = "observaciones")
    private String notes;
    @Column(name = "matricula")
    private String registrationNumber;
    @Column(name = "tomo_folio_finca")
    private String volumeFolioLandRecord;
    @Column(name = "linderos")
    private String boundaries;
    @OneToMany(mappedBy = "fkIdProperty", fetch = FetchType.LAZY)
    private List<Procedure> procedureList;

    /**
     * Constructor por default de Inmueble. Inicializa el ID presupuesto segun el
     * campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO, y las listas internas.
     */
    public Property() {
        this.idProperty = BusinessConstants.ID_OBJETO_NO_VALIDO;
        this.procedureList = new ArrayList<>();
    }

    public Property(Integer idProperty) {
        this.idProperty = idProperty;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idProperty;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idProperty == null || idProperty.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdProperty() {
        return idProperty;
    }

    public void setIdProperty(Integer idProperty) {
        this.idProperty = idProperty;
    }

    public String getCadastralDesignation() {
        return cadastralDesignation;
    }

    public void setCadastralDesignation(String cadastralDesignation) {
        this.cadastralDesignation = cadastralDesignation;
    }

    public Float getFiscalAppraisal() {
        return fiscalAppraisal;
    }

    public void setFiscalAppraisal(Float fiscalAppraisal) {
        this.fiscalAppraisal = fiscalAppraisal;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVolumeFolioLandRecord() {
        return volumeFolioLandRecord;
    }

    public void setVolumeFolioLandRecord(String volumeFolioLandRecord) {
        this.volumeFolioLandRecord = volumeFolioLandRecord;
    }

    public String getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(String boundaries) {
        this.boundaries = boundaries;
    }

    @XmlTransient
    @JsonIgnore
    public List<Procedure> getProcedureList() {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList) {
        this.procedureList = procedureList;
    }

    public DtoProperty getDto() {
        DtoProperty miDto = new DtoProperty();

        miDto.setAddress(this.getAddress());
        miDto.setIdProperty(this.getIdProperty());
        miDto.setCadastralDesignation(this.getCadastralDesignation());
        miDto.setNotes(this.getNotes());
        miDto.setFiscalAppraisal(this.getFiscalAppraisal());
        miDto.setRegistrationNumber(this.getRegistrationNumber());
        miDto.setVolumeFolioLandRecord(this.getVolumeFolioLandRecord());
        miDto.setBoundaries(this.getBoundaries());

        return miDto;
    }

    public void setAtributos(DtoProperty miDtoProperty) {
        if (miDtoProperty.isValido()) {
            this.address = miDtoProperty.getAddress();

            if (miDtoProperty.getIdProperty() != null) {
                this.idProperty = miDtoProperty.getIdProperty();
            }

            this.cadastralDesignation = miDtoProperty.getCadastralDesignation();
            this.notes = miDtoProperty.getNotes();
            this.fiscalAppraisal = miDtoProperty.getFiscalAppraisal();
            this.registrationNumber = miDtoProperty.getRegistrationNumber();
            this.volumeFolioLandRecord = miDtoProperty.getVolumeFolioLandRecord();
            this.boundaries = miDtoProperty.getBoundaries();
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProperty != null ? idProperty.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Property)) {
            return false;
        }
        Property other = (Property) object;
        if ((this.idProperty == null && other.idProperty != null)
                || (this.idProperty != null && !this.idProperty.equals(other.idProperty))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Inmueble[ idInmueble=" + idProperty + " ]"
                + "[ nomenclatura=" + cadastralDesignation + " ]";
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
