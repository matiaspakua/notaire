/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author User
 */
@Embeddable
public class IdentificacionPK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_persona")
    private int fkIdPersona;
    @Basic(optional = false)
    @Column(name = "fk_id_tipo_identificacion")
    private int fkIdTipoIdentificacion;

    public IdentificacionPK()
    {
    }

    public IdentificacionPK(int fkIdPersona, int fkIdTipoIdentificacion)
    {
        this.fkIdPersona = fkIdPersona;
        this.fkIdTipoIdentificacion = fkIdTipoIdentificacion;
    }

    public int getFkIdPersona()
    {
        return fkIdPersona;
    }

    public void setFkIdPersona(int fkIdPersona)
    {
        this.fkIdPersona = fkIdPersona;
    }

    public int getFkIdTipoIdentificacion()
    {
        return fkIdTipoIdentificacion;
    }

    public void setFkIdTipoIdentificacion(int fkIdTipoIdentificacion)
    {
        this.fkIdTipoIdentificacion = fkIdTipoIdentificacion;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) fkIdPersona;
        hash += (int) fkIdTipoIdentificacion;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IdentificacionPK))
        {
            return false;
        }
        IdentificacionPK other = (IdentificacionPK) object;
        if (this.fkIdPersona != other.fkIdPersona)
        {
            return false;
        }
        if (this.fkIdTipoIdentificacion != other.fkIdTipoIdentificacion)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.IdentificacionPK[ fkIdPersona=" + fkIdPersona + ", fkIdTipoIdentificacion=" + fkIdTipoIdentificacion + " ]";
    }
}
