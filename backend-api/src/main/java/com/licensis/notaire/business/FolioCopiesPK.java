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
public class FolioCopiesPK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_folio")
    private int fkIdFolio;
    @Basic(optional = false)
    @Column(name = "fk_id_copia")
    private int fkIdCopy;

    public FolioCopiesPK()
    {
    }

    public FolioCopiesPK(int fkIdFolio, int fkIdCopy)
    {
        this.fkIdFolio = fkIdFolio;
        this.fkIdCopy = fkIdCopy;
    }

    public int getFkIdFolio()
    {
        return fkIdFolio;
    }

    public void setFkIdFolio(int fkIdFolio)
    {
        this.fkIdFolio = fkIdFolio;
    }

    public int getFkIdCopy()
    {
        return fkIdCopy;
    }

    public void setFkIdCopy(int fkIdCopy)
    {
        this.fkIdCopy = fkIdCopy;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) fkIdFolio;
        hash += (int) fkIdCopy;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FolioCopiesPK))
        {
            return false;
        }
        FolioCopiesPK other = (FolioCopiesPK) object;
        if (this.fkIdFolio != other.fkIdFolio)
        {
            return false;
        }
        if (this.fkIdCopy != other.fkIdCopy)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.FoliosCopiasPK[ fkIdFolio=" + fkIdFolio + ", fkIdCopia=" + fkIdCopy + " ]";
    }
}
