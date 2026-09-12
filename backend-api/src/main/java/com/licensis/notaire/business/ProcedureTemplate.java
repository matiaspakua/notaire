/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoProcedureTemplate;
import java.io.Serializable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author User
 */
@Entity
@Table(name = "plantilla_tramites")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "PlantillaTramite.findAll", query = "SELECT p FROM ProcedureTemplate p"),
            @NamedQuery(name = "PlantillaTramite.findByFkIdTipoTramite", query = "SELECT p FROM ProcedureTemplate p WHERE p.procedureTemplatePK.fkIdProcedureType = :fkIdTipoTramite"),
            @NamedQuery(name = "PlantillaTramite.findByFkIdTipoDocumento", query = "SELECT p FROM ProcedureTemplate p WHERE p.procedureTemplatePK.fkIdDocumentType = :fkIdTipoDocumento")
        })
public class ProcedureTemplate implements Serializable, Persistable<ProcedureTemplatePK>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ProcedureTemplatePK procedureTemplatePK;
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ProcedureType procedureType;
    @JoinColumn(name = "fk_id_tipo_documento", referencedColumnName = "id_tipo_documento", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private DocumentType documentType;
    @Transient
    private boolean isNewEntity = true;

    // Sets by Spring Data JPA's isNew() default heuristic for entities whose @EmbeddedId
    // is client-assigned (never null), so id-nullness cannot signal "new" the way it does
    // for @GeneratedValue entities. A transient flag flipped by these lifecycle callbacks
    // is the correct, standard Spring Data pattern for this case.
    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public ProcedureTemplatePK getId() {
        return procedureTemplatePK;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return isNewEntity;
    }


    public ProcedureTemplate()
    {
    }

    public ProcedureTemplate(ProcedureTemplatePK procedureTemplatePK)
    {
        this.procedureTemplatePK = procedureTemplatePK;
    }

    public ProcedureTemplate(int fkIdProcedureType, int fkIdDocumentType)
    {
        this.procedureTemplatePK = new ProcedureTemplatePK(fkIdProcedureType, fkIdDocumentType);
    }

    public ProcedureTemplatePK getProcedureTemplatePK()
    {
        return procedureTemplatePK;
    }

    public void setProcedureTemplatePK(ProcedureTemplatePK procedureTemplatePK)
    {
        this.procedureTemplatePK = procedureTemplatePK;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public ProcedureType getProcedureType()
    {
        return procedureType;
    }

    public void setProcedureType(ProcedureType procedureType)
    {
        this.procedureType = procedureType;
    }

    public DocumentType getDocumentType()
    {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType)
    {
        this.documentType = documentType;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoProcedureTemplate getDto()
    {
        DtoProcedureTemplate miDto = new DtoProcedureTemplate();

        miDto.setNotes(notes);
        miDto.setTiposDeDocument(documentType.getDto());
        miDto.setTiposDeProcedure(procedureType.getDto());

        return miDto;

    }

    public void setAtributos(DtoProcedureTemplate miDto)
    {
        notes = miDto.getNotes();
        if (documentType == null)
        {
            documentType = new DocumentType();
        }
        documentType.setAtributos(miDto.getTiposDeDocument());

        if (procedureType == null)
        {
            procedureType = new ProcedureType();
        }
        procedureType.setAtributos(miDto.getTiposDeProcedure());

        procedureTemplatePK = new ProcedureTemplatePK(procedureType.getIdProcedureType(), documentType.getIdDocumentType());
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (procedureTemplatePK != null ? procedureTemplatePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcedureTemplate))
        {
            return false;
        }
        ProcedureTemplate other = (ProcedureTemplate) object;
        if ((this.procedureTemplatePK == null && other.procedureTemplatePK != null) || (this.procedureTemplatePK != null && !this.procedureTemplatePK.equals(other.procedureTemplatePK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PlantillaTramite[ plantillaTramitePK=" + procedureTemplatePK + " ]";
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
