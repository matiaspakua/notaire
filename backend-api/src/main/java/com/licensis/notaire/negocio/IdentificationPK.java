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
 * @author User
 */
@Embeddable
public class IdentificationPK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_persona")
    private int fkIdPerson;
    @Basic(optional = false)
    @Column(name = "fk_id_tipo_identificacion")
    private int fkIdIdentificationType;

    public IdentificationPK()
    {
    }

    public IdentificationPK(int fkIdPerson, int fkIdIdentificationType)
    {
        this.fkIdPerson = fkIdPerson;
        this.fkIdIdentificationType = fkIdIdentificationType;
    }

    public int getFkIdPerson()
    {
        return fkIdPerson;
    }

    public void setFkIdPerson(int fkIdPerson)
    {
        this.fkIdPerson = fkIdPerson;
    }

    public int getFkIdIdentificationType()
    {
        return fkIdIdentificationType;
    }

    public void setFkIdIdentificationType(int fkIdIdentificationType)
    {
        this.fkIdIdentificationType = fkIdIdentificationType;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) fkIdPerson;
        hash += (int) fkIdIdentificationType;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IdentificationPK))
        {
            return false;
        }
        IdentificationPK other = (IdentificationPK) object;
        if (this.fkIdPerson != other.fkIdPerson)
        {
            return false;
        }
        if (this.fkIdIdentificationType != other.fkIdIdentificationType)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.IdentificacionPK[ fkIdPersona=" + fkIdPerson + ", fkIdTipoIdentificacion=" + fkIdIdentificationType + " ]";
    }
}
