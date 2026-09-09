/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoProcedureType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
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
 * @author User
 */
@Entity
@Table(name = "tipos_de_tramite")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "TipoDeTramite.findAll", query = "SELECT t FROM ProcedureType t"),
        @NamedQuery(name = "TipoDeTramite.findByIdTipoTramite", query = "SELECT t FROM ProcedureType t WHERE t.idProcedureType = :idTipoTramite"),
        @NamedQuery(name = "TipoDeTramite.findBySeArchiva", query = "SELECT t FROM ProcedureType t WHERE t.isArchived = :seArchiva"),
        @NamedQuery(name = "TipoDeTramite.findBySeInscribe", query = "SELECT t FROM ProcedureType t WHERE t.isRegistered = :seInscribe"),
        @NamedQuery(name = "TipoDeTramite.findByAsociaInmuebles", query = "SELECT t FROM ProcedureType t WHERE t.associatesProperties = :asociaInmuebles"),
        @NamedQuery(name = "TipoDeTramite.findByNombre", query = "SELECT t FROM ProcedureType t WHERE t.name = :nombre")
})
public class ProcedureType implements Serializable, Persistable<Integer> {

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
    @Column(name = "id_tipo_tramite")
    private Integer idProcedureType;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Basic(optional = false)
    @Column(name = "se_archiva")
    private boolean isArchived;
    @Basic(optional = false)
    @Column(name = "se_inscribe")
    private boolean isRegistered;
    @Basic(optional = false)
    @Column(name = "asocia_inmuebles")
    private boolean associatesProperties;
    @Column(name = "observaciones")
    private String notes;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "procedureType", fetch = FetchType.LAZY)
    private List<BudgetTemplate> budgetTemplateList = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "procedureType", fetch = FetchType.LAZY)
    private List<ProcedureTemplate> procedureTemplateList = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdProcedureType", fetch = FetchType.LAZY)
    private List<Procedure> procedureList = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_workflow_definition_id")
    private WorkflowDefinition workflowDefinition;

    public ProcedureType() {
    }

    public ProcedureType(Integer idProcedureType) {
        this.idProcedureType = idProcedureType;
    }

    public ProcedureType(Integer idProcedureType, String name, boolean isArchived, boolean isRegistered,
            boolean associatesProperties) {
        this.idProcedureType = idProcedureType;
        this.name = name;
        this.isArchived = isArchived;
        this.isRegistered = isRegistered;
        this.associatesProperties = associatesProperties;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idProcedureType;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idProcedureType == null || idProcedureType.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdProcedureType() {
        return idProcedureType;
    }

    public void setIdProcedureType(Integer idProcedureType) {
        this.idProcedureType = idProcedureType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public boolean getAssociatesProperties() {
        return associatesProperties;
    }

    public void setAssociatesProperties(boolean associatesProperties) {
        this.associatesProperties = associatesProperties;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @XmlTransient
    @JsonIgnore
    public List<BudgetTemplate> getBudgetTemplateList() {
        return budgetTemplateList;
    }

    public void setBudgetTemplateList(List<BudgetTemplate> budgetTemplateList) {
        this.budgetTemplateList = budgetTemplateList;
    }

    @XmlTransient
    @JsonIgnore
    public List<ProcedureTemplate> getProcedureTemplateList() {
        return procedureTemplateList;
    }

    public void setProcedureTemplateList(List<ProcedureTemplate> procedureTemplateList) {
        this.procedureTemplateList = procedureTemplateList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Procedure> getProcedureList() {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList) {
        this.procedureList = procedureList;
    }

    @JsonIgnore
    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public void setAtributos(DtoProcedureType dtoProcedureType) {
        if (dtoProcedureType.isValido()) {
            this.idProcedureType = dtoProcedureType.getIdProcedureType();
            this.name = dtoProcedureType.getName();
            // Null-safe unboxing: PUT payloads may omit boolean flags, which
            // previously NPE'd (e.g. getAsociaInmuebles() null on update).
            this.isArchived = Boolean.TRUE.equals(dtoProcedureType.getIsArchived());
            this.isRegistered = Boolean.TRUE.equals(dtoProcedureType.getIsRegistered());
            this.associatesProperties = Boolean.TRUE.equals(dtoProcedureType.getAssociatesProperties());
            this.notes = dtoProcedureType.getNotes();
            // Preserve current version when the payload omits it (avoids NPE on update).
            if (dtoProcedureType.getVersion() != null) {
                this.version = dtoProcedureType.getVersion();
            }
            enabled = !Boolean.FALSE.equals(dtoProcedureType.getEnabled());
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoProcedureType getDto() {
        DtoProcedureType miDto = new DtoProcedureType();

        miDto.setIdProcedureType(idProcedureType);
        miDto.setName(name);
        miDto.setIsArchived(isArchived);
        miDto.setIsRegistered(isRegistered);
        miDto.setAssociatesProperties(associatesProperties);
        miDto.setNotes(notes);
        miDto.setVersion(version);
        miDto.setEnabled(enabled);
        if (workflowDefinition != null) {
            miDto.setWorkflowDefinitionId(workflowDefinition.getId());
            miDto.setWorkflowDefinitionName(workflowDefinition.getName());
        }

        return miDto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProcedureType != null ? idProcedureType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcedureType)) {
            return false;
        }
        ProcedureType other = (ProcedureType) object;
        if ((this.idProcedureType == null && other.idProcedureType != null)
                || (this.idProcedureType != null && !this.idProcedureType.equals(other.idProcedureType))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TipoDeTramite[ idTipoTramite=" + idProcedureType + " ]"
                + "[ nombre=" + name + " ]";
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
