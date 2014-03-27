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
 * @author juanca
 */
@Embeddable
public class PlantillaTramitePK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_tramite")
    private int fkIdTipoTramite;
    @Basic(optional = false)
    @Column(name = "fk_id_tipo_documento")
    private int fkIdTipoDocumento;

    public PlantillaTramitePK() {
    }

    public PlantillaTramitePK(int fkIdTipoTramite, int fkIdTipoDocumento) {
        this.fkIdTipoTramite = fkIdTipoTramite;
        this.fkIdTipoDocumento = fkIdTipoDocumento;
    }

    public int getFkIdTipoTramite() {
        return fkIdTipoTramite;
    }

    public void setFkIdTipoTramite(int fkIdTipoTramite) {
        this.fkIdTipoTramite = fkIdTipoTramite;
    }

    public int getFkIdTipoDocumento() {
        return fkIdTipoDocumento;
    }

    public void setFkIdTipoDocumento(int fkIdTipoDocumento) {
        this.fkIdTipoDocumento = fkIdTipoDocumento;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) fkIdTipoTramite;
        hash += (int) fkIdTipoDocumento;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlantillaTramitePK))
        {
            return false;
        }
        PlantillaTramitePK other = (PlantillaTramitePK) object;
        if (this.fkIdTipoTramite != other.fkIdTipoTramite)
        {
            return false;
        }
        if (this.fkIdTipoDocumento != other.fkIdTipoDocumento)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "negocio.PlantillaTramitePK[ fkIdTipoTramite=" + fkIdTipoTramite + ", fkIdTipoDocumento=" + fkIdTipoDocumento + " ]";
    }
}
