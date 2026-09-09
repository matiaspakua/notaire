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
public class ProcedureTemplatePK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_tramite")
    private int fkIdProcedureType;
    @Basic(optional = false)
    @Column(name = "fk_id_tipo_documento")
    private int fkIdDocumentType;

    public ProcedureTemplatePK()
    {
    }

    public ProcedureTemplatePK(int fkIdProcedureType, int fkIdDocumentType)
    {
        this.fkIdProcedureType = fkIdProcedureType;
        this.fkIdDocumentType = fkIdDocumentType;
    }

    public int getFkIdProcedureType()
    {
        return fkIdProcedureType;
    }

    public void setFkIdProcedureType(int fkIdProcedureType)
    {
        this.fkIdProcedureType = fkIdProcedureType;
    }

    public int getFkIdDocumentType()
    {
        return fkIdDocumentType;
    }

    public void setFkIdDocumentType(int fkIdDocumentType)
    {
        this.fkIdDocumentType = fkIdDocumentType;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) fkIdProcedureType;
        hash += (int) fkIdDocumentType;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcedureTemplatePK))
        {
            return false;
        }
        ProcedureTemplatePK other = (ProcedureTemplatePK) object;
        if (this.fkIdProcedureType != other.fkIdProcedureType)
        {
            return false;
        }
        if (this.fkIdDocumentType != other.fkIdDocumentType)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.PlantillaTramitePK[ fkIdTipoTramite=" + fkIdProcedureType + ", fkIdTipoDocumento=" + fkIdDocumentType + " ]";
    }
}
