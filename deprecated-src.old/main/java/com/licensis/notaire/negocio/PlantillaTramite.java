/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoPlantillaTramite;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author User
 */
@Entity
@Table(name = "plantilla_tramites")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "PlantillaTramite.findAll", query = "SELECT p FROM PlantillaTramite p"),
            @NamedQuery(name = "PlantillaTramite.findByFkIdTipoTramite", query = "SELECT p FROM PlantillaTramite p WHERE p.plantillaTramitePK.fkIdTipoTramite = :fkIdTipoTramite"),
            @NamedQuery(name = "PlantillaTramite.findByFkIdTipoDocumento", query = "SELECT p FROM PlantillaTramite p WHERE p.plantillaTramitePK.fkIdTipoDocumento = :fkIdTipoDocumento")
        })
public class PlantillaTramite implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PlantillaTramitePK plantillaTramitePK;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoDeTramite tipoDeTramite;
    @JoinColumn(name = "fk_id_tipo_documento", referencedColumnName = "id_tipo_documento", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoDeDocumento tipoDeDocumento;

    public PlantillaTramite()
    {
    }

    public PlantillaTramite(PlantillaTramitePK plantillaTramitePK)
    {
        this.plantillaTramitePK = plantillaTramitePK;
    }

    public PlantillaTramite(int fkIdTipoTramite, int fkIdTipoDocumento)
    {
        this.plantillaTramitePK = new PlantillaTramitePK(fkIdTipoTramite, fkIdTipoDocumento);
    }

    public PlantillaTramitePK getPlantillaTramitePK()
    {
        return plantillaTramitePK;
    }

    public void setPlantillaTramitePK(PlantillaTramitePK plantillaTramitePK)
    {
        this.plantillaTramitePK = plantillaTramitePK;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public TipoDeTramite getTipoDeTramite()
    {
        return tipoDeTramite;
    }

    public void setTipoDeTramite(TipoDeTramite tipoDeTramite)
    {
        this.tipoDeTramite = tipoDeTramite;
    }

    public TipoDeDocumento getTipoDeDocumento()
    {
        return tipoDeDocumento;
    }

    public void setTipoDeDocumento(TipoDeDocumento tipoDeDocumento)
    {
        this.tipoDeDocumento = tipoDeDocumento;
    }

    public DtoPlantillaTramite getDto()
    {
        DtoPlantillaTramite miDto = new DtoPlantillaTramite();

        miDto.setObservaciones(observaciones);
        miDto.setTiposDeDocumento(tipoDeDocumento.getDto());
        miDto.setTiposDeTramite(tipoDeTramite.getDto());

        return miDto;

    }

    public void setAtributos(DtoPlantillaTramite miDto)
    {
        observaciones = miDto.getObservaciones();
        if (tipoDeDocumento == null)
        {
            tipoDeDocumento = new TipoDeDocumento();
        }
        tipoDeDocumento.setAtributos(miDto.getTiposDeDocumento());

        if (tipoDeTramite == null)
        {
            tipoDeTramite = new TipoDeTramite();
        }
        tipoDeTramite.setAtributos(miDto.getTiposDeTramite());

        plantillaTramitePK = new PlantillaTramitePK(tipoDeTramite.getIdTipoTramite(), tipoDeDocumento.getIdTipoDocumento());
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (plantillaTramitePK != null ? plantillaTramitePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlantillaTramite))
        {
            return false;
        }
        PlantillaTramite other = (PlantillaTramite) object;
        if ((this.plantillaTramitePK == null && other.plantillaTramitePK != null) || (this.plantillaTramitePK != null && !this.plantillaTramitePK.equals(other.plantillaTramitePK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PlantillaTramite[ plantillaTramitePK=" + plantillaTramitePK + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
