/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoProcedure;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.hibernate.LazyInitializationException;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Clase que representa a la entidad persona (general).
 * <p>
 * DISEÑO DEL SISTEMA
 * <p>
 * + Tanto los
 * usuarios del sistema, como los escribanos y los clientes, son "personas".
 * Esta clase representa,
 * mendiante el valor de derminados atributos, cada una de estas entidades.
 * <p>
 * REGLA DE NEGOCIO
 * <p>
 * + Si una instancia de personas, solo tiene asignado los siguientes valores:
 * nombre, apellido,
 * telefono, domicilio, tipo y numero de identificado e e-mail, la instancia
 * representa a una
 * persona que tiene presupuestos asociados (No es un cliente).
 * <p>
 * + Si una instancia de persona,
 * tiene ademas de los atributos de personas, los demas atributos (a excepcion
 * del numero de
 * registro) asignado, entonces se trata de un "cliente" y por lo tanto, debe
 * tener gestiones
 * asociadas.
 * <p>
 * + Si una instancia de personas, tiene todos los atributos asignados, pero
 * ademas
 * posee un numero de registro, entonces se trata de un "escribano".
 * <p>
 *
 * @author juanca
 */
@Entity
@Table(name = "people")
@XmlRootElement
@JsonIgnoreProperties({
        "hibernateLazyInitializer", "handler",
        "tramiteList", "tramitesPersonasList", "presupuestoList",
        "GestionDeEscrituraList", "folioList", "suplenciaList", "suplenciaList1",
        "usuariosList", "copiaList"
})
@NamedQueries({
        @NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p"),
        @NamedQuery(name = "Person.findByIdPersona", query = "SELECT p FROM Person p WHERE p.idPerson = :idPersona"),
        @NamedQuery(name = "Person.findByNumeroIdentificacion", query = "SELECT p FROM Person p WHERE p.identificationNumber = :numeroIdentificacion"),
        @NamedQuery(name = "Person.findBySexo", query = "SELECT p FROM Person p WHERE p.sex = :sexo"),
        @NamedQuery(name = "Person.findByFechaNacimiento", query = "SELECT p FROM Person p WHERE p.birthDate = :fechaNacimiento"),
        @NamedQuery(name = "Person.findByNumeroNupcias", query = "SELECT p FROM Person p WHERE p.marriageCount = :numeroNupcias"),
        @NamedQuery(name = "Person.findByRegistroEscribano", query = "SELECT p FROM Person p WHERE p.notaryRegistrationNumber = :registroEscribano"),
        @NamedQuery(name = "Person.findByEsCliente", query = "SELECT p FROM Person p WHERE p.isClient = :esCliente"),
        @NamedQuery(name = "Person.findByPersonaNombreApellido", query = "SELECT p FROM Person p WHERE p.name LIKE :nombre and p.lastName LIKE :apellido"),
})
public class Person implements Serializable, Persistable<Integer> {

    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date birthDate;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", fetch = FetchType.LAZY)
    private List<PersonProcedure> personProcedureList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer idPerson;
    @Basic(optional = false)
    @Column(name = "first_name")
    @NotBlank
    private String name;
    @Basic(optional = false)
    @Column(name = "last_name")
    @NotBlank
    private String lastName;
    @Column(name = "nationality")
    private String nationality;
    @Basic(optional = false)
    @Column(name = "identification_number")
    @NotBlank
    private String identificationNumber;
    @Column(name = "tax_id")
    private String taxId;
    @Column(name = "sex")
    private String sex;
    @Column(name = "marital_status")
    private String maritalStatus;
    @Column(name = "marriage_count")
    private Integer marriageCount;
    @Column(name = "occupation")
    private String occupation;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email")
    private String email;
    @Column(name = "notary_registration_number")
    private Integer notaryRegistrationNumber;
    @Basic(optional = false)
    @Column(name = "is_client")
    private boolean isClient;
    @XmlTransient
    @JsonIgnore
    @ManyToMany(mappedBy = "personList", fetch = FetchType.LAZY)
    private List<Procedure> procedureList;
    @JoinColumn(name = "fk_id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private IdentificationType fkIdIdentificationType;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPerson", fetch = FetchType.LAZY)
    private List<Budget> budgetList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdNotaryPerson", fetch = FetchType.LAZY)
    private List<DeedManagement> DeedManagementList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdNotaryPerson", fetch = FetchType.LAZY)
    private List<Folio> folioList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdSubstitute", fetch = FetchType.LAZY)
    private List<Substitution> substitutionList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdSubstitute", fetch = FetchType.LAZY)
    private List<Substitution> substitutionList1;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPerson", fetch = FetchType.LAZY)
    private List<User> userList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPerson", fetch = FetchType.LAZY)
    private List<Copy> copyList;

