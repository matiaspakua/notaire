/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoDocumentType;
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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author User
 */
@Entity
@Table(name = "tipos_de_documento")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "TipoDeDocumento.findAll", query = "SELECT t FROM DocumentType t"),
            @NamedQuery(name = "TipoDeDocumento.findByIdTipoDocumento", query = "SELECT t FROM DocumentType t WHERE t.idDocumentType = :idTipoDocumento"),
            @NamedQuery(name = "TipoDeDocumento.findByVence", query = "SELECT t FROM DocumentType t WHERE t.expires = :vence"),
            @NamedQuery(name = "TipoDeDocumento.findByDiasVencimiento", query = "SELECT t FROM DocumentType t WHERE t.dueDays = :diasVencimiento"),
            @NamedQuery(name = "TipoDeDocumento.findByNombre", query = "SELECT t FROM DocumentType t WHERE t.name = :nombre")
        })
public class DocumentType implements Serializable, Persistable<Integer>
{

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdDocumentType")
    private Collection<SubmittedDocument> submittedDocumentCollection;
    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean enabled;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_documento")
    private Integer idDocumentType;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Basic(optional = false)
    @Column(name = "vence")
    private boolean expires;
    @Column(name = "dias_vencimiento")
    private Integer dueDays;
    @Basic(optional = false)
    @Column(name = "quien_entrega")
    private String deliveredBy;
    @Basic(optional = false)
    @Column(name = "devuelto")
    private boolean returned;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "documentType", fetch = FetchType.EAGER)
    private List<ProcedureTemplate> procedureTemplateList = new ArrayList<>();

    public DocumentType()
    {
    }

    public DocumentType(Integer idDocumentType)
    {
        this.idDocumentType = idDocumentType;
    }

    public DocumentType(Integer idDocumentType, String name, boolean expires, String deliveredBy)
    {
        this.idDocumentType = idDocumentType;
        this.name = name;
        this.expires = expires;
        this.deliveredBy = deliveredBy;
        this.returned = false;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idDocumentType;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idDocumentType == null || idDocumentType.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdDocumentType()
    {
        return idDocumentType;
    }

    public void setIdDocumentType(Integer idDocumentType)
    {
        this.idDocumentType = idDocumentType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean getExpires()
    {
        return expires;
    }

    public void setExpires(boolean expires)
    {
        this.expires = expires;
    }

    public Integer getDueDays()
    {
        return dueDays;
    }

    public void setDueDays(Integer dueDays)
    {
        this.dueDays = dueDays;
    }

    public String getDeliveredBy()
    {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy)
    {
        this.deliveredBy = deliveredBy;
    }

    public boolean getReturned()
    {
        return returned;
    }

    public void setReturned(boolean returned)
    {
        this.returned = returned;
    }

    @XmlTransient
    @JsonIgnore
    public List<ProcedureTemplate> getProcedureTemplateList()
    {
        return procedureTemplateList;
    }

    public void setProcedureTemplateList(List<ProcedureTemplate> procedureTemplateList)
    {
        this.procedureTemplateList = procedureTemplateList;
    }

    public void setAtributos(DtoDocumentType miDto)
    {
        if (miDto.getIdDocumentType() != null)
        {
            this.idDocumentType = miDto.getIdDocumentType();
        }

        this.name = miDto.getName();
        this.expires = miDto.isExpires();

        if (this.expires)
        {
            this.dueDays = miDto.getDueDays();
        } else
        {
            this.dueDays = null;
        }

        this.deliveredBy = miDto.getDeliveredBy();
        // Preserve current version when omitted; default habilitado to enabled.
        // Both are nullable in the DTO and previously NPE'd on update.
        if (miDto.getVersion() != null) {
            this.version = miDto.getVersion();
        }
        enabled = !Boolean.FALSE.equals(miDto.getEnabled());
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoDocumentType getDto()
    {
        DtoDocumentType miDto = new DtoDocumentType();

        miDto.setIdDocumentType(this.idDocumentType);
        miDto.setName(this.name);
        miDto.setExpires(this.expires);

        if (miDto.isExpires())
        {
            miDto.setDueDays(this.dueDays);
        }

        miDto.setDeliveredBy(this.deliveredBy);

        miDto.setVersion(version);
        miDto.setEnabled(enabled);

        return miDto;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idDocumentType != null ? idDocumentType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DocumentType))
        {
            return false;
        }
        DocumentType other = (DocumentType) object;
        if ((this.idDocumentType == null && other.idDocumentType != null) || (this.idDocumentType != null && !this.idDocumentType.equals(other.idDocumentType)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TipoDeDocumento[ idTipoDocumento=" + idDocumentType + " ]"
                + "[ nombre=" + name + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<SubmittedDocument> getSubmittedDocumentCollection()
    {
        return submittedDocumentCollection;
    }

    public void setSubmittedDocumentCollection(Collection<SubmittedDocument> submittedDocumentCollection)
    {
        this.submittedDocumentCollection = submittedDocumentCollection;
    }
}
