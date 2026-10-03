/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoPago;
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
@Table(name = "pagos")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Pago.findAll", query = "SELECT p FROM Pago p"),
            @NamedQuery(name = "Pago.findByIdPago", query = "SELECT p FROM Pago p WHERE p.idPago = :idPago"),
            @NamedQuery(name = "Pago.findByMonto", query = "SELECT p FROM Pago p WHERE p.monto = :monto"),
            @NamedQuery(name = "Pago.findByFecha", query = "SELECT p FROM Pago p WHERE p.fecha = :fecha"),
            @NamedQuery(name = "Pago.findByPresupuesto", query = "SELECT p FROM Pago p WHERE p.fkIdPresupuesto.idPresupuesto = :idPresupuesto")
        })
public class Pago implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "monto")
    private float monto;
    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_pago")
    private Integer idPago;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_presupuesto", referencedColumnName = "id_presupuesto")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Presupuesto fkIdPresupuesto;

    public Pago()
    {
    }

    public Pago(Integer idPago)
    {
        this.idPago = idPago;
    }

    public Pago(Integer idPago, Float monto, Date fecha)
    {
        this.idPago = idPago;
        this.monto = monto;
        this.fecha = fecha;
    }

    public Integer getIdPago()
    {
        return idPago;
    }

    public void setIdPago(Integer idPago)
    {
        this.idPago = idPago;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public Presupuesto getPresupuesto()
    {
        return fkIdPresupuesto;
    }

    public void setPresupuesto(Presupuesto fkIdPresupuesto)
    {
        this.fkIdPresupuesto = fkIdPresupuesto;
    }

    public DtoPago getDto()
    {
        DtoPago miDtoPago = new DtoPago();

        miDtoPago.setIdPago(idPago);
        miDtoPago.setFecha(fecha);
        miDtoPago.setMonto(monto);

        if (observaciones != null)
        {
            miDtoPago.setObservaciones(observaciones);
        }

        miDtoPago.setVersion(version);

        return miDtoPago;
    }

    public void setAtributos(DtoPago miDtoPago)
    {

        this.idPago = miDtoPago.getIdPago();
        this.fecha = miDtoPago.getFecha();
        this.monto = miDtoPago.getMonto();

        if (miDtoPago.getObservaciones() != null)
        {
            this.observaciones = miDtoPago.getObservaciones();
        }

        if (miDtoPago.getPresupuesto() != null)
        {
            this.fkIdPresupuesto = new Presupuesto();
            this.fkIdPresupuesto.setAtributos(miDtoPago.getPresupuesto());
        }

        version = miDtoPago.getVersion();
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idPago != null ? idPago.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pago))
        {
            return false;
        }
        Pago other = (Pago) object;
        if ((this.idPago == null && other.idPago != null) || (this.idPago != null && !this.idPago.equals(other.idPago)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Pago[ idPago=" + idPago
                + ", Fecha pago: " + this.getFecha()
                + ", Monto pago: " + this.monto
                + ", Observaciones: " + this.observaciones + "]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public float getMonto()
    {
        return monto;
    }

    public void setMonto(float monto)
    {
        this.monto = monto;
    }

    public Date getFecha()
    {
        return fecha;
    }

    public void setFecha(Date fecha)
    {
        this.fecha = fecha;
    }
}
