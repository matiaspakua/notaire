/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author juanca
 */
@Embeddable
public class BudgetTemplatePK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_tramite")
    private int fkIdProcedureType;
    @Basic(optional = false)
    @Column(name = "fk_id_concepto")
    private int fkIdConcept;

    public BudgetTemplatePK()
    {
    }

    public BudgetTemplatePK(int fkIdProcedureType, int fkIdConcept)
    {
        this.fkIdProcedureType = fkIdProcedureType;
        this.fkIdConcept = fkIdConcept;
    }

    public int getFkIdProcedureType()
    {
        return fkIdProcedureType;
    }

    public void setFkIdProcedureType(int fkIdProcedureType)
    {
        this.fkIdProcedureType = fkIdProcedureType;
    }

    public int getFkIdConcept()
    {
        return fkIdConcept;
    }

    public void setFkIdConcept(int fkIdConcept)
    {
        this.fkIdConcept = fkIdConcept;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) fkIdProcedureType;
        hash += (int) fkIdConcept;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BudgetTemplatePK))
        {
            return false;
        }
        BudgetTemplatePK other = (BudgetTemplatePK) object;
        if (this.fkIdProcedureType != other.fkIdProcedureType)
        {
            return false;
        }
        if (this.fkIdConcept != other.fkIdConcept)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.PlantillaPresupuestoPK[ fkIdTipoTramite=" + fkIdProcedureType + ", fkIdConcepto=" + fkIdConcept + " ]";
    }
}
