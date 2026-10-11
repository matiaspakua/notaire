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
 * Clase that representa a procedure in curso (is not the definicion of a procedure,
 * sino a procedure
 * concreto that esta llevando a cabo). Posee the referencias hacia the tipo of
 * procedure, the
 * client, the budget of the cual origino and the number of management.
 * <p>
 * REGLA DE NEGOCIO:
 * <p>
 * +
 * When crea a budget, in the tabla procedures crea a registo, the
 * cual asocia al
 * budget a determinado procedure together with the procedure type indicado.
 * <p>
 * + Cuando se inicia
 * a management, must asociar the misma with the registros of procedures asociados
 * a the budgets
 * seleccionado. La combinacion de "DeedManagement" y "Procedure" representan
 * the concept
 * abstracto of a "management".
 * <p>
 * + When start a management, can haber varios clients
 * involucrados en la misma. La tabla tramites_personas, expresa esta relacion.
 * El atributo
 * personasList is utilizado by the framework of persistencia for escribir
 * sobre la tabla
 * relacional indicada.
 * <p>
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
     * Constructor by default for Procedure. Inicializa the ID budget according to the
     * campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO, and all the listas.
     */
    public Procedure() {
        this.idProcedure = BusinessConstants.ID_OBJETO_NO_VALIDO;
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
        return idProcedure == null || idProcedure.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
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
        DtoProcedure miDto = new DtoProcedure();

        miDto.setIdProcedure(getIdProcedure());
        miDto.setNotes(notes);
        miDto.setProcedureType(fkIdProcedureType.getDto());

        if (fkIdDeed != null) {
            DtoDeed miDtoDeed = new DtoDeed();

            miDtoDeed.setIdDeed(fkIdDeed.getIdDeed());
            miDtoDeed.setNumber(fkIdDeed.getNumber());

            miDto.setDeed(miDtoDeed);
        }

        if (fkIdManagement != null) {
            DtoDeedManagement miDtoDeedManagement = new DtoDeedManagement();

            miDtoDeedManagement.setIdManagement(fkIdManagement.getIdManagement());
            miDtoDeedManagement.setNumber(fkIdManagement.getNumber());

            DtoPerson miNotary = new DtoPerson();
            miNotary.setId(fkIdManagement.getFkIdNotaryPerson().getPersonId());
            miNotary.setNotaryRegistrationNumber(fkIdManagement.getFkIdNotaryPerson().getNotaryRegistrationNumber());

            miDtoDeedManagement.setPersonNotary(miNotary);

            miDto.setDeedManagement(miDtoDeedManagement);
        }

        if (fkIdProperty != null) {
            miDto.setProperty(fkIdProperty.getDto());
        } else {
            miDto.setProperty(null);
        }

        if (this.fkIdBudget != null) {
            DtoBudget budget = new DtoBudget();

            budget.setIdBudget(fkIdBudget.getIdBudget());

            miDto.setBudget(budget);
        }

        this.submittedDocumentList = new ArrayList<>();

        return miDto;
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

        // No se hace set de fkidtramite , porque se produce bucle
        return dtoSubmittedDocument;
    }

    public DtoPerson getDtoPerson(Person miPerson) {

        DtoPerson dtoPerson = new DtoPerson();

        // Version of the objeto
        dtoPerson.setVersion(miPerson.getVersion());
        dtoPerson.setId(miPerson.getPersonId());
        dtoPerson.setFirstName(miPerson.getFirstName());
        dtoPerson.setLastName(miPerson.getLastName());
        dtoPerson.setTaxId(miPerson.getTaxId());
        dtoPerson.setEmail(miPerson.getEmail());
        dtoPerson.setIsClient(miPerson.getIsClient());
        dtoPerson.setMaritalStatus(miPerson.getMaritalStatus());
        dtoPerson.setBirthDate(miPerson.getBirthDate());
        dtoPerson.setNationality(miPerson.getNationality());
        dtoPerson.setIdentificationNumber(miPerson.getIdentificationNumber());
        dtoPerson.setMarriageCount(miPerson.getMarriageCount());
        dtoPerson.setOccupation(miPerson.getOccupation());
        dtoPerson.setAddress(miPerson.getAddress());
        dtoPerson.setNotaryRegistrationNumber(miPerson.getNotaryRegistrationNumber());
        dtoPerson.setSex(miPerson.getSex());
        dtoPerson.setPhone(miPerson.getPhone());

        DtoIdentificationType dtoIdentificationType = new DtoIdentificationType();
        dtoIdentificationType.setIdIdentificationType(miPerson.getFkIdIdentificationType().getIdIdentificationType());

        dtoPerson.setDtoIdentificationType(dtoIdentificationType);

        dtoIdentificationType.setName(IdentificationTypeLookup.shared()
                .nameOf(miPerson.getFkIdIdentificationType().getIdIdentificationType()));

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
        return "Procedure[ idTramite=" + getIdProcedure() + " ]";
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
