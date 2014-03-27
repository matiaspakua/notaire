/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoSuplencia;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Clase que representa una suplencia. Una suplencia indicia el reemplazado en el desarrollo y administracion
 * de gestiones de un escribano (suplente) por otro (suplantado), en un determinado periodo de tiempo (fecha desde,
 * fecha hasta).
 * @author juanca
 */
@Entity
@Table(name = "suplencias")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "Suplencia.findAll", query = "SELECT s FROM Suplencia s"),
    @NamedQuery(name = "Suplencia.findByIdSuplencia", query = "SELECT s FROM Suplencia s WHERE s.idSuplencia = :idSuplencia"),
    @NamedQuery(name = "Suplencia.findByFechaInicio", query = "SELECT s FROM Suplencia s WHERE s.fechaInicio = :fechaInicio"),
    @NamedQuery(name = "Suplencia.findByFechaFin", query = "SELECT s FROM Suplencia s WHERE s.fechaFin = :fechaFin"),
    @NamedQuery(name = "Suplencia.findSuplenciasPorAnio", query = "SELECT s FROM Suplencia s WHERE s.fechaInicio >= :fechaInicio AND s.fechaFin <= :fechaFin"),
})
public class Suplencia implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @Basic(optional = false)
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;
    @Basic(optional = false)
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.DATE)
    private Date fechaFin;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_suplencia")
    private Integer idSuplencia;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_suplente", referencedColumnName = "id_persona")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Persona fkIdSuplente;
    @JoinColumn(name = "fk_id_suplantado", referencedColumnName = "id_persona")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Persona fkIdSuplantado;

    public Suplencia() {
    }

    public Suplencia(Integer idSuplencia) {
        this.idSuplencia = idSuplencia;
    }

    public Suplencia(Integer idSuplencia, Date fechaInicio, Date fechaFin) {
        this.idSuplencia = idSuplencia;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Integer getIdSuplencia() {
        return idSuplencia;
    }

    public void setIdSuplencia(Integer idSuplencia) {
        this.idSuplencia = idSuplencia;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Persona getFkIdSuplente() {
        return fkIdSuplente;
    }

    public void setFkIdSuplente(Persona fkIdSuplente) {
        this.fkIdSuplente = fkIdSuplente;
    }

    public Persona getFkIdSuplantado() {
        return fkIdSuplantado;
    }

    public void setFkIdSuplantado(Persona fkIdSuplantado) {
        this.fkIdSuplantado = fkIdSuplantado;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSuplencia != null ? idSuplencia.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Suplencia))
        {
            return false;
        }
        Suplencia other = (Suplencia) object;
        if ((this.idSuplencia == null && other.idSuplencia != null) || (this.idSuplencia != null && !this.idSuplencia.equals(other.idSuplencia)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Suplencia[ idSuplencia=" + idSuplencia + " ]";
    }

    public void setAtributos(DtoSuplencia nuevaSuplencia) throws DtoInvalidoException {
        if (nuevaSuplencia.isValido())
        {
            this.setFechaInicio(nuevaSuplencia.getFechaInicio());
            this.setFechaFin(nuevaSuplencia.getFechaFin());
            this.setObservaciones(nuevaSuplencia.getObservaciones());

            if (nuevaSuplencia.getPersonasByFkIdSuplantado().isValido())
            {
                Persona escribanoSuplantado = new Persona();
                escribanoSuplantado.setAtributos(nuevaSuplencia.getPersonasByFkIdSuplantado());
                this.setFkIdSuplantado(escribanoSuplantado);
            }

            if (nuevaSuplencia.getPersonasByFkIdSuplente().isValido())
            {
                Persona escribanoSuplente = new Persona();
                escribanoSuplente.setAtributos(nuevaSuplencia.getPersonasByFkIdSuplente());
                this.setFkIdSuplente(escribanoSuplente);
            }
        }
        else
        {
            throw new DtoInvalidoException("El Dto Suplencia es invalido");
        }
    }

    public DtoSuplencia getDto() {
        DtoSuplencia valoresSuplencia = new DtoSuplencia();

        valoresSuplencia.setIdSuplencia(idSuplencia);
        valoresSuplencia.setFechaInicio(fechaInicio);
        valoresSuplencia.setFechaFin(fechaFin);
        valoresSuplencia.setObservaciones(observaciones);
        valoresSuplencia.setPersonasByFkIdSuplantado(fkIdSuplantado.getDto());
        valoresSuplencia.setPersonasByFkIdSuplente(fkIdSuplente.getDto());

        return valoresSuplencia;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
