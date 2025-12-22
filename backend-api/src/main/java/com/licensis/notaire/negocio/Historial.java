/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoHistorial;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Clase que representa el historial (registro de cambios de estado) de una gestion de escritura.
 * Cada instancia del historial de una misma gestion de escritura, representa un cambio de estado de
 * la gestion.
 *
 * @author juanca
 */
@Entity
@Table(name = "historial")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Historial.findAll", query = "SELECT h FROM Historial h"),
            @NamedQuery(name = "Historial.findByIdHistorial", query = "SELECT h FROM Historial h WHERE h.idHistorial = :idHistorial"),
            @NamedQuery(name = "Historial.findByIdGestion", query = "SELECT h FROM Historial h WHERE h.fkIdGestion.idGestion = :idGestion"),
            @NamedQuery(name = "Historial.findByFecha", query = "SELECT h FROM Historial h WHERE h.fecha = :fecha")
        })
public class Historial implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_historial")
    private Integer idHistorial;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_estado_gestion", referencedColumnName = "id_estado_gestion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private EstadoDeGestion fkIdEstadoGestion;
    @JoinColumn(name = "fk_id_gestion", referencedColumnName = "id_gestion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private GestionDeEscritura fkIdGestion;

    /**
     * Constructor por default para historial. Asigna al ID el valor de
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public Historial()
    {
        this.idHistorial = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
    }

    public Historial(Integer idHistorial)
    {
        this.idHistorial = idHistorial;
    }

    public Historial(Integer idHistorial, Date fecha)
    {
        this.idHistorial = idHistorial;
        this.fecha = fecha;
    }

    public Integer getIdHistorial()
    {
        return idHistorial;
    }

    public void setIdHistorial(Integer idHistorial)
    {
        this.idHistorial = idHistorial;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public EstadoDeGestion getFkIdEstadoGestion()
    {
        return fkIdEstadoGestion;
    }

    public void setFkIdEstadoGestion(EstadoDeGestion fkIdEstadoGestion)
    {
        this.fkIdEstadoGestion = fkIdEstadoGestion;
    }

    public GestionDeEscritura getFkIdGestion()
    {
        return fkIdGestion;
    }

    public void setFkIdGestion(GestionDeEscritura fkIdGestion)
    {
        this.fkIdGestion = fkIdGestion;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public Date getFecha()
    {
        return fecha;
    }

    public void setFecha(Date fecha)
    {
        this.fecha = fecha;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (getIdHistorial() != null ? getIdHistorial().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Historial))
        {
            return false;
        }
        Historial other = (Historial) object;
        if ((this.getIdHistorial() == null && other.getIdHistorial() != null) || (this.getIdHistorial() != null && !this.idHistorial.equals(other.idHistorial)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Historial[ idHistorial=" + getIdHistorial() + " ]";
    }

    public DtoHistorial getDto()
    {
        DtoHistorial dto = new DtoHistorial();

        dto.setIdHistorial(this.idHistorial);
        dto.setVersion(this.version);
        dto.setFecha(this.fecha);
        dto.setGestionesDeEscrituras(this.getFkIdGestion().getDto());
        dto.setEstadosDeGestion(this.fkIdEstadoGestion.getDto());
        dto.setObservaciones(this.observaciones);

        return dto;
    }

    public void setAtributos(DtoHistorial dto)
    {
    }
}
