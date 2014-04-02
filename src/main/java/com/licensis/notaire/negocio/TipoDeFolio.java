/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
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
 * Clase que representa un tipo de folio en particular que puede ser para el protocolo: principal,
 * auxiliar, etc.
 * <p>
 * REGLA DE NEGOCIO<p>
 * <li> Los tipos de folios no se pueden eliminar (debido a
 * que se tiene que mantener un historia de los mismos), por lo tanto, solo se le puede cambiar el
 * estado a "INHABILITADO". </li>
 */
@Entity
@Table(name = "tipos_de_folio")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "TipoDeFolio.findAll", query = "SELECT t FROM TipoDeFolio t"),
            @NamedQuery(name = "TipoDeFolio.findByIdTipoFolio", query = "SELECT t FROM TipoDeFolio t WHERE t.idTipoFolio = :idTipoFolio")
        })
public class TipoDeFolio implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_folio")
    private Integer idTipoFolio;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTipoFolio", fetch = FetchType.EAGER)
    private List<Folio> folioList;
    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean habilitado;

    public TipoDeFolio()
    {
    }

    public TipoDeFolio(String nombreTipoDeFolio)
    {
        this.nombre = nombreTipoDeFolio;
    }

    public TipoDeFolio(Integer idTipoFolio)
    {
        this.idTipoFolio = idTipoFolio;
    }

    public TipoDeFolio(Integer idTipoFolio, String nombre)
    {
        this.idTipoFolio = idTipoFolio;
        this.nombre = nombre;
    }

    public Integer getIdTipoFolio()
    {
        return idTipoFolio;
    }

    public void setIdTipoFolio(Integer idTipoFolio)
    {
        this.idTipoFolio = idTipoFolio;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
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
    public List<Folio> getFolioList()
    {
        return folioList;
    }

    public void setFolioList(List<Folio> folioList)
    {
        this.folioList = folioList;
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
        hash += (idTipoFolio != null ? idTipoFolio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoDeFolio))
        {
            return false;
        }
        TipoDeFolio other = (TipoDeFolio) object;
        if ((this.idTipoFolio == null && other.idTipoFolio != null) || (this.idTipoFolio != null && !this.idTipoFolio.equals(other.idTipoFolio)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TipoDeFolio[ idTipoFolio=" + idTipoFolio + " ]"
                + "[ nombre=" + nombre + " ]";
    }

    public DtoTipoDeFolio getDto()
    {
        DtoTipoDeFolio miDtoTipoDeFolio = new DtoTipoDeFolio();

        miDtoTipoDeFolio.setIdTipoFolio(this.getIdTipoFolio());
        miDtoTipoDeFolio.setNombre(this.getNombre());
        miDtoTipoDeFolio.setObservaciones(this.getObservaciones());
        miDtoTipoDeFolio.setHabilitado(this.habilitado);
        miDtoTipoDeFolio.setVersion(this.getVersion());

        return miDtoTipoDeFolio;

    }

    public void setAtributos(DtoTipoDeFolio dtoTipoDeFolio) throws DtoInvalidoException
    {
        if (dtoTipoDeFolio.isValido())
        {
            this.setIdTipoFolio(dtoTipoDeFolio.getIdTipoFolio());
            this.setNombre(dtoTipoDeFolio.getNombre());
            this.setObservaciones(dtoTipoDeFolio.getObservaciones());
            this.setHabilitado(dtoTipoDeFolio.isHabilitado());
            this.setVersion(dtoTipoDeFolio.getVersion());
        } else
        {
            throw new DtoInvalidoException("El dto indicado no es valido.");
        }

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
