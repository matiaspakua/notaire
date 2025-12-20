/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoTipoDeTramite;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author User
 */
@Entity
@Table(name = "tipos_de_tramite")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "TipoDeTramite.findAll", query = "SELECT t FROM TipoDeTramite t"),
            @NamedQuery(name = "TipoDeTramite.findByIdTipoTramite", query = "SELECT t FROM TipoDeTramite t WHERE t.idTipoTramite = :idTipoTramite"),
            @NamedQuery(name = "TipoDeTramite.findBySeArchiva", query = "SELECT t FROM TipoDeTramite t WHERE t.seArchiva = :seArchiva"),
            @NamedQuery(name = "TipoDeTramite.findBySeInscribe", query = "SELECT t FROM TipoDeTramite t WHERE t.seInscribe = :seInscribe"),
            @NamedQuery(name = "TipoDeTramite.findByAsociaInmuebles", query = "SELECT t FROM TipoDeTramite t WHERE t.asociaInmuebles = :asociaInmuebles"),
            @NamedQuery(name = "TipoDeTramite.findByNombre", query = "SELECT t FROM TipoDeTramite t WHERE t.nombre = :nombre")
        })
public class TipoDeTramite implements Serializable
{

    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean habilitado;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_tramite")
    private Integer idTipoTramite;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @Column(name = "se_archiva")
    private boolean seArchiva;
    @Basic(optional = false)
    @Column(name = "se_inscribe")
    private boolean seInscribe;
    @Basic(optional = false)
    @Column(name = "asocia_inmuebles")
    private boolean asociaInmuebles;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tipoDeTramite", fetch = FetchType.LAZY)
    private List<PlantillaPresupuesto> plantillaPresupuestoList = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tipoDeTramite", fetch = FetchType.LAZY)
    private List<PlantillaTramite> plantillaTramiteList = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTipoTramite", fetch = FetchType.LAZY)
    private List<Tramite> tramiteList = new ArrayList<>();

    public TipoDeTramite()
    {
    }

    public TipoDeTramite(Integer idTipoTramite)
    {
        this.idTipoTramite = idTipoTramite;
    }

    public TipoDeTramite(Integer idTipoTramite, String nombre, boolean seArchiva, boolean seInscribe, boolean asociaInmuebles)
    {
        this.idTipoTramite = idTipoTramite;
        this.nombre = nombre;
        this.seArchiva = seArchiva;
        this.seInscribe = seInscribe;
        this.asociaInmuebles = asociaInmuebles;
    }

    public Integer getIdTipoTramite()
    {
        return idTipoTramite;
    }

    public void setIdTipoTramite(Integer idTipoTramite)
    {
        this.idTipoTramite = idTipoTramite;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public boolean getSeArchiva()
    {
        return seArchiva;
    }

    public void setSeArchiva(boolean seArchiva)
    {
        this.seArchiva = seArchiva;
    }

    public boolean getSeInscribe()
    {
        return seInscribe;
    }

    public void setSeInscribe(boolean seInscribe)
    {
        this.seInscribe = seInscribe;
    }

    public boolean getAsociaInmuebles()
    {
        return asociaInmuebles;
    }

    public void setAsociaInmuebles(boolean asociaInmuebles)
    {
        this.asociaInmuebles = asociaInmuebles;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    @XmlTransient
    public List<PlantillaPresupuesto> getPlantillaPresupuestoList()
    {
        return plantillaPresupuestoList;
    }

    public void setPlantillaPresupuestoList(List<PlantillaPresupuesto> plantillaPresupuestoList)
    {
        this.plantillaPresupuestoList = plantillaPresupuestoList;
    }

    @XmlTransient
    public List<PlantillaTramite> getPlantillaTramiteList()
    {
        return plantillaTramiteList;
    }

    public void setPlantillaTramiteList(List<PlantillaTramite> plantillaTramiteList)
    {
        this.plantillaTramiteList = plantillaTramiteList;
    }

    @XmlTransient
    public List<Tramite> getTramiteList()
    {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList)
    {
        this.tramiteList = tramiteList;
    }

    public void setAtributos(DtoTipoDeTramite dtoTipoDeTramite)
    {
        if (dtoTipoDeTramite.isValido())
        {
            this.idTipoTramite = dtoTipoDeTramite.getIdTipoTramite();
            this.nombre = dtoTipoDeTramite.getNombre();
            this.seArchiva = dtoTipoDeTramite.isSeArchiva();
            this.seInscribe = dtoTipoDeTramite.isSeInscribe();
            this.asociaInmuebles = dtoTipoDeTramite.getAsociaInmuebles();
            this.observaciones = dtoTipoDeTramite.getObservaciones();
            this.version = dtoTipoDeTramite.getVersion();
            habilitado = dtoTipoDeTramite.getHabilitado();
        }
    }

    public DtoTipoDeTramite getDto()
    {
        DtoTipoDeTramite miDto = new DtoTipoDeTramite();

        miDto.setIdTipoTramite(idTipoTramite);
        miDto.setNombre(nombre);
        miDto.setSeArchiva(seArchiva);
        miDto.setSeInscribe(seInscribe);
        miDto.setAsociaInmuebles(asociaInmuebles);
        miDto.setObservaciones(observaciones);
        miDto.setVersion(version);
        miDto.setHabilitado(habilitado);

        return miDto;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idTipoTramite != null ? idTipoTramite.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoDeTramite))
        {
            return false;
        }
        TipoDeTramite other = (TipoDeTramite) object;
        if ((this.idTipoTramite == null && other.idTipoTramite != null) || (this.idTipoTramite != null && !this.idTipoTramite.equals(other.idTipoTramite)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TipoDeTramite[ idTipoTramite=" + idTipoTramite + " ]"
                + "[ nombre=" + nombre + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public boolean getHabilitado()
    {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado)
    {
        this.habilitado = habilitado;
    }
}
