/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoBudget;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un presupuesto.
 *
 * @author juanca
 */
@Entity
@Table(name = "presupuestos")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Presupuesto.findAll", query = "SELECT p FROM Budget p"),
        @NamedQuery(name = "Presupuesto.findByIdPresupuesto", query = "SELECT p FROM Budget p WHERE p.idBudget = :idPresupuesto"),
        @NamedQuery(name = "Presupuesto.findByFecha", query = "SELECT p FROM Budget p WHERE p.date = :fecha"),
        @NamedQuery(name = "Presupuesto.findByNumero", query = "SELECT p FROM Budget p WHERE p.number = :numero"),
        @NamedQuery(name = "Presupuesto.findByEstado", query = "SELECT p FROM Budget p WHERE p.status = :estado"),
        @NamedQuery(name = "Presupuesto.findByPersona", query = "SELECT p FROM Budget p WHERE p.fkIdPerson.idPerson = :idPersona"),
})
public class Budget implements Serializable, Persistable<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_presupuesto")
    private Integer idBudget;

    @Basic(optional = false)
    @Column(name = "numero")
    private int number;

    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Basic(optional = false)
    @Column(name = "encabezado")
    private String encabezado;

    @Column(name = "observaciones")
    private String notes;

    @Basic(optional = false)
    @Column(name = "estado")
    private String status;

    @Column(name = "monto_inmueble")
    private Float propertyAmount;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;

    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Person fkIdPerson;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdBudget", fetch = FetchType.EAGER)
    private java.util.Set<Payment> paymentList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdBudget", fetch = FetchType.LAZY)
    private List<Procedure> procedureList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdBudget", fetch = FetchType.LAZY)
    private List<Item> itemList = new ArrayList<>();

    /**
     * Constructor por default de presupuesto. Inicializa el ID presupuesto segun el
     * campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO, y todas las listas de objetos.
     */
    public Budget() {
        this.idBudget = BusinessConstants.ID_OBJETO_NO_VALIDO;
        this.itemList = new ArrayList<>();
        this.paymentList = new java.util.HashSet<>();
        this.procedureList = new ArrayList<>();
    }

    public Budget(Integer idBudget) {
        this.idBudget = idBudget;
    }

    public Budget(Integer idBudget, int number, Date date, String encabezado, String status) {
        this.idBudget = idBudget;
        this.number = number;
        this.date = date;
        this.encabezado = encabezado;
        this.status = status;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idBudget;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idBudget == null || idBudget.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdBudget() {
        return idBudget;
    }

    public void setIdBudget(Integer idBudget) {
        this.idBudget = idBudget;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @XmlTransient
    @JsonIgnore
    public java.util.Set<Payment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(java.util.Set<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    @JsonProperty("person")
    public Person getFkIdPerson() {
        return fkIdPerson;
    }

    @JsonProperty("person")
    public void setFkIdPerson(Person fkIdPerson) {
        this.fkIdPerson = fkIdPerson;
    }

    @XmlTransient
    @JsonIgnore
    public List<Procedure> getProcedureList() {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList) {
        this.procedureList = procedureList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoBudget getDto() {
        DtoBudget miDto = new DtoBudget();

        miDto.setIdBudget(idBudget);
        miDto.setDate(date);
        miDto.setNumber(number);
        miDto.setEncabezado(encabezado);
        miDto.setStatus(status);
        miDto.setPropertyAmount(propertyAmount);
        miDto.setNotes(notes);

        if (fkIdPerson != null) {
            try {
                miDto.setPerson(fkIdPerson.getDto());
            } catch (Exception ex) {
                DtoPerson persons = new DtoPerson();
                persons.setId(fkIdPerson.getPersonId());
                persons.setFirstName(fkIdPerson.getFirstName());
                persons.setLastName(fkIdPerson.getLastName());
                persons.setDtoIdentificationType(fkIdPerson.getFkIdIdentificationType().getDto());
                persons.setIdentificationNumber(fkIdPerson.getIdentificationNumber());
            }
        } else {
            miDto.setPerson(null);
        }

        miDto.setVersion(version);

        return miDto;
    }

    public void setAtributos(DtoBudget dtoBudget) {
        this.setIdBudget(dtoBudget.getIdBudget());
        this.setDate(dtoBudget.getDate());
        this.setNumber(dtoBudget.getNumber());
        this.setEncabezado(dtoBudget.getEncabezado());
        this.setStatus(dtoBudget.getStatus());
        this.setPropertyAmount(dtoBudget.getPropertyAmount());
        this.setNotes(dtoBudget.getNotes());

        if (dtoBudget.getPerson() != null) {
            Person client = new Person();
            client.setAtributos(dtoBudget.getPerson());
            this.setFkIdPerson(client);
        }

        if (dtoBudget.getItems() != null && !dtoBudget.getItems().isEmpty()) {
            for (Iterator<DtoItem> it = dtoBudget.getItems().iterator(); it.hasNext();) {
                DtoItem dtoItem = it.next();
                Item item = new Item();
                item.setAtributos(dtoItem);
            }
        }

        version = dtoBudget.getVersion();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idBudget != null ? idBudget.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Budget)) {
            return false;
        }
        Budget other = (Budget) object;
        if ((this.idBudget == null && other.idBudget != null)
                || (this.idBudget != null && !this.idBudget.equals(other.idBudget))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Presupuesto[ idPresupuesto=" + idBudget + " ]";
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Float getPropertyAmount() {
        return propertyAmount;
    }

    public void setPropertyAmount(Float propertyAmount) {
        this.propertyAmount = propertyAmount;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Deprecated
    public Float getSaldo() {
        return null;
    }

    @Deprecated
    public void setSaldo(Float saldo) {
    }

    @Deprecated
    public Float getTotal() {
        return propertyAmount;
    }

    @Deprecated
    public void setTotal(Float total) {
    }
}
