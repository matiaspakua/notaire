/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoBudgetTemplate;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.util.logging.Level;
import java.util.logging.Logger;
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
@Table(name = "plantilla_presupuestos")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "PlantillaPresupuesto.findAll", query = "SELECT p FROM BudgetTemplate p"),
            @NamedQuery(name = "PlantillaPresupuesto.findByFkIdTipoTramite", query = "SELECT p FROM BudgetTemplate p WHERE p.budgetTemplatePK.fkIdProcedureType = :fkIdTipoTramite"),
            @NamedQuery(name = "PlantillaPresupuesto.findByFkIdConcepto", query = "SELECT p FROM BudgetTemplate p WHERE p.budgetTemplatePK.fkIdConcept = :fkIdConcepto")
        })
public class BudgetTemplate implements Serializable, Persistable<BudgetTemplatePK>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected BudgetTemplatePK budgetTemplatePK;
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ProcedureType procedureType;
    @JoinColumn(name = "fk_id_concepto", referencedColumnName = "id_concepto", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Concept concept;
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
    public BudgetTemplatePK getId() {
        return budgetTemplatePK;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return isNewEntity;
    }


    public BudgetTemplate()
    {
    }

    public BudgetTemplate(BudgetTemplatePK budgetTemplatePK)
    {
        this.budgetTemplatePK = budgetTemplatePK;
    }

    public BudgetTemplate(int fkIdProcedureType, int fkIdConcept)
    {
        this.budgetTemplatePK = new BudgetTemplatePK(fkIdProcedureType, fkIdConcept);
    }

    public BudgetTemplatePK getBudgetTemplatePK()
    {
        return budgetTemplatePK;
    }

    public void setBudgetTemplatePK(BudgetTemplatePK budgetTemplatePK)
    {
        this.budgetTemplatePK = budgetTemplatePK;
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

    public Concept getConcept()
    {
        return concept;
    }

    public void setConcept(Concept concept)
    {
        this.concept = concept;
    }

    public void setAtributos(DtoBudgetTemplate miDto)
    {
        try
        {
            if (procedureType == null)
            {
                procedureType = new ProcedureType();
            }
            procedureType.setAtributos(miDto.getTiposDeProcedure());

            if (concept == null)
            {
                concept = new Concept();
            }
            concept.setAtributos(miDto.getConceptos());

            budgetTemplatePK = new BudgetTemplatePK(procedureType.getIdProcedureType(), concept.getIdConcept());

            version = miDto.getVersion();

        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(BudgetTemplate.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoBudgetTemplate getDto()
    {
        DtoBudgetTemplate miDto = new DtoBudgetTemplate();

        miDto.setConceptos(concept.getDto());
        miDto.setTiposDeProcedure(procedureType.getDto());

        miDto.setVersion(version);

        return miDto;
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
        hash += (budgetTemplatePK != null ? budgetTemplatePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BudgetTemplate))
        {
            return false;
        }
        BudgetTemplate other = (BudgetTemplate) object;
        if ((this.budgetTemplatePK == null && other.budgetTemplatePK != null) || (this.budgetTemplatePK != null && !this.budgetTemplatePK.equals(other.budgetTemplatePK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PlantillaPresupuesto[ plantillaPresupuestoPK=" + budgetTemplatePK + " ]";
    }
}
