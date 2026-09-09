/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author User
 */
@Entity
@Table(name = "identificaciones")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Identificacion.findAll", query = "SELECT i FROM Identification i"),
            @NamedQuery(name = "Identificacion.findByNumero", query = "SELECT i FROM Identification i WHERE i.number = :numero"),
            @NamedQuery(name = "Identificacion.findByFkIdPersona", query = "SELECT i FROM Identification i WHERE i.identificationPK.fkIdPerson = :fkIdPersona"),
            @NamedQuery(name = "Identificacion.findByFkIdTipoIdentificacion", query = "SELECT i FROM Identification i WHERE i.identificationPK.fkIdIdentificationType = :fkIdTipoIdentificacion")
        })
public class Identification implements Serializable
{

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected IdentificationPK identificationPK;
    @Basic(optional = false)
    @Column(name = "numero")
    private int number;
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Person person;
    @JoinColumn(name = "fk_id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private IdentificationType identificationType;

    public Identification()
    {
    }

    public Identification(IdentificationPK identificationPK)
    {
        this.identificationPK = identificationPK;
    }

    public Identification(IdentificationPK identificationPK, int number)
    {
        this.identificationPK = identificationPK;
        this.number = number;
    }

    public Identification(int fkIdPerson, int fkIdIdentificationType)
    {
        this.identificationPK = new IdentificationPK(fkIdPerson, fkIdIdentificationType);
    }

    public IdentificationPK getIdentificationPK()
    {
        return identificationPK;
    }

    public void setIdentificationPK(IdentificationPK identificationPK)
    {
        this.identificationPK = identificationPK;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public Person getPerson()
    {
        return person;
    }

    public void setPerson(Person person)
    {
        this.person = person;
    }

    public IdentificationType getIdentificationType()
    {
        return identificationType;
    }

    public void setIdentificationType(IdentificationType identificationType)
    {
        this.identificationType = identificationType;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (identificationPK != null ? identificationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Identification))
        {
            return false;
        }
        Identification other = (Identification) object;
        if ((this.identificationPK == null && other.identificationPK != null) || (this.identificationPK != null && !this.identificationPK.equals(other.identificationPK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Identificacion[ identificacionPK=" + identificationPK + " ]"
                + "[ numero=" + number + " ]";
    }
}
