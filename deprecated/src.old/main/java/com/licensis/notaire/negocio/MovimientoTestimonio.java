/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoTestimonio;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "movimientos_testimonio")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "MovimientoTestimonio.findAll", query = "SELECT m FROM MovimientoTestimonio m"),
            @NamedQuery(name = "MovimientoTestimonio.findByIdMovimientoTestimonio", query = "SELECT m FROM MovimientoTestimonio m WHERE m.idMovimientoTestimonio = :idMovimientoTestimonio"),
            @NamedQuery(name = "MovimientoTestimonio.findByFechaIngreso", query = "SELECT m FROM MovimientoTestimonio m WHERE m.fechaIngreso = :fechaIngreso"),
            @NamedQuery(name = "MovimientoTestimonio.findByFechaSalida", query = "SELECT m FROM MovimientoTestimonio m WHERE m.fechaSalida = :fechaSalida"),
            @NamedQuery(name = "MovimientoTestimonio.findByFechaInscripcion", query = "SELECT m FROM MovimientoTestimonio m WHERE m.fechaInscripcion = :fechaInscripcion"),
            @NamedQuery(name = "MovimientoTestimonio.findByInscripta", query = "SELECT m FROM MovimientoTestimonio m WHERE m.inscripta = :inscripta"),
            @NamedQuery(name = "MovimientoTestimonio.findByTestimonio", query = "SELECT m FROM MovimientoTestimonio m WHERE m.fkIdTestimonio.idTestimonio = :idTestimonio"),
            @NamedQuery(name = "MovimientoTestimonio.findByNumeroCarton", query = "SELECT m FROM MovimientoTestimonio m WHERE m.numeroCarton = :numeroCarton")
        })
public class MovimientoTestimonio implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "fecha_ingreso")
    @Temporal(TemporalType.DATE)
    private Date fechaIngreso;
    @Column(name = "fecha_salida")
    @Temporal(TemporalType.DATE)
    private Date fechaSalida;
    @Column(name = "fecha_inscripcion")
    @Temporal(TemporalType.DATE)
    private Date fechaInscripcion;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_movimiento_testimonio")
    private Integer idMovimientoTestimonio;
    @Basic(optional = false)
    @Column(name = "inscripta")
    private boolean inscripta;
    @Basic(optional = false)
    @Column(name = "numero_carton")
    private int numeroCarton;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_testimonio", referencedColumnName = "id_testimonio")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Testimonio fkIdTestimonio;

    public MovimientoTestimonio()
    {
    }

    public MovimientoTestimonio(Integer idMovimientoTestimonio)
    {
        this.idMovimientoTestimonio = idMovimientoTestimonio;
    }

    public MovimientoTestimonio(Integer idMovimientoTestimonio, Date fechaIngreso, boolean inscripta, int numeroCarton)
    {
        this.idMovimientoTestimonio = idMovimientoTestimonio;
        this.fechaIngreso = fechaIngreso;
        this.inscripta = inscripta;
        this.numeroCarton = numeroCarton;
    }

    public Integer getIdMovimientoTestimonio()
    {
        return idMovimientoTestimonio;
    }

    public void setIdMovimientoTestimonio(Integer idMovimientoTestimonio)
    {
        this.idMovimientoTestimonio = idMovimientoTestimonio;
    }

    public Date getFechaIngreso()
    {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso)
    {
        this.fechaIngreso = fechaIngreso;
    }

    public Date getFechaSalida()
    {
        return fechaSalida;
    }

    public void setFechaSalida(Date fechaSalida)
    {
        this.fechaSalida = fechaSalida;
    }

    public Date getFechaInscripcion()
    {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(Date fechaInscripcion)
    {
        this.fechaInscripcion = fechaInscripcion;
    }

    public boolean getInscripta()
    {
        return inscripta;
    }

    public void setInscripta(boolean inscripta)
    {
        this.inscripta = inscripta;
    }

    public int getNumeroCarton()
    {
        return numeroCarton;
    }

    public void setNumeroCarton(int numeroCarton)
    {
        this.numeroCarton = numeroCarton;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public Testimonio getTestimonio()
    {
        return fkIdTestimonio;
    }

    public void setTestimonio(Testimonio fkIdTestimonio)
    {
        this.fkIdTestimonio = fkIdTestimonio;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idMovimientoTestimonio != null ? idMovimientoTestimonio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MovimientoTestimonio))
        {
            return false;
        }
        MovimientoTestimonio other = (MovimientoTestimonio) object;
        if ((this.idMovimientoTestimonio == null && other.idMovimientoTestimonio != null) || (this.idMovimientoTestimonio != null && !this.idMovimientoTestimonio.equals(other.idMovimientoTestimonio)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MovimientoTestimonio[ idMovimientoTestimonio=" + idMovimientoTestimonio + " ]"
                + "[ idTestimonio=" + fkIdTestimonio.getIdTestimonio() + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public DtoMovimientoTestimonio getDto()
    {
        DtoMovimientoTestimonio miDto = new DtoMovimientoTestimonio();

        miDto.setIdMovimientoTestimonio(idMovimientoTestimonio);
        miDto.setFechaIngreso(fechaIngreso);
        miDto.setFechaInscripcion(fechaInscripcion);
        miDto.setFechaSalida(fechaSalida);
        miDto.setInscripta(inscripta);
        miDto.setNumeroCarton(numeroCarton);
        miDto.setObservaciones(observaciones);
        miDto.setVersion(version);

        DtoTestimonio miDtoTestimonio = new DtoTestimonio();
        miDtoTestimonio.setIdTestimonio(fkIdTestimonio.getIdTestimonio());
        miDto.setTestimonio(miDtoTestimonio);

        return miDto;
    }

    public void setAtributos(DtoMovimientoTestimonio miDto)
    {

        if (miDto.getIdMovimientoTestimonio() != null)
        {
            idMovimientoTestimonio = miDto.getIdMovimientoTestimonio();
        }

        fechaIngreso = miDto.getFechaIngreso();
        fechaInscripcion = miDto.getFechaInscripcion();
        fechaSalida = miDto.getFechaSalida();
        inscripta = miDto.isInscripta();
        numeroCarton = miDto.getNumeroCarton();
        observaciones = miDto.getObservaciones();
        version = miDto.getVersion();

        if (miDto.getTestimonio() != null)
        {
            fkIdTestimonio = new Testimonio(miDto.getTestimonio().getIdTestimonio());
        }
    }
}
