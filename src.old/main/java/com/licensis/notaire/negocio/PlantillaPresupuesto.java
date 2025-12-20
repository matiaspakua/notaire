/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoPlantillaPresupuesto;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Table(name = "plantilla_presupuestos")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "PlantillaPresupuesto.findAll", query = "SELECT p FROM PlantillaPresupuesto p"),
            @NamedQuery(name = "PlantillaPresupuesto.findByFkIdTipoTramite", query = "SELECT p FROM PlantillaPresupuesto p WHERE p.plantillaPresupuestoPK.fkIdTipoTramite = :fkIdTipoTramite"),
            @NamedQuery(name = "PlantillaPresupuesto.findByFkIdConcepto", query = "SELECT p FROM PlantillaPresupuesto p WHERE p.plantillaPresupuestoPK.fkIdConcepto = :fkIdConcepto")
        })
public class PlantillaPresupuesto implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PlantillaPresupuestoPK plantillaPresupuestoPK;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoDeTramite tipoDeTramite;
    @JoinColumn(name = "fk_id_concepto", referencedColumnName = "id_concepto", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Concepto concepto;

    public PlantillaPresupuesto()
    {
    }

    public PlantillaPresupuesto(PlantillaPresupuestoPK plantillaPresupuestoPK)
    {
        this.plantillaPresupuestoPK = plantillaPresupuestoPK;
    }

    public PlantillaPresupuesto(int fkIdTipoTramite, int fkIdConcepto)
    {
        this.plantillaPresupuestoPK = new PlantillaPresupuestoPK(fkIdTipoTramite, fkIdConcepto);
    }

    public PlantillaPresupuestoPK getPlantillaPresupuestoPK()
    {
        return plantillaPresupuestoPK;
    }

    public void setPlantillaPresupuestoPK(PlantillaPresupuestoPK plantillaPresupuestoPK)
    {
        this.plantillaPresupuestoPK = plantillaPresupuestoPK;
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

    public Concepto getConcepto()
    {
        return concepto;
    }

    public void setConcepto(Concepto concepto)
    {
        this.concepto = concepto;
    }

    public void setAtributos(DtoPlantillaPresupuesto miDto)
    {
        try
        {
            if (tipoDeTramite == null)
            {
                tipoDeTramite = new TipoDeTramite();
            }
            tipoDeTramite.setAtributos(miDto.getTiposDeTramite());

            if (concepto == null)
            {
                concepto = new Concepto();
            }
            concepto.setAtributos(miDto.getConceptos());

            plantillaPresupuestoPK = new PlantillaPresupuestoPK(tipoDeTramite.getIdTipoTramite(), concepto.getIdConcepto());

            version = miDto.getVersion();

        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(PlantillaPresupuesto.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public DtoPlantillaPresupuesto getDto()
    {
        DtoPlantillaPresupuesto miDto = new DtoPlantillaPresupuesto();

        miDto.setConceptos(concepto.getDto());
        miDto.setTiposDeTramite(tipoDeTramite.getDto());

        miDto.setVersion(version);

        return miDto;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (plantillaPresupuestoPK != null ? plantillaPresupuestoPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlantillaPresupuesto))
        {
            return false;
        }
        PlantillaPresupuesto other = (PlantillaPresupuesto) object;
        if ((this.plantillaPresupuestoPK == null && other.plantillaPresupuestoPK != null) || (this.plantillaPresupuestoPK != null && !this.plantillaPresupuestoPK.equals(other.plantillaPresupuestoPK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PlantillaPresupuesto[ plantillaPresupuestoPK=" + plantillaPresupuestoPK + " ]";
    }
}
