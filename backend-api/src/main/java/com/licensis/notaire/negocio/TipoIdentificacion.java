/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoTipoIdentificacion;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
 *
 * @author juanca
 */
@Entity
@Table(name = "tipos_identificacion")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "TipoIdentificacion.findAll", query = "SELECT t FROM TipoIdentificacion t"),
            @NamedQuery(name = "TipoIdentificacion.findByIdTipoIdentificacion", query = "SELECT t FROM TipoIdentificacion t WHERE t.idTipoIdentificacion = :idTipoIdentificacion")
        })
public class TipoIdentificacion implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_identificacion")
    private Integer idTipoIdentificacion;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTipoIdentificacion", fetch = FetchType.LAZY)
    private List<Persona> personaList;

    public TipoIdentificacion()
    {
    }

    public TipoIdentificacion(Integer idTipoIdentificacion)
    {
        this.idTipoIdentificacion = idTipoIdentificacion;
    }

    public TipoIdentificacion(Integer idTipoIdentificacion, String nombre)
    {
        this.idTipoIdentificacion = idTipoIdentificacion;
        this.nombre = nombre;
    }

    public Integer getIdTipoIdentificacion()
    {
        return idTipoIdentificacion;
    }

    public void setIdTipoIdentificacion(Integer idTipoIdentificacion)
    {
        this.idTipoIdentificacion = idTipoIdentificacion;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    @XmlTransient
    public List<Persona> getPersonaList()
    {
        return personaList;
    }

    public void setPersonaList(List<Persona> personaList)
    {
        this.personaList = personaList;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idTipoIdentificacion != null ? idTipoIdentificacion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoIdentificacion))
        {
            return false;
        }
        TipoIdentificacion other = (TipoIdentificacion) object;
        if ((this.idTipoIdentificacion == null && other.idTipoIdentificacion != null) || (this.idTipoIdentificacion != null && !this.idTipoIdentificacion.equals(other.idTipoIdentificacion)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TipoIdentificacion[ idTipoIdentificacion=" + idTipoIdentificacion + " ]"
                + "[ nombre=" + nombre + " ]";
    }

    public DtoTipoIdentificacion getDto()
    {

        DtoTipoIdentificacion miDto = new DtoTipoIdentificacion();

        try
        {
            miDto.setIdTipoIdentificacion(this.getIdTipoIdentificacion());
            miDto.setNombre(this.getNombre());

        }
        catch (NullPointerException e)
        {
            System.out.println("Erro getDto Tipo Identificacion");
        }
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
}
