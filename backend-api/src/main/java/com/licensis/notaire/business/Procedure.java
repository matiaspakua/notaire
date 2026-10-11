/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoSubmittedDocument;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoBudget;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoProcedure;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.springframework.data.domain.Persistable;

/**
 * A procedure that is underway (a concrete case, not the procedure-type
 * definition). It references the procedure type, the client, the budget it
 * came from, and the management number.
 * <p>
 * Business rule:
 * <p>
 * Creating a budget inserts a row in {@code procedures} that links that budget
 * to a procedure and the chosen procedure type.
 * <p>
 * Starting a management associates it with the procedure rows of the selected
 * budgets. {@code DeedManagement} and {@code Procedure} together are the
 * management.
 * <p>
 * A management can involve several clients. The {@code person_procedures}
 * table stores that link. {@code personProcedureList} is the persistence mapping
 * for that table.
 *
 * @author juanca
 */
@Entity
@Table(name = "procedures")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Procedure.findAll", query = "SELECT t FROM Procedure t"),
        @NamedQuery(name = "Procedure.findByIdProcedure", query = "SELECT t FROM Procedure t WHERE t.idProcedure = :idProcedure"),
        @NamedQuery(name = "Procedure.findByIdBudget", query = "SELECT t FROM Procedure t WHERE t.fkIdBudget.idBudget = :idBudget")
})
public class Procedure implements Serializable, Persistable<Integer> {

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "procedure", fetch = FetchType.LAZY)
    private List<PersonProcedure> personProcedureList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer idProcedure;
    @Column(name = "notes")
    private String notes;
    @JoinTable(name = "person_procedures", joinColumns = {
            @JoinColumn(name = "fk_id_procedure", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "fk_id_client_person", referencedColumnName = "id")
    })
    @ManyToMany(fetch = FetchType.LAZY)
    private List<Person> personList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdProcedure", fetch = FetchType.LAZY)
    private List<SubmittedDocument> submittedDocumentList;
    @JoinColumn(name = "fk_id_property", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Property fkIdProperty;
    @JoinColumn(name = "fk_id_budget", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Budget fkIdBudget;
    @JoinColumn(name = "fk_id_deed", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Deed fkIdDeed;
    @JoinColumn(name = "fk_id_deed_management", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    private DeedManagement fkIdManagement;
    @JoinColumn(name = "fk_id_procedure_type", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private ProcedureType fkIdProcedureType;

    /**
     * Default constructor. Initializes the id from
     * {@link BusinessConstants#INVALID_OBJECT_ID}, and the lists.
     */
    public Procedure() {
        this.idProcedure = BusinessConstants.INVALID_OBJECT_ID;
        this.submittedDocumentList = new ArrayList<>();
        this.personList = new ArrayList<>();
    }

    public Procedure(Integer idProcedure) {
        this.idProcedure = idProcedure;
    }

    public Integer getIdProcedure() {
        return idProcedure;
    }

    public void setIdProcedure(Integer idProcedure) {
        this.idProcedure = idProcedure;
    }

    @Override
    public Integer getId() {
        return idProcedure;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 — indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @JsonIgnore
    public boolean isNew() {
        return idProcedure == null || idProcedure.equals(BusinessConstants.INVALID_OBJECT_ID);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @XmlTransient
    @JsonIgnore
    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    @XmlTransient
    @JsonIgnore
    public List<SubmittedDocument> getSubmittedDocumentList() {
        return submittedDocumentList;
    }

    public void setSubmittedDocumentList(List<SubmittedDocument> submittedDocumentList) {
        this.submittedDocumentList = submittedDocumentList;
    }

    public Property getFkIdProperty() {
        return fkIdProperty;
    }

    public void setFkIdProperty(Property fkIdProperty) {
        this.fkIdProperty = fkIdProperty;
    }

    public Budget getFkIdBudget() {
        return fkIdBudget;
    }

    public void setFkIdBudget(Budget fkIdBudget) {
        this.fkIdBudget = fkIdBudget;
    }

    public Deed getFkIdDeed() {
        return fkIdDeed;
    }

    public void setFkIdDeed(Deed fkIdDeed) {
        this.fkIdDeed = fkIdDeed;
    }

    public DeedManagement getFkIdManagement() {
        return fkIdManagement;
    }

    public void setFkIdManagement(DeedManagement fkIdManagement) {
        this.fkIdManagement = fkIdManagement;
    }

    public ProcedureType getFkIdProcedureType() {
        return fkIdProcedureType;
    }

    public void setFkIdProcedureType(ProcedureType fkIdProcedureType) {
        this.fkIdProcedureType = fkIdProcedureType;
    }



    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoProcedure getDto() {
        DtoProcedure dto = new DtoProcedure();

        dto.setIdProcedure(getIdProcedure());
        dto.setNotes(notes);
        dto.setProcedureType(fkIdProcedureType.getDto());

        if (fkIdDeed != null) {
            DtoDeed deedDto = new DtoDeed();

            deedDto.setIdDeed(fkIdDeed.getIdDeed());
            deedDto.setNumber(fkIdDeed.getNumber());

            dto.setDeed(deedDto);
        }

        if (fkIdManagement != null) {
            DtoDeedManagement deedManagementDto = new DtoDeedManagement();

            deedManagementDto.setIdManagement(fkIdManagement.getIdManagement());
            deedManagementDto.setNumber(fkIdManagement.getNumber());

            DtoPerson notary = new DtoPerson();
            notary.setId(fkIdManagement.getFkIdNotaryPerson().getPersonId());
            notary.setNotaryRegistrationNumber(fkIdManagement.getFkIdNotaryPerson().getNotaryRegistrationNumber());

            deedManagementDto.setPersonNotary(notary);

            dto.setDeedManagement(deedManagementDto);
        }

        if (fkIdProperty != null) {
            dto.setProperty(fkIdProperty.getDto());
        } else {
            dto.setProperty(null);
        }

        if (this.fkIdBudget != null) {
            DtoBudget budget = new DtoBudget();

            budget.setIdBudget(fkIdBudget.getIdBudget());

            dto.setBudget(budget);
        }

        this.submittedDocumentList = new ArrayList<>();

        return dto;
    }

    public DtoSubmittedDocument setDtoDocument(SubmittedDocument document) {
        DtoSubmittedDocument dtoSubmittedDocument = new DtoSubmittedDocument();

        dtoSubmittedDocument.setVersion(document.getVersion());
        dtoSubmittedDocument.setDelivered(document.getDelivered());
        dtoSubmittedDocument.setDueDays(document.getDueDays());
        dtoSubmittedDocument.setDateEntry(document.getDateEntry());
        dtoSubmittedDocument.setDateReleased(document.getDateReleased());
        dtoSubmittedDocument.setDatePayment(document.getDatePayment());
        dtoSubmittedDocument.setDateExit(document.getDateExit());
        dtoSubmittedDocument.setDateDue(document.getDateDue());
        dtoSubmittedDocument.setIdSubmittedDocument(document.getIdSubmittedDocument());
        dtoSubmittedDocument.setAmountApagar(document.getAmountToPay());
        dtoSubmittedDocument.setReleased(document.getReleased());
        dtoSubmittedDocument.setName(document.getName());
        dtoSubmittedDocument.setCardNumber(document.getCardNumber());
        dtoSubmittedDocument.setNotes(document.getNotes());
        dtoSubmittedDocument.setFlagged(document.getFlagged());
        dtoSubmittedDocument.setPrepared(document.getPrepared());
        dtoSubmittedDocument.setExpires(document.getExpires());

        // Do not set the procedure id here; that would recurse.
        return dtoSubmittedDocument;
    }

    public DtoPerson getDtoPerson(Person person) {

        DtoPerson dtoPerson = new DtoPerson();

        // Version of the object
        dtoPerson.setVersion(person.getVersion());
        dtoPerson.setId(person.getPersonId());
        dtoPerson.setFirstName(person.getFirstName());
        dtoPerson.setLastName(person.getLastName());
        dtoPerson.setTaxId(person.getTaxId());
        dtoPerson.setEmail(person.getEmail());
        dtoPerson.setIsClient(person.getIsClient());
        dtoPerson.setMaritalStatus(person.getMaritalStatus());
        dtoPerson.setBirthDate(person.getBirthDate());
        dtoPerson.setNationality(person.getNationality());
        dtoPerson.setIdentificationNumber(person.getIdentificationNumber());
        dtoPerson.setMarriageCount(person.getMarriageCount());
        dtoPerson.setOccupation(person.getOccupation());
        dtoPerson.setAddress(person.getAddress());
        dtoPerson.setNotaryRegistrationNumber(person.getNotaryRegistrationNumber());
        dtoPerson.setSex(person.getSex());
        dtoPerson.setPhone(person.getPhone());

        DtoIdentificationType dtoIdentificationType = new DtoIdentificationType();
        dtoIdentificationType.setIdIdentificationType(person.getFkIdIdentificationType().getIdIdentificationType());

        dtoPerson.setDtoIdentificationType(dtoIdentificationType);

        dtoIdentificationType.setName(IdentificationTypeLookup.shared()
                .nameOf(person.getFkIdIdentificationType().getIdIdentificationType()));

        return dtoPerson;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getIdProcedure() != null ? getIdProcedure().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Procedure)) {
            return false;
        }
        Procedure other = (Procedure) object;
        if ((this.getIdProcedure() == null && other.getIdProcedure() != null)
                || (this.getIdProcedure() != null && !this.idProcedure.equals(other.idProcedure))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Procedure[ idProcedure=" + getIdProcedure() + " ]";
    }

    @XmlTransient
    @JsonIgnore
    public List<PersonProcedure> getPersonProcedureList() {
        return personProcedureList;
    }

    public void setPersonProcedureList(List<PersonProcedure> personProcedureList) {
        this.personProcedureList = personProcedureList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
