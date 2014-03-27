/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "folios_copias")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "FoliosCopias.findAll", query = "SELECT f FROM FoliosCopias f"),
    @NamedQuery(name = "FoliosCopias.findByVersion", query = "SELECT f FROM FoliosCopias f WHERE f.version = :version"),
    @NamedQuery(name = "FoliosCopias.findByFkIdFolio", query = "SELECT f FROM FoliosCopias f WHERE f.foliosCopiasPK.fkIdFolio = :fkIdFolio"),
    @NamedQuery(name = "FoliosCopias.findByFkIdCopia", query = "SELECT f FROM FoliosCopias f WHERE f.foliosCopiasPK.fkIdCopia = :fkIdCopia")
})
public class FoliosCopias implements Serializable
{

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected FoliosCopiasPK foliosCopiasPK;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @JoinColumn(name = "fk_id_copia", referencedColumnName = "id_copia", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Copia copia;
    @JoinColumn(name = "fk_id_folio", referencedColumnName = "id_folio", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Folio folio;

    public FoliosCopias() {
    }

    public FoliosCopias(FoliosCopiasPK foliosCopiasPK) {
        this.foliosCopiasPK = foliosCopiasPK;
    }

    public FoliosCopias(FoliosCopiasPK foliosCopiasPK, int version) {
        this.foliosCopiasPK = foliosCopiasPK;
        this.version = version;
    }

    public FoliosCopias(int fkIdFolio, int fkIdCopia) {
        this.foliosCopiasPK = new FoliosCopiasPK(fkIdFolio, fkIdCopia);
    }

    public FoliosCopiasPK getFoliosCopiasPK() {
        return foliosCopiasPK;
    }

    public void setFoliosCopiasPK(FoliosCopiasPK foliosCopiasPK) {
        this.foliosCopiasPK = foliosCopiasPK;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Copia getCopia() {
        return copia;
    }

    public void setCopia(Copia copia) {
        this.copia = copia;
    }

    public Folio getFolio() {
        return folio;
    }

    public void setFolio(Folio folio) {
        this.folio = folio;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (foliosCopiasPK != null ? foliosCopiasPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FoliosCopias))
        {
            return false;
        }
        FoliosCopias other = (FoliosCopias) object;
        if ((this.foliosCopiasPK == null && other.foliosCopiasPK != null) || (this.foliosCopiasPK != null && !this.foliosCopiasPK.equals(other.foliosCopiasPK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "negocio.FoliosCopias[ foliosCopiasPK=" + foliosCopiasPK + " ]";
    }
}
