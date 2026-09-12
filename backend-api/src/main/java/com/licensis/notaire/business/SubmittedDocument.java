/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoSubmittedDocument;
import com.licensis.notaire.dto.DtoDocumentType;
import com.licensis.notaire.dto.DtoProcedure;
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
 *
 * @author juanca
 */
@Entity
@Table(name = "documentos_presentados")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "DocumentoPresentado.findAll", query = "SELECT d FROM SubmittedDocument d"),
            @NamedQuery(name = "DocumentoPresentado.findByIdDocumentoPresentado", query = "SELECT d FROM SubmittedDocument d WHERE d.idSubmittedDocument = :idDocumentoPresentado"),
            @NamedQuery(name = "DocumentoPresentado.findByNumeroCarton", query = "SELECT d FROM SubmittedDocument d WHERE d.cardNumber = :numeroCarton"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaIngreso", query = "SELECT d FROM SubmittedDocument d WHERE d.dateEntry = :fechaIngreso"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaSalida", query = "SELECT d FROM SubmittedDocument d WHERE d.dateExit = :fechaSalida"),
            @NamedQuery(name = "DocumentoPresentado.findByPreparado", query = "SELECT d FROM SubmittedDocument d WHERE d.prepared = :preparado"),
            @NamedQuery(name = "DocumentoPresentado.findByVence", query = "SELECT d FROM SubmittedDocument d WHERE d.expires = :vence"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaVencimiento", query = "SELECT d FROM SubmittedDocument d WHERE d.dateDue >= :fechaVencimiento"),
            @NamedQuery(name = "DocumentoPresentado.findByDiasVencimiento", query = "SELECT d FROM SubmittedDocument d WHERE d.dueDays = :diasVencimiento"),
            @NamedQuery(name = "DocumentoPresentado.findByImporteAPagar", query = "SELECT d FROM SubmittedDocument d WHERE d.amountToPay = :importeAPagar"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaPago", query = "SELECT d FROM SubmittedDocument d WHERE d.datePayment = :fechaPago"),
            @NamedQuery(name = "DocumentoPresentado.findByLiberado", query = "SELECT d FROM SubmittedDocument d WHERE d.released = :liberado"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaLiberado", query = "SELECT d FROM SubmittedDocument d WHERE d.dateReleased = :fechaLiberado"),
            @NamedQuery(name = "DocumentoPresentado.findByObservado", query = "SELECT d FROM SubmittedDocument d WHERE d.flagged = :observado")
        })
public class SubmittedDocument implements Serializable, Persistable<Integer>
{

    @Column(name = "fecha_ingreso")
    @Temporal(TemporalType.DATE)
    private Date dateEntry;
    @Column(name = "liberado")
    private Boolean released;
    @Column(name = "observado")
    private Boolean flagged;
    @Column(name = "fk_id_tipo_documento")
    private Integer fkIdDocumentType;
    @Basic(optional = false)
    @Column(name = "quien_entrega")
    private String deliveredBy;
    @Column(name = "reingresado")
    private Boolean reentered;
    @Column(name = "fecha_salida")
    @Temporal(TemporalType.DATE)
    private Date dateExit;
    @Column(name = "fecha_vencimiento")
    @Temporal(TemporalType.DATE)
    private Date dateDue;
    @Column(name = "fecha_pago")
    @Temporal(TemporalType.DATE)
    private Date datePayment;
    @Column(name = "fecha_liberado")
    @Temporal(TemporalType.DATE)
    private Date dateReleased;
    @Column(name = "entregado")
    private Boolean delivered;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_documento_presentado")
    private Integer idSubmittedDocument;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Column(name = "numero_carton")
    private Integer cardNumber;
    @Basic(optional = false)
    @Column(name = "preparado")
    private boolean prepared;
    @Basic(optional = false)
    @Column(name = "vence")
    private boolean expires;
    @Column(name = "dias_vencimiento")
    private Integer dueDays;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "importe_a_pagar")
    private Float amountToPay;
    @Column(name = "observaciones")
    private String notes;
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Procedure fkIdProcedure;

    public SubmittedDocument()
    {
    }

    public SubmittedDocument(Integer idSubmittedDocument)
    {
        this.idSubmittedDocument = idSubmittedDocument;
    }

    public SubmittedDocument(Integer idSubmittedDocument, String name, Date dateEntry, boolean prepared, boolean expires, boolean released, boolean flagged)
    {
        this.idSubmittedDocument = idSubmittedDocument;
        this.name = name;
        //this.fechaIngreso = fechaIngreso;
        this.prepared = prepared;
        this.expires = expires;
        this.released = released;
        this.flagged = flagged;
        this.reentered = reentered;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idSubmittedDocument;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idSubmittedDocument == null || idSubmittedDocument.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdSubmittedDocument()
    {
        return idSubmittedDocument;
    }

    public void setIdSubmittedDocument(Integer idSubmittedDocument)
    {
        this.idSubmittedDocument = idSubmittedDocument;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getCardNumber()
    {
        return cardNumber;
    }

    public void setCardNumber(Integer cardNumber)
    {
        this.cardNumber = cardNumber;
    }

    public Date getDateExit()
    {
        return dateExit;
    }

    public void setDateExit(Date dateExit)
    {
        this.dateExit = dateExit;
    }

    public boolean getPrepared()
    {
        return prepared;
    }

    public void setPrepared(boolean prepared)
    {
        this.prepared = prepared;
    }

    public boolean getExpires()
    {
        return expires;
    }

    public void setExpires(boolean expires)
    {
        this.expires = expires;
    }

    public Date getDateDue()
    {
        return dateDue;
    }

    public void setDateDue(Date dateDue)
    {
        this.dateDue = dateDue;
    }

    public Integer getDueDays()
    {
        return dueDays;
    }

    public void setDueDays(Integer dueDays)
    {
        this.dueDays = dueDays;
    }

    public Float getAmountToPay()
    {
        return amountToPay;
    }

    public void setAmountToPay(Float amountToPay)
    {
        this.amountToPay = amountToPay;
    }

    public Date getDatePayment()
    {
        return datePayment;
    }

    public void setDatePayment(Date datePayment)
    {
        this.datePayment = datePayment;
    }

    public Date getDateReleased()
    {
        return dateReleased;
    }

    public void setDateReleased(Date dateReleased)
    {
        this.dateReleased = dateReleased;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Procedure getFkIdProcedure()
    {
        return fkIdProcedure;
    }

    public void setFkIdProcedure(Procedure fkIdProcedure)
    {
        this.fkIdProcedure = fkIdProcedure;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idSubmittedDocument != null ? idSubmittedDocument.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SubmittedDocument))
        {
            return false;
        }
        SubmittedDocument other = (SubmittedDocument) object;
        if ((this.idSubmittedDocument == null && other.idSubmittedDocument != null) || (this.idSubmittedDocument != null && !this.idSubmittedDocument.equals(other.idSubmittedDocument)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "DocumentoPresentado[ idDocumentoPresentado=" + idSubmittedDocument
                + ", carton: " + this.cardNumber
                + ", fecha ingreso: " + this.dateEntry
                + ", fecha salida: " + this.dateExit
                + ", observado: " + this.flagged
                + ", importe: " + this.amountToPay
                + ", echa pago: " + this.datePayment
                + ", liberado" + this.released
                + ", fecha liberacion: " + this.dateReleased
                + ", observaciones: " + this.notes
                + "]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public Boolean getDelivered()
    {
        return delivered;
    }

    public void setDelivered(Boolean delivered)
    {
        this.delivered = delivered;
    }

    public DtoSubmittedDocument getDto()
    {

        DtoSubmittedDocument dtoSubmittedDocument = new DtoSubmittedDocument();

        dtoSubmittedDocument.setVersion(version);
        dtoSubmittedDocument.setDueDays(dueDays);
        dtoSubmittedDocument.setDateEntry(dateEntry);
        dtoSubmittedDocument.setDateReleased(dateReleased);
        dtoSubmittedDocument.setDatePayment(datePayment);
        dtoSubmittedDocument.setDateExit(dateExit);
        dtoSubmittedDocument.setDateDue(dateDue);
        dtoSubmittedDocument.setIdSubmittedDocument(idSubmittedDocument);
        dtoSubmittedDocument.setAmountApagar(amountToPay);
        dtoSubmittedDocument.setReleased(released);
        dtoSubmittedDocument.setName(name);
        dtoSubmittedDocument.setCardNumber(cardNumber);
        dtoSubmittedDocument.setNotes(notes);
        dtoSubmittedDocument.setFlagged(flagged);
        dtoSubmittedDocument.setPrepared(prepared);
        dtoSubmittedDocument.setExpires(expires);
        dtoSubmittedDocument.setDeliveredBy(deliveredBy);

        if (delivered != null)
        {
            dtoSubmittedDocument.setDelivered(delivered);
        }

        if (reentered != null)
        {
            dtoSubmittedDocument.setReentered(reentered);
        }

        DtoProcedure dtoProcedure = new DtoProcedure();
        dtoProcedure = fkIdProcedure.getDto();
        dtoSubmittedDocument.setFkProcedure(dtoProcedure);

        DtoDocumentType dtoDocumentType = new DtoDocumentType();

        return dtoSubmittedDocument;
    }

    public void setAtributos(DtoSubmittedDocument dtoSubmittedDocument)
    {

        version = dtoSubmittedDocument.getVersion();
        idSubmittedDocument = dtoSubmittedDocument.getIdSubmittedDocument();
        name = dtoSubmittedDocument.getName();
        cardNumber = dtoSubmittedDocument.getCardNumber();
        dateEntry = dtoSubmittedDocument.getDateEntry();
        dateExit = dtoSubmittedDocument.getDateExit();
        prepared = dtoSubmittedDocument.isPrepared();
        expires = dtoSubmittedDocument.isExpires();
        dateDue = dtoSubmittedDocument.getDateDue();
        dueDays = dtoSubmittedDocument.getDueDays();
        amountToPay = dtoSubmittedDocument.getAmountToPay();
        datePayment = dtoSubmittedDocument.getDatePayment();
        released = dtoSubmittedDocument.isReleased();
        dateReleased = dtoSubmittedDocument.getDateReleased();
        flagged = dtoSubmittedDocument.isFlagged();
        notes = dtoSubmittedDocument.getNotes();
        reentered = dtoSubmittedDocument.isReentered();
        deliveredBy = dtoSubmittedDocument.getDeliveredBy();
        delivered = dtoSubmittedDocument.isDelivered();
        idSubmittedDocument = dtoSubmittedDocument.getIdSubmittedDocument();

        Procedure procedure = new Procedure();
        procedure.setAtributos(dtoSubmittedDocument.getFkProcedure());

        fkIdProcedure = procedure;
    }

    public Boolean getReentered()
    {
        return reentered;
    }

    public void setReentered(Boolean reentered)
    {
        this.reentered = reentered;
    }

    public String getDeliveredBy()
    {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy)
    {
        this.deliveredBy = deliveredBy;
    }

    public int getFkIdDocumentType()
    {
        return fkIdDocumentType;
    }

    public Integer getFkIdDocumentTypeNullable()
    {
        return fkIdDocumentType;
    }

    public void setFkIdDocumentType(int fkIdDocumentType)
    {
        this.fkIdDocumentType = fkIdDocumentType;
    }

    public Boolean getReleased()
    {
        return released;
    }

    public void setReleased(Boolean released)
    {
        this.released = released;
    }

    public Boolean getFlagged()
    {
        return flagged;
    }

    public void setFlagged(Boolean flagged)
    {
        this.flagged = flagged;
    }

    public void setFkIdDocumentType(Integer fkIdDocumentType)
    {
        this.fkIdDocumentType = fkIdDocumentType;
    }

    public Date getDateEntry()
    {
        return dateEntry;
    }

    public void setDateEntry(Date dateEntry)
    {
        this.dateEntry = dateEntry;
    }
}
