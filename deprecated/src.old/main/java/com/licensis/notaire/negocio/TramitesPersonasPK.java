/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author matias
 */
@Embeddable
public class TramitesPersonasPK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_tramite")
    private int fkIdTramite;
    @Basic(optional = false)
    @Column(name = "fk_id_persona_cliente")
    private int fkIdPersonaCliente;

    public TramitesPersonasPK()
    {
    }

    public TramitesPersonasPK(int fkIdTramite, int fkIdPersonaCliente)
    {
        this.fkIdTramite = fkIdTramite;
        this.fkIdPersonaCliente = fkIdPersonaCliente;
    }

    public int getFkIdTramite()
    {
        return fkIdTramite;
    }

    public void setFkIdTramite(int fkIdTramite)
    {
        this.fkIdTramite = fkIdTramite;
    }

    public int getFkIdPersonaCliente()
    {
        return fkIdPersonaCliente;
    }

    public void setFkIdPersonaCliente(int fkIdPersonaCliente)
    {
        this.fkIdPersonaCliente = fkIdPersonaCliente;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += fkIdTramite;
        hash += fkIdPersonaCliente;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TramitesPersonasPK))
        {
            return false;
        }
        TramitesPersonasPK other = (TramitesPersonasPK) object;
        if (this.fkIdTramite != other.fkIdTramite)
        {
            return false;
        }
        if (this.fkIdPersonaCliente != other.fkIdPersonaCliente)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.TramitesPersonasPK[ fkIdTramite=" + fkIdTramite + ", fkIdPersonaCliente=" + fkIdPersonaCliente + " ]";
    }
}
