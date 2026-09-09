/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoSubstitution;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Clase que representa una suplencia. Una suplencia indicia el reemplazado en el desarrollo y
 * administracion
 * de gestiones de un escribano (suplente) por otro (suplantado), en un determinado periodo de
 * tiempo (fecha desde,
 * fecha hasta).
 *
 * @author juanca
 */
@Entity
@Table(name = "suplencias")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Suplencia.findAll", query = "SELECT s FROM Substitution s"),
            @NamedQuery(name = "Suplencia.findByIdSuplencia", query = "SELECT s FROM Substitution s WHERE s.idSubstitution = :idSuplencia"),
            @NamedQuery(name = "Suplencia.findByFechaInicio", query = "SELECT s FROM Substitution s WHERE s.dateStart = :fechaInicio"),
            @NamedQuery(name = "Suplencia.findByFechaFin", query = "SELECT s FROM Substitution s WHERE s.dateEnd = :fechaFin"),
            @NamedQuery(name = "Suplencia.findSuplenciasPorAnio", query = "SELECT s FROM Substitution s WHERE s.dateStart >= :fechaInicio AND s.dateEnd <= :fechaFin"),
        })
public class Substitution implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @Basic(optional = false)
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date dateStart;
    @Basic(optional = false)
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.DATE)
    private Date dateEnd;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_suplencia")
    private Integer idSubstitution;
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_suplente", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Person fkIdSubstitute;
    @JoinColumn(name = "fk_id_suplantado", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Person fkIdSubstituted;

    public Substitution()
    {
    }

    public Substitution(Integer idSubstitution)
    {
        this.idSubstitution = idSubstitution;
    }

    public Substitution(Integer idSubstitution, Date dateStart, Date dateEnd)
    {
        this.idSubstitution = idSubstitution;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idSubstitution;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idSubstitution == null || idSubstitution.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdSubstitution()
    {
        return idSubstitution;
    }

    public void setIdSubstitution(Integer idSubstitution)
    {
        this.idSubstitution = idSubstitution;
    }

    public Date getDateStart()
    {
        return dateStart;
    }

    public void setDateStart(Date dateStart)
    {
        this.dateStart = dateStart;
    }

    public Date getDateEnd()
    {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd)
    {
        this.dateEnd = dateEnd;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Person getFkIdSubstitute()
    {
        return fkIdSubstitute;
    }

    public void setFkIdSubstitute(Person fkIdSubstitute)
    {
        this.fkIdSubstitute = fkIdSubstitute;
    }

    public Person getFkIdSubstituted()
    {
        return fkIdSubstituted;
    }

    public void setFkIdSubstituted(Person fkIdSubstituted)
    {
        this.fkIdSubstituted = fkIdSubstituted;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idSubstitution != null ? idSubstitution.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Substitution))
        {
            return false;
        }
        Substitution other = (Substitution) object;
        if ((this.idSubstitution == null && other.idSubstitution != null) || (this.idSubstitution != null && !this.idSubstitution.equals(other.idSubstitution)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Suplencia[ idSuplencia=" + idSubstitution + " ]";
    }

    public void setAtributos(DtoSubstitution nuevaSubstitution) throws DtoInvalidoException
    {
        if (nuevaSubstitution.isValido())
        {
            this.setDateStart(nuevaSubstitution.getDateStart());
            this.setDateEnd(nuevaSubstitution.getDateEnd());
            this.setNotes(nuevaSubstitution.getNotes());

            if (nuevaSubstitution.getPersonsByFkIdSubstituted().isValido())
            {
                Person notarySuplantado = new Person();
                notarySuplantado.setAtributos(nuevaSubstitution.getPersonsByFkIdSubstituted());
                this.setFkIdSubstituted(notarySuplantado);
            }

            if (nuevaSubstitution.getPersonsByFkIdSubstitute().isValido())
            {
                Person notarySuplente = new Person();
                notarySuplente.setAtributos(nuevaSubstitution.getPersonsByFkIdSubstitute());
                this.setFkIdSubstitute(notarySuplente);
            }
        } else
        {
            throw new DtoInvalidoException("El Dto Suplencia es invalido");
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoSubstitution getDto()
    {
        DtoSubstitution valoresSubstitution = new DtoSubstitution();

        valoresSubstitution.setIdSubstitution(idSubstitution);
        valoresSubstitution.setDateStart(dateStart);
        valoresSubstitution.setDateEnd(dateEnd);
        valoresSubstitution.setNotes(notes);
        valoresSubstitution.setPersonsByFkIdSubstituted(fkIdSubstituted.getDto());
        valoresSubstitution.setPersonsByFkIdSubstitute(fkIdSubstitute.getDto());

        return valoresSubstitution;
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
