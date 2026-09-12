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
 * @author matias
 */
@Embeddable
public class PersonProcedurePK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_tramite")
    private int fkIdProcedure;
    @Basic(optional = false)
    @Column(name = "fk_id_persona_cliente")
    private int fkIdClientPerson;

    public PersonProcedurePK()
    {
    }

    public PersonProcedurePK(int fkIdProcedure, int fkIdClientPerson)
    {
        this.fkIdProcedure = fkIdProcedure;
        this.fkIdClientPerson = fkIdClientPerson;
    }

    public int getFkIdProcedure()
    {
        return fkIdProcedure;
    }

    public void setFkIdProcedure(int fkIdProcedure)
    {
        this.fkIdProcedure = fkIdProcedure;
    }

    public int getFkIdClientPerson()
    {
        return fkIdClientPerson;
    }

    public void setFkIdClientPerson(int fkIdClientPerson)
    {
        this.fkIdClientPerson = fkIdClientPerson;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += fkIdProcedure;
        hash += fkIdClientPerson;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PersonProcedurePK))
        {
            return false;
        }
        PersonProcedurePK other = (PersonProcedurePK) object;
        if (this.fkIdProcedure != other.fkIdProcedure)
        {
            return false;
        }
        if (this.fkIdClientPerson != other.fkIdClientPerson)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.TramitesPersonasPK[ fkIdTramite=" + fkIdProcedure + ", fkIdPersonaCliente=" + fkIdClientPerson + " ]";
    }
}
