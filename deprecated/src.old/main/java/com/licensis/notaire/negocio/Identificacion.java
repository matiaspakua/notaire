/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author User
 */
@Entity
@Table(name = "identificaciones")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Identificacion.findAll", query = "SELECT i FROM Identificacion i"),
            @NamedQuery(name = "Identificacion.findByNumero", query = "SELECT i FROM Identificacion i WHERE i.numero = :numero"),
            @NamedQuery(name = "Identificacion.findByFkIdPersona", query = "SELECT i FROM Identificacion i WHERE i.identificacionPK.fkIdPersona = :fkIdPersona"),
            @NamedQuery(name = "Identificacion.findByFkIdTipoIdentificacion", query = "SELECT i FROM Identificacion i WHERE i.identificacionPK.fkIdTipoIdentificacion = :fkIdTipoIdentificacion")
        })
public class Identificacion implements Serializable
{

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected IdentificacionPK identificacionPK;
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id_persona", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Persona persona;
    @JoinColumn(name = "fk_id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoIdentificacion tipoIdentificacion;

    public Identificacion()
    {
    }

    public Identificacion(IdentificacionPK identificacionPK)
    {
        this.identificacionPK = identificacionPK;
    }

    public Identificacion(IdentificacionPK identificacionPK, int numero)
    {
        this.identificacionPK = identificacionPK;
        this.numero = numero;
    }

    public Identificacion(int fkIdPersona, int fkIdTipoIdentificacion)
    {
        this.identificacionPK = new IdentificacionPK(fkIdPersona, fkIdTipoIdentificacion);
    }

    public IdentificacionPK getIdentificacionPK()
    {
        return identificacionPK;
    }

    public void setIdentificacionPK(IdentificacionPK identificacionPK)
    {
        this.identificacionPK = identificacionPK;
    }

    public int getNumero()
    {
        return numero;
    }

    public void setNumero(int numero)
    {
        this.numero = numero;
    }

    public Persona getPersona()
    {
        return persona;
    }

    public void setPersona(Persona persona)
    {
        this.persona = persona;
    }

    public TipoIdentificacion getTipoIdentificacion()
    {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion)
    {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (identificacionPK != null ? identificacionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Identificacion))
        {
            return false;
        }
        Identificacion other = (Identificacion) object;
        if ((this.identificacionPK == null && other.identificacionPK != null) || (this.identificacionPK != null && !this.identificacionPK.equals(other.identificacionPK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Identificacion[ identificacionPK=" + identificacionPK + " ]"
                + "[ numero=" + numero + " ]";
    }
}
