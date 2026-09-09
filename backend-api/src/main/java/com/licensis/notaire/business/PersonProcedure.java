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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author matias
 */
@Entity
@Table(name = "tramites_personas")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "TramitesPersonas.findAll", query = "SELECT t FROM PersonProcedure t"),
            @NamedQuery(name = "TramitesPersonas.findByFkIdTramite", query = "SELECT t FROM PersonProcedure t WHERE t.personProcedurePK.fkIdProcedure = :fkIdTramite"),
            @NamedQuery(name = "TramitesPersonas.findByFkIdPersonaCliente", query = "SELECT t FROM PersonProcedure t WHERE t.personProcedurePK.fkIdClientPerson = :fkIdPersonaCliente"),
            @NamedQuery(name = "TramitesPersonas.findByTramiteCliente", query = "SELECT t FROM PersonProcedure t WHERE t.personProcedurePK.fkIdClientPerson = :fkIdPersonaCliente AND t.personProcedurePK.fkIdProcedure = :fkIdTramite"),
        //@NamedQuery(name = "TramitesPersonas.eliminarRegistro", query = "DELETE FROM TramitesPersonas t WHERE t.tramitesPersonasPK.fkIdPersonaCliente = :fkIdPersonaCliente AND t.tramitesPersonasPK.fkIdTramite = :fkIdTramite"),
        })
public class PersonProcedure implements Serializable, Persistable<PersonProcedurePK>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PersonProcedurePK personProcedurePK;
    @Basic(optional = false)
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_persona_cliente", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Person person;
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Procedure procedure;
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
    public PersonProcedurePK getId() {
        return personProcedurePK;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return isNewEntity;
    }


    public PersonProcedure()
    {
    }

    public PersonProcedure(PersonProcedurePK personProcedurePK)
    {
        this.personProcedurePK = personProcedurePK;
    }

    public PersonProcedure(PersonProcedurePK personProcedurePK, String notes)
    {
        this.personProcedurePK = personProcedurePK;
        this.notes = notes;
    }

    public PersonProcedure(int fkIdProcedure, int fkIdClientPerson)
    {
        this.personProcedurePK = new PersonProcedurePK(fkIdProcedure, fkIdClientPerson);
    }

    public PersonProcedurePK getPersonProcedurePK()
    {
        return personProcedurePK;
    }

    public void setPersonProcedurePK(PersonProcedurePK personProcedurePK)
    {
        this.personProcedurePK = personProcedurePK;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Person getPerson()
    {
        return person;
    }

    public void setPerson(Person person)
    {
        this.person = person;
    }

    public Procedure getProcedure()
    {
        return procedure;
    }

    public void setProcedure(Procedure procedure)
    {
        this.procedure = procedure;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (personProcedurePK != null ? personProcedurePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PersonProcedure))
        {
            return false;
        }
        PersonProcedure other = (PersonProcedure) object;
        if ((this.personProcedurePK == null && other.personProcedurePK != null) || (this.personProcedurePK != null && !this.personProcedurePK.equals(other.personProcedurePK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TramitesPersonas[ tramitesPersonasPK=" + personProcedurePK + " ]";
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
