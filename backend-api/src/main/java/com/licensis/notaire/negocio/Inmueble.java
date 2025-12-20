/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoInmueble;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa a un inmueble.
 *
 * @author juanca
 */
@Entity
@Table(name = "inmuebles")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Inmueble.findAll", query = "SELECT i FROM Inmueble i"),
            @NamedQuery(name = "Inmueble.findByIdInmueble", query = "SELECT i FROM Inmueble i WHERE i.idInmueble = :idInmueble"),
            @NamedQuery(name = "Inmueble.findByNomenclatura", query = "SELECT i FROM Inmueble i WHERE i.nomenclaturaCatastral = :nomenclatura")
        })
public class Inmueble implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_inmueble")
    private Integer idInmueble;
    @Basic(optional = false)
    @Lob
    @Column(name = "nomenclatura_catastral")
    private String nomenclaturaCatastral;
    @Lob
    @Column(name = "valuacion_fiscal")
    private String valuacionFiscal;
    @Basic(optional = false)
    @Lob
    @Column(name = "domicilio")
    private String domicilio;
    @Basic(optional = false)
    @Lob
    @Column(name = "tipo_inmueble")
    private String tipoInmueble;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "fkIdInmueble", fetch = FetchType.LAZY)
    private List<Tramite> tramiteList;

    /**
     * Constructor por default de Inmueble. Inicializa el ID presupuesto segun el campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO, y las listas internas.
     */
    public Inmueble()
    {
        this.idInmueble = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
        this.tramiteList = new ArrayList<>();
    }

    public Inmueble(Integer idInmueble)
    {
        this.idInmueble = idInmueble;
    }

    public Inmueble(Integer idInmueble, String nomenclaturaCatastral, String domicilio, String tipoInmueble)
    {
        this.idInmueble = idInmueble;
        this.nomenclaturaCatastral = nomenclaturaCatastral;
        this.domicilio = domicilio;
        this.tipoInmueble = tipoInmueble;
    }

    public Integer getIdInmueble()
    {
        return idInmueble;
    }

    public void setIdInmueble(Integer idInmueble)
    {
        this.idInmueble = idInmueble;
    }

    public String getNomenclaturaCatastral()
    {
        return nomenclaturaCatastral;
    }

    public void setNomenclaturaCatastral(String nomenclaturaCatastral)
    {
        this.nomenclaturaCatastral = nomenclaturaCatastral;
    }

    public String getValuacionFiscal()
    {
        return valuacionFiscal;
    }

    public void setValuacionFiscal(String valuacionFiscal)
    {
        this.valuacionFiscal = valuacionFiscal;
    }

    public String getDomicilio()
    {
        return domicilio;
    }

    public void setDomicilio(String domicilio)
    {
        this.domicilio = domicilio;
    }

    public String getTipoInmueble()
    {
        return tipoInmueble;
    }

    public void setTipoInmueble(String tipoInmueble)
    {
        this.tipoInmueble = tipoInmueble;
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
    public List<Tramite> getTramiteList()
    {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList)
    {
        this.tramiteList = tramiteList;
    }

    public DtoInmueble getDto()
    {
        DtoInmueble miDto = new DtoInmueble();

        miDto.setDomicilio(this.getDomicilio());
        miDto.setIdInmueble(this.getIdInmueble());
        miDto.setNomenclaturaCatastral(this.getNomenclaturaCatastral());
        miDto.setObservaciones(this.getObservaciones());
        miDto.setTipoInmueble(this.getTipoInmueble());
        miDto.setValuacionFiscal(this.getValuacionFiscal());

        return miDto;
    }

    public void setAtributos(DtoInmueble miDtoInmueble)
    {
        if (miDtoInmueble.isValido())
        {
            this.domicilio = miDtoInmueble.getDomicilio();

            if (miDtoInmueble.getIdInmueble() != null)
            {
                this.idInmueble = miDtoInmueble.getIdInmueble();
            }

            this.nomenclaturaCatastral = miDtoInmueble.getNomenclaturaCatastral();
            this.observaciones = miDtoInmueble.getObservaciones();
            this.tipoInmueble = miDtoInmueble.getTipoInmueble();
            this.valuacionFiscal = miDtoInmueble.getValuacionFiscal();
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idInmueble != null ? idInmueble.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Inmueble))
        {
            return false;
        }
        Inmueble other = (Inmueble) object;
        if ((this.idInmueble == null && other.idInmueble != null) || (this.idInmueble != null && !this.idInmueble.equals(other.idInmueble)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Inmueble[ idInmueble=" + idInmueble + " ]"
                + "[ nomenclatura=" + nomenclaturaCatastral + " ]";
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
