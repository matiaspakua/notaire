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
public class PlantillaPresupuestoPK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_tipo_tramite")
    private int fkIdTipoTramite;
    @Basic(optional = false)
    @Column(name = "fk_id_concepto")
    private int fkIdConcepto;

    public PlantillaPresupuestoPK() {
    }

    public PlantillaPresupuestoPK(int fkIdTipoTramite, int fkIdConcepto) {
        this.fkIdTipoTramite = fkIdTipoTramite;
        this.fkIdConcepto = fkIdConcepto;
    }

    public int getFkIdTipoTramite() {
        return fkIdTipoTramite;
    }

    public void setFkIdTipoTramite(int fkIdTipoTramite) {
        this.fkIdTipoTramite = fkIdTipoTramite;
    }

    public int getFkIdConcepto() {
        return fkIdConcepto;
    }

    public void setFkIdConcepto(int fkIdConcepto) {
        this.fkIdConcepto = fkIdConcepto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) fkIdTipoTramite;
        hash += (int) fkIdConcepto;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlantillaPresupuestoPK))
        {
            return false;
        }
        PlantillaPresupuestoPK other = (PlantillaPresupuestoPK) object;
        if (this.fkIdTipoTramite != other.fkIdTipoTramite)
        {
            return false;
        }
        if (this.fkIdConcepto != other.fkIdConcepto)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "negocio.PlantillaPresupuestoPK[ fkIdTipoTramite=" + fkIdTipoTramite + ", fkIdConcepto=" + fkIdConcepto + " ]";
    }
}
