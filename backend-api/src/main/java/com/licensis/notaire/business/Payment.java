/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoPayment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
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
import org.springframework.data.domain.Persistable;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "pagos")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Pago.findAll", query = "SELECT p FROM Payment p"),
            @NamedQuery(name = "Pago.findByIdPago", query = "SELECT p FROM Payment p WHERE p.idPayment = :idPago"),
            @NamedQuery(name = "Pago.findByMonto", query = "SELECT p FROM Payment p WHERE p.amount = :monto"),
            @NamedQuery(name = "Pago.findByFecha", query = "SELECT p FROM Payment p WHERE p.date = :fecha"),
            @NamedQuery(name = "Pago.findByPresupuesto", query = "SELECT p FROM Payment p WHERE p.fkIdBudget.idBudget = :idPresupuesto")
        })
public class Payment implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "monto")
    private float amount;
    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date date;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_pago")
    private Integer idPayment;
    @Column(name = "observaciones")
    private String notes;
    @Column(name = "metodo_pago")
    private String paymentMethod;
    @JoinColumn(name = "fk_id_presupuesto", referencedColumnName = "id_presupuesto")
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Budget fkIdBudget;

    public Payment()
    {
    }

    public Payment(Integer idPayment)
    {
        this.idPayment = idPayment;
    }

    public Payment(Integer idPayment, Float amount, Date date)
    {
        this.idPayment = idPayment;
        this.amount = amount;
        this.date = date;
    }

    public Integer getIdPayment()
    {
        return idPayment;
    }

    public void setIdPayment(Integer idPayment)
    {
        this.idPayment = idPayment;
    }

    @Override
    public Integer getId()
    {
        return idPayment;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 — indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @JsonIgnore
    public boolean isNew()
    {
        return idPayment == null;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
    }

    @JsonIgnore
    public Budget getBudget()
    {
        return fkIdBudget;
    }

    public void setBudget(Budget fkIdBudget)
    {
        this.fkIdBudget = fkIdBudget;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoPayment getDto()
    {
        DtoPayment miDtoPayment = new DtoPayment();

        miDtoPayment.setIdPayment(idPayment);
        miDtoPayment.setDate(date);
        miDtoPayment.setAmount(amount);

        if (notes != null)
        {
            miDtoPayment.setNotes(notes);
        }

        if (paymentMethod != null)
        {
            miDtoPayment.setPaymentMethod(paymentMethod);
        }

        miDtoPayment.setVersion(version);

        return miDtoPayment;
    }

    public void setAtributos(DtoPayment miDtoPayment)
    {

        this.idPayment = miDtoPayment.getIdPayment();
        this.date = miDtoPayment.getDate();
        this.amount = miDtoPayment.getAmount();

        if (miDtoPayment.getNotes() != null)
        {
            this.notes = miDtoPayment.getNotes();
        }

        if (miDtoPayment.getPaymentMethod() != null)
        {
            this.paymentMethod = miDtoPayment.getPaymentMethod();
        }

        if (miDtoPayment.getBudget() != null)
        {
            this.fkIdBudget = new Budget();
            this.fkIdBudget.setAtributos(miDtoPayment.getBudget());
        }

        version = miDtoPayment.getVersion();
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idPayment != null ? idPayment.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Payment))
        {
            return false;
        }
        Payment other = (Payment) object;
        if ((this.idPayment == null && other.idPayment != null) || (this.idPayment != null && !this.idPayment.equals(other.idPayment)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Pago[ idPago=" + idPayment
                + ", Fecha pago: " + this.getDate()
                + ", Monto pago: " + this.amount
                + ", Observaciones: " + this.notes + "]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public float getAmount()
    {
        return amount;
    }

    public void setAmount(float amount)
    {
        this.amount = amount;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
}
