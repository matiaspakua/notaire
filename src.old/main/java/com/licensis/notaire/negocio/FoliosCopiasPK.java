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
public class FoliosCopiasPK implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fk_id_folio")
    private int fkIdFolio;
    @Basic(optional = false)
    @Column(name = "fk_id_copia")
    private int fkIdCopia;

    public FoliosCopiasPK()
    {
    }

    public FoliosCopiasPK(int fkIdFolio, int fkIdCopia)
    {
        this.fkIdFolio = fkIdFolio;
        this.fkIdCopia = fkIdCopia;
    }

    public int getFkIdFolio()
    {
        return fkIdFolio;
    }

    public void setFkIdFolio(int fkIdFolio)
    {
        this.fkIdFolio = fkIdFolio;
    }

    public int getFkIdCopia()
    {
        return fkIdCopia;
    }

    public void setFkIdCopia(int fkIdCopia)
    {
        this.fkIdCopia = fkIdCopia;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) fkIdFolio;
        hash += (int) fkIdCopia;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FoliosCopiasPK))
        {
            return false;
        }
        FoliosCopiasPK other = (FoliosCopiasPK) object;
        if (this.fkIdFolio != other.fkIdFolio)
        {
            return false;
        }
        if (this.fkIdCopia != other.fkIdCopia)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.FoliosCopiasPK[ fkIdFolio=" + fkIdFolio + ", fkIdCopia=" + fkIdCopia + " ]";
    }
}
