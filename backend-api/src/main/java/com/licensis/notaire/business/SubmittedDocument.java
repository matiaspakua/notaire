/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import java.math.BigDecimal;

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
@Table(name = "submitted_documents")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "SubmittedDocument.findAll", query = "SELECT d FROM SubmittedDocument d"),
            @NamedQuery(name = "SubmittedDocument.findByIdSubmittedDocument", query = "SELECT d FROM SubmittedDocument d WHERE d.idSubmittedDocument = :idDocumentoPresentado"),
            @NamedQuery(name = "SubmittedDocument.findByCardNumber", query = "SELECT d FROM SubmittedDocument d WHERE d.cardNumber = :numeroCarton"),
            @NamedQuery(name = "SubmittedDocument.findByEntryDate", query = "SELECT d FROM SubmittedDocument d WHERE d.dateEntry = :fechaIngreso"),
            @NamedQuery(name = "SubmittedDocument.findByExitDate", query = "SELECT d FROM SubmittedDocument d WHERE d.dateExit = :fechaSalida"),
            @NamedQuery(name = "SubmittedDocument.findByPrepared", query = "SELECT d FROM SubmittedDocument d WHERE d.prepared = :prepared"),
            @NamedQuery(name = "SubmittedDocument.findByExpires", query = "SELECT d FROM SubmittedDocument d WHERE d.expires = :vence"),
            @NamedQuery(name = "SubmittedDocument.findByDueDate", query = "SELECT d FROM SubmittedDocument d WHERE d.dateDue >= :fechaVencimiento"),
            @NamedQuery(name = "SubmittedDocument.findByDueDays", query = "SELECT d FROM SubmittedDocument d WHERE d.dueDays = :diasVencimiento"),
            @NamedQuery(name = "SubmittedDocument.findByAmountToPay", query = "SELECT d FROM SubmittedDocument d WHERE d.amountToPay = :importeAPagar"),
            @NamedQuery(name = "SubmittedDocument.findByPaymentDate", query = "SELECT d FROM SubmittedDocument d WHERE d.datePayment = :fechaPago"),
            @NamedQuery(name = "SubmittedDocument.findByReleased", query = "SELECT d FROM SubmittedDocument d WHERE d.released = :released"),
            @NamedQuery(name = "SubmittedDocument.findByReleasedDate", query = "SELECT d FROM SubmittedDocument d WHERE d.dateReleased = :fechaLiberado"),
            @NamedQuery(name = "SubmittedDocument.findByFlagged", query = "SELECT d FROM SubmittedDocument d WHERE d.flagged = :flagged")
        })
public class SubmittedDocument implements Serializable, Persistable<Integer>
{

    @Column(name = "entry_date")
    @Temporal(TemporalType.DATE)
    private Date dateEntry;
    @Column(name = "released")
    private Boolean released;
    @Column(name = "observed")
    private Boolean flagged;
    @JoinColumn(name = "fk_id_document_type", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private DocumentType documentType;
    @Basic(optional = false)
    @Column(name = "delivered_by")
    private String deliveredBy;
    @Column(name = "reentered")
    private Boolean reentered;
    @Column(name = "exit_date")
    @Temporal(TemporalType.DATE)
    private Date dateExit;
    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dateDue;
    @Column(name = "payment_date")
    @Temporal(TemporalType.DATE)
    private Date datePayment;
    @Column(name = "released_date")
    @Temporal(TemporalType.DATE)
    private Date dateReleased;
    @Column(name = "delivered")
    private Boolean delivered;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer idSubmittedDocument;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Column(name = "folder_number")
    private Integer cardNumber;
    @Basic(optional = false)
    @Column(name = "prepared")
    private boolean prepared;
    @Basic(optional = false)
    @Column(name = "expires")
    private boolean expires;
    @Column(name = "due_days")
    private Integer dueDays;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "amount_to_pay")
    private BigDecimal amountToPay;
    @Column(name = "notes")
    private String notes;
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id")
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

    public BigDecimal getAmountToPay()
    {
        return amountToPay;
    }

    public void setAmountToPay(BigDecimal amountToPay)
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
        return "SubmittedDocument[ idSubmittedDocument=" + idSubmittedDocument
                + ", folderNumber: " + this.cardNumber
                + ", entryDate: " + this.dateEntry
                + ", exitDate: " + this.dateExit
                + ", flagged: " + this.flagged
                + ", amount: " + this.amountToPay
                + ", paymentDate: " + this.datePayment
                + ", released: " + this.released
                + ", releasedDate: " + this.dateReleased
                + ", notes: " + this.notes
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

    public DocumentType getDocumentType()
    {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType)
    {
        this.documentType = documentType;
    }

    /**
     * Compatibility accessor for callers that still use the legacy document-type id API.
     * Returns 0 when the association is unset (legacy primitive return type).
     */
    public int getFkIdDocumentType()
    {
        Integer id = getFkIdDocumentTypeNullable();
        return id != null ? id : 0;
    }

    public Integer getFkIdDocumentTypeNullable()
    {
        return documentType != null ? documentType.getIdDocumentType() : null;
    }

    public void setFkIdDocumentType(int fkIdDocumentType)
    {
        setFkIdDocumentType(Integer.valueOf(fkIdDocumentType));
    }

    public void setFkIdDocumentType(Integer fkIdDocumentType)
    {
        if (fkIdDocumentType == null) {
            this.documentType = null;
        } else {
            this.documentType = new DocumentType(fkIdDocumentType);
        }
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

    public Date getDateEntry()
    {
        return dateEntry;
    }

    public void setDateEntry(Date dateEntry)
    {
        this.dateEntry = dateEntry;
    }
}