    public Person() {
    }

    public Person(Integer idPerson) {
        this.idPerson = idPerson;
    }

    public Person(Integer idPerson, String name, String lastName, String identificationNumber, boolean isClient) {
        this.idPerson = idPerson;
        this.name = name;
        this.lastName = lastName;
        this.identificationNumber = identificationNumber;
        this.isClient = isClient;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idPerson;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idPerson == null || idPerson.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getPersonId() {
        return idPerson;
    }

    public void setPersonId(Integer idPerson) {
        this.idPerson = idPerson;
    }

    public String getFirstName() {
        return name;
    }

    public void setFirstName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    @JsonAlias("dni")
    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    @JsonAlias("cuil")
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Integer getMarriageCount() {
        return marriageCount;
    }

    public void setMarriageCount(Integer marriageCount) {
        this.marriageCount = marriageCount;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getNotaryRegistrationNumber() {
        return notaryRegistrationNumber;
    }

    public void setNotaryRegistrationNumber(Integer notaryRegistrationNumber) {
        this.notaryRegistrationNumber = notaryRegistrationNumber;
    }

    public boolean getIsClient() {
        return isClient;
    }

    public void setIsClient(boolean isClient) {
        this.isClient = isClient;
    }

    @XmlTransient
    @JsonIgnore
    public List<Procedure> getProcedureList() {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList) {
        this.procedureList = procedureList;
    }

    public IdentificationType getFkIdIdentificationType() {
        return fkIdIdentificationType;
    }

    public void setFkIdIdentificationType(IdentificationType fkIdIdentificationType) {
        this.fkIdIdentificationType = fkIdIdentificationType;
    }

    @XmlTransient
    @JsonIgnore
    public List<Budget> getBudgetList() {
        return budgetList;
    }

    @XmlTransient
    @JsonIgnore
    public List<DeedManagement> getDeedManagementList() {
        return DeedManagementList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Folio> getFolioList() {
        return folioList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Substitution> getSubstitutionList() {
        return substitutionList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Substitution> getSubstitutionList1() {
        return substitutionList1;
    }

    @XmlTransient
    @JsonIgnore
    public List<User> getUserList() {
        return userList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Copy> getCopyList() {
        return copyList;
    }

    public void setBudgetList(List<Budget> budgetList) {
        this.budgetList = budgetList;
    }

    public void setDeedManagementList(List<DeedManagement> DeedManagementList) {
        this.DeedManagementList = DeedManagementList;
    }

    public void setFolioList(List<Folio> folioList) {
        this.folioList = folioList;
    }

    public void setSubstitutionList(List<Substitution> substitutionList) {
        this.substitutionList = substitutionList;
    }

    public void setSubstitutionList1(List<Substitution> substitutionList1) {
        this.substitutionList1 = substitutionList1;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void setCopyList(List<Copy> copyList) {
        this.copyList = copyList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPerson != null ? idPerson.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;
        if ((this.idPerson == null && other.idPerson != null)
                || (this.idPerson != null && !this.idPerson.equals(other.idPerson))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Person[ idPersona=" + idPerson + " ]"
                + "[ nombre=" + name + " ]"
                + "[ apellido=" + lastName + " ]";
    }

    @JsonIgnore
    public DtoPerson getDto() {

        DtoPerson dtoPerson = new DtoPerson();

        // Version del objeto
        dtoPerson.setVersion(this.version);
        dtoPerson.setId(this.idPerson);
        dtoPerson.setFirstName(this.name);
        dtoPerson.setLastName(this.lastName);
        dtoPerson.setTaxId(this.taxId);
        dtoPerson.setEmail(this.email);
        dtoPerson.setIsClient(this.isClient);
        dtoPerson.setMaritalStatus(this.maritalStatus);
        dtoPerson.setBirthDate(this.birthDate);
        dtoPerson.setNationality(this.nationality);
        dtoPerson.setIdentificationNumber(this.getIdentificationNumber());
        dtoPerson.setMarriageCount(this.marriageCount);
        dtoPerson.setOccupation(this.occupation);
        dtoPerson.setAddress(this.address);
        dtoPerson.setNotaryRegistrationNumber(this.notaryRegistrationNumber);
        dtoPerson.setSex(this.sex);
        dtoPerson.setPhone(this.phone);

        DtoIdentificationType dtoIdentificationType = new DtoIdentificationType();
        dtoIdentificationType.setIdIdentificationType(getFkIdIdentificationType().getIdIdentificationType());

        dtoPerson.setDtoIdentificationType(dtoIdentificationType);

        // Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoIdentificationType.setName(BusinessController.getInstancia().asociarNameIdentificationType(dtoPerson));

        // Asocio la lista de gestiones que tiene la persona si es Escribano
        if (this.getNotaryRegistrationNumber() != null) {
            ArrayList<DtoDeedManagement> miListaDtoManagementNotary = new ArrayList<>();

            if (!this.DeedManagementList.isEmpty()) {
                for (int i = 0; i < this.DeedManagementList.size(); i++) {
                    DtoDeedManagement dtoDeedManagement = this.DeedManagementList.get(i).getDto();
                    miListaDtoManagementNotary.add(dtoDeedManagement);
                }
                dtoPerson.setListDtoManagementDeEscriturasDeNotary(miListaDtoManagementNotary);
            }
        }

        // Asocio la lista de tramites que pertenece a la persona
        ArrayList<DtoProcedure> miListaDtoProcedures = new ArrayList<>();
        if (this.procedureList != null && !this.procedureList.isEmpty()) {
            for (int i = 0; i < this.procedureList.size(); i++) {
                DtoProcedure dtoProcedure = this.procedureList.get(i).getDto();
                miListaDtoProcedures.add(dtoProcedure);
            }
            dtoPerson.setListaProceduresPerson(miListaDtoProcedures);
        }

        // Asocio la lista de gestiones que tiene la persona
        ArrayList<DtoDeedManagement> miListaDtoManagementPerson = new ArrayList<>();
        ArrayList<Integer> listaIdGestiones = new ArrayList<>();

        try {
            if (procedureList != null && !procedureList.isEmpty()) {
                for (int i = 0; i < this.procedureList.size(); i++) {
                    // esto retorna tantas gestiones como tramites tenga la persona, si hay una
                    // tramite con una persona, esa persona tiene gestion
                    // si es otro tramite pero de la misma pgestion con la misma persona, repite la
                    // gestion
                    DtoDeedManagement dtoDeedManagement = this.procedureList.get(i).getFkIdManagement().getDto();

                    // Elimino Gestiones duplicadas, a causa de los tramites
                    // Si retorna entero positivo esta, sino no.
                    if (!listaIdGestiones.contains(dtoDeedManagement.getIdManagement())) {
                        listaIdGestiones.add(dtoDeedManagement.getIdManagement());
                        miListaDtoManagementPerson.add(dtoDeedManagement);
                    }
                }
                dtoPerson.setListaDtoManagementDeEscriturasPerson(miListaDtoManagementPerson);
            }
        } catch (LazyInitializationException ex) {
            dtoPerson.setListaDtoManagementDeEscriturasPerson(miListaDtoManagementPerson);
        }
        return dtoPerson;
    }

    public ArrayList<DtoDeedManagement> elimimarDuplicados(ArrayList<DtoDeedManagement> listaDtoDeed) {

        // Creamos un objeto HashSet
        HashSet hs = new HashSet();

        // Lo cargamos con los valores del array, esto hace quite los repetidos
        hs.addAll(listaDtoDeed);

        // Limpiamos el array
        listaDtoDeed.clear();
        listaDtoDeed.addAll(hs);

        return listaDtoDeed;

    }

    public void setAtributos(DtoPerson dtoPerson) {

        // Version del objeto
        this.setVersion(dtoPerson.getVersion());

        this.setFirstName(dtoPerson.getFirstName());
        this.setLastName(dtoPerson.getLastName());
        this.setPhone(dtoPerson.getPhone());
        this.setEmail(dtoPerson.getEmail());
        this.setPersonId(dtoPerson.getId());
        this.setIdentificationNumber(dtoPerson.getIdentificationNumber());

        IdentificationType identificationType = new IdentificationType();
        identificationType.setIdIdentificationType(dtoPerson.getDtoIdentificationType().getIdIdentificationType());
        identificationType.setName(dtoPerson.getDtoIdentificationType().getName());
        this.setFkIdIdentificationType(identificationType);

        // Set atributos Cliente
        if (dtoPerson.getIsClient()) {
            this.setNationality(dtoPerson.getNationality());
            this.setBirthDate(dtoPerson.getBirthDate());
            this.setTaxId(dtoPerson.getTaxId());
            this.setMaritalStatus(dtoPerson.getMaritalStatus());
            this.setMarriageCount(dtoPerson.getMarriageCount());
            this.setSex(dtoPerson.getSex());
            this.setOccupation(dtoPerson.getOccupation());
            this.setAddress(dtoPerson.getAddress());
            this.setIsClient(dtoPerson.getIsClient());
        }

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
