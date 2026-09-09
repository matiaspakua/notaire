/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import java.io.Serializable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "folios_copias")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "FoliosCopias.findAll", query = "SELECT f FROM FolioCopies f"),
            @NamedQuery(name = "FoliosCopias.findByVersion", query = "SELECT f FROM FolioCopies f WHERE f.version = :version"),
            @NamedQuery(name = "FoliosCopias.findByFkIdFolio", query = "SELECT f FROM FolioCopies f WHERE f.folioCopiesPK.fkIdFolio = :fkIdFolio"),
            @NamedQuery(name = "FoliosCopias.findByFkIdCopia", query = "SELECT f FROM FolioCopies f WHERE f.folioCopiesPK.fkIdCopy = :fkIdCopia")
        })
public class FolioCopies implements Serializable, Persistable<FolioCopiesPK>
{

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected FolioCopiesPK folioCopiesPK;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @JoinColumn(name = "fk_id_copia", referencedColumnName = "id_copia", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Copy copy;
    @JoinColumn(name = "fk_id_folio", referencedColumnName = "id_folio", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Folio folio;
    @Transient
    private boolean isNewEntity = true;

    // Sets by Spring Data JPA's isNew() default heuristic for entities whose @EmbeddedId
    // is client-assigned (never null), so id-nullness cannot signal "new" the way it does
    // for @GeneratedValue entities. A transient flag flipped by these lifecycle callbacks
    // is the correct, standard Spring Data pattern for this case.
    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public FolioCopiesPK getId() {
        return folioCopiesPK;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return isNewEntity;
    }


    public FolioCopies()
    {
    }

    public FolioCopies(FolioCopiesPK folioCopiesPK)
    {
        this.folioCopiesPK = folioCopiesPK;
    }

    public FolioCopies(FolioCopiesPK folioCopiesPK, int version)
    {
        this.folioCopiesPK = folioCopiesPK;
        this.version = version;
    }

    public FolioCopies(int fkIdFolio, int fkIdCopy)
    {
        this.folioCopiesPK = new FolioCopiesPK(fkIdFolio, fkIdCopy);
    }

    public FolioCopiesPK getFolioCopiesPK()
    {
        return folioCopiesPK;
    }

    public void setFolioCopiesPK(FolioCopiesPK folioCopiesPK)
    {
        this.folioCopiesPK = folioCopiesPK;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public Copy getCopy()
    {
        return copy;
    }

    public void setCopy(Copy copy)
    {
        this.copy = copy;
    }

    public Folio getFolio()
    {
        return folio;
    }

    public void setFolio(Folio folio)
    {
        this.folio = folio;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (folioCopiesPK != null ? folioCopiesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FolioCopies))
        {
            return false;
        }
        FolioCopies other = (FolioCopies) object;
        if ((this.folioCopiesPK == null && other.folioCopiesPK != null) || (this.folioCopiesPK != null && !this.folioCopiesPK.equals(other.folioCopiesPK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.FoliosCopias[ foliosCopiasPK=" + folioCopiesPK + " ]";
    }
}
