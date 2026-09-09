/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
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
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un tipo de folio en particular que puede ser para el protocolo: principal,
 * auxiliar, etc.
 * <p>
 * REGLA DE NEGOCIO<p>
 * <li> Los tipos de folios no se pueden eliminar (debido a
 * que se tiene que mantener un historia de los mismos), por lo tanto, solo se le puede cambiar el
 * estado a "INHABILITADO". </li>
 */
@Entity
@Table(name = "tipos_de_folio")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "TipoDeFolio.findAll", query = "SELECT t FROM FolioType t"),
            @NamedQuery(name = "TipoDeFolio.findByIdTipoFolio", query = "SELECT t FROM FolioType t WHERE t.idFolioType = :idTipoFolio")
        })
public class FolioType implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_folio")
    private Integer idFolioType;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Column(name = "observaciones")
    private String notes;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdFolioType", fetch = FetchType.EAGER)
    private List<Folio> folioList;
    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean enabled;
    @Basic(optional = false)
    @Column(name = "es_auxiliar")
    private boolean isAuxiliary;

    public FolioType()
    {
    }

    public FolioType(String nameFolioType)
    {
        this.name = nameFolioType;
    }

    public FolioType(Integer idFolioType)
    {
        this.idFolioType = idFolioType;
    }

    public FolioType(Integer idFolioType, String name)
    {
        this.idFolioType = idFolioType;
        this.name = name;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idFolioType;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idFolioType == null || idFolioType.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdFolioType()
    {
        return idFolioType;
    }

    public void setIdFolioType(Integer idFolioType)
    {
        this.idFolioType = idFolioType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idFolioType != null ? idFolioType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FolioType))
        {
            return false;
        }
        FolioType other = (FolioType) object;
        if ((this.idFolioType == null && other.idFolioType != null) || (this.idFolioType != null && !this.idFolioType.equals(other.idFolioType)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TipoDeFolio[ idTipoFolio=" + idFolioType + " ]"
                + "[ nombre=" + name + " ]";
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoFolioType getDto()
    {
        DtoFolioType miDtoFolioType = new DtoFolioType();

        miDtoFolioType.setIdFolioType(this.getIdFolioType());
        miDtoFolioType.setName(this.getName());
        miDtoFolioType.setNotes(this.getNotes());
        miDtoFolioType.setEnabled(this.enabled);
        miDtoFolioType.setIsAuxiliary(this.isAuxiliary);
        miDtoFolioType.setVersion(this.getVersion());

        return miDtoFolioType;

    }

    public void setAtributos(DtoFolioType dtoFolioType) throws DtoInvalidoException
    {
        if (dtoFolioType.isValido())
        {
            this.setIdFolioType(dtoFolioType.getIdFolioType());
            this.setName(dtoFolioType.getName());
            this.setNotes(dtoFolioType.getNotes());
            this.setEnabled(dtoFolioType.isEnabled());
            this.setIsAuxiliary(dtoFolioType.isIsAuxiliary());
            this.setVersion(dtoFolioType.getVersion());
        } else
        {
            throw new DtoInvalidoException("El dto indicado no es valido.");
        }

    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isIsAuxiliary()
    {
        return isAuxiliary;
    }

    public void setIsAuxiliary(boolean isAuxiliary)
    {
        this.isAuxiliary = isAuxiliary;
    }
}
