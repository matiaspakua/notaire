/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoProperty;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoProcedure;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Clase que representa un gestion de escritura.
 * <p>
 * REGLA DE NEGOCIO:
 * <p>
 *
 * <lo>
 * <li>El numero de carpeta es auto-incremental sugerido por el sistema, pero
 * puede ser
 * modificado por el usuario, donde se verifica que el numero indicado no exista
 * ya registrado.
 * </li> </lo>
 *
 *
 * @author User
 */
@Entity
@Table(name = "gestiones_de_escrituras")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "GestionDeEscritura.findAll", query = "SELECT g FROM DeedManagement g"),
        @NamedQuery(name = "GestionDeEscritura.findByIdGestion", query = "SELECT g FROM DeedManagement g WHERE g.idManagement = :idGestion"),
        @NamedQuery(name = "GestionDeEscritura.findByNumero", query = "SELECT g FROM DeedManagement g WHERE g.number = :numero"),
        @NamedQuery(name = "GestionDeEscritura.findByFechaInicio", query = "SELECT g FROM DeedManagement g WHERE g.dateStart = :fechaInicio")
})
public class DeedManagement implements Serializable, Persistable<Integer> {

    @Basic(optional = false)
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date dateStart;
    @JoinColumn(name = "fk_id_estado_de_gestion", referencedColumnName = "id_estado_gestion")
    @ManyToOne
    private ManagementStatus fkIdManagementStatus;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_gestion")
    private Integer idManagement;
    @Basic(optional = false)
    @Column(name = "numero")
    private int number;
    @Basic(optional = false)
    @Column(name = "encabezado")
    private String encabezado;
    @Column(name = "observaciones")
    private String notes;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdManagement")
    private List<History> historyList;
    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Person fkIdNotaryPerson;
    @OneToMany(mappedBy = "fkIdManagement")
    private List<Procedure> procedureList;
    @Column(name = "deuda_pendiente_al_archivar")
    private Boolean pendingDebtAtArchiving;

    /**
     * Constructor por default para gestion de escritura. Asigna al ID y al numero
     * de gestion el
     * valor de {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public DeedManagement() {
        this.idManagement = BusinessConstants.ID_OBJETO_NO_VALIDO;
        this.number = BusinessConstants.ID_OBJETO_NO_VALIDO;
        this.procedureList = new ArrayList<>();
        this.historyList = new ArrayList<>();
    }

    public DeedManagement(Integer idManagement) {
        this.idManagement = idManagement;
    }

    public DeedManagement(Integer idManagement, int number, Date dateStart, String encabezado) {
        this.idManagement = idManagement;
        this.number = number;
        this.dateStart = dateStart;
        this.encabezado = encabezado;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idManagement;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idManagement == null || idManagement.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdManagement() {
        return idManagement;
    }

    public void setIdManagement(Integer idManagement) {
        this.idManagement = idManagement;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @XmlTransient
    @JsonIgnore
    public List<History> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<History> historyList) {
        this.historyList = historyList;
    }

    public Person getFkIdNotaryPerson() {
        return fkIdNotaryPerson;
    }

    public void setFkIdNotaryPerson(Person fkIdNotaryPerson) {
        this.fkIdNotaryPerson = fkIdNotaryPerson;
    }

    @XmlTransient
    @JsonIgnore
    public List<Procedure> getProcedureList() {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList) {
        this.procedureList = procedureList;
    }

    public Boolean getPendingDebtAtArchiving() {
        return pendingDebtAtArchiving;
    }

    public void setPendingDebtAtArchiving(Boolean pendingDebtAtArchiving) {
        this.pendingDebtAtArchiving = pendingDebtAtArchiving;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idManagement != null ? idManagement.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DeedManagement)) {
            return false;
        }
        DeedManagement other = (DeedManagement) object;
        if ((this.idManagement == null && other.idManagement != null)
                || (this.idManagement != null && !this.idManagement.equals(other.idManagement))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GestionDeEscritura[ idGestion=" + idManagement + " ]"
                + "[ numero=" + number + " ]";
    }

    public void setAtributos(DtoDeedManagement dtoManagement) throws DtoInvalidoException {

        this.setVersion(dtoManagement.getVersion());
        this.setIdManagement(dtoManagement.getIdManagement());
        this.setNumber(dtoManagement.getNumber());
        this.setEncabezado(dtoManagement.getEncabezado());
        this.setDateStart(dtoManagement.getDateStart());
        this.setNotes(dtoManagement.getNotes());

        if (dtoManagement.getPersonNotary() != null) {
            Person notary = new Person();
            notary.setAtributos(dtoManagement.getPersonNotary());

            this.setFkIdNotaryPerson(notary);
        }

        for (Iterator<DtoProcedure> it = dtoManagement.getListaProceduresAsociados().iterator(); it.hasNext();) {
            DtoProcedure dtoProcedure = it.next();

            dtoProcedure.setDeedManagement(dtoManagement);
            dtoProcedure.setProperty(new DtoProperty());
        }

        // Estado de la gestion
        ManagementStatus fkStatusManagement = new ManagementStatus();
        fkStatusManagement.setAtributo(dtoManagement.getStatus());
        this.setFkIdManagementStatus(fkStatusManagement);

    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoDeedManagement getDto() {
        DtoDeedManagement dtoManagement = new DtoDeedManagement();

        dtoManagement.setVersion(this.getVersion());
        dtoManagement.setIdManagement(this.getIdManagement());
        dtoManagement.setNumber(this.getNumber());
        dtoManagement.setEncabezado(this.getEncabezado());
        dtoManagement.setDateStart(this.getDateStart());
        dtoManagement.setNotes(this.getNotes());
        dtoManagement.setStatus(this.fkIdManagementStatus.getDto());

        // Evito que se produzca un bucle, por esta razon esta el
        // metodo getDtoEscribano y no getDto.
        dtoManagement.setPersonNotary(this.getDtoNotary());

        // Tramites asociados a la gestion
        ArrayList<DtoProcedure> listaDtoProcedures = new ArrayList<>();
        if (!procedureList.isEmpty()) {
            for (int i = 0; i < procedureList.size(); i++) {
                listaDtoProcedures.add(this.getDtoProcedure(procedureList.get(i)));
            }
            dtoManagement.setListaProceduresAsociados(listaDtoProcedures);
        }

        /*
         * Personas asociadas a la gestion
         * Cargo los clientes asociados a la gestion, no descrimino por tRamite,
         * esto signifca que se eliminan las personas duplicadas,
         * debido a que una gestion tien mas de un tarmite, y un tarmite mas de una
         * persona
         * involucrada,
         * esto produce que se repitan las personas involucradas en la gesion
         * Atencion: para mejorar se puede filtrar que persona pertenece a que tramite,
         * lo soporta
         */
        ArrayList<DtoPerson> listaDtoPersons = new ArrayList<>();
        ArrayList<Integer> listaIdPerson = new ArrayList<>();

        if (!(this.procedureList.isEmpty())) {
            for (int j = 0; j < procedureList.size(); j++) {
                for (int i = 0; i < procedureList.get(j).getPersonList().size(); i++) {

                    DtoPerson miDtoPerson = this.getDtoPersonInvolucrada(procedureList.get(j).getPersonList().get(i));

                    if (!listaIdPerson.contains(miDtoPerson.getId())) {
                        listaIdPerson.add(miDtoPerson.getId());
                        listaDtoPersons.add(miDtoPerson);
                    }
                }
            }

            dtoManagement.setListaClientesInvolucrados(listaDtoPersons);
        }

        // Estado de la gestion
        DtoManagementStatus statusDto = new DtoManagementStatus();
        statusDto.setIdManagementStatus(this.getFkIdManagementStatus().getIdManagementStatus());
        statusDto.setName(fkIdManagementStatus.getName());
        statusDto.setNotes(fkIdManagementStatus.getNotes());
        statusDto.setVersion(fkIdManagementStatus.getVersion());

        dtoManagement.setStatus(statusDto);

        return dtoManagement;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoPerson getDtoNotary() {

        DtoPerson dtoPerson = new DtoPerson();

        // Version del objeto
        dtoPerson.setVersion(fkIdNotaryPerson.getVersion());
        dtoPerson.setId(fkIdNotaryPerson.getPersonId());
        dtoPerson.setFirstName(fkIdNotaryPerson.getFirstName());
        dtoPerson.setLastName(fkIdNotaryPerson.getLastName());
        dtoPerson.setTaxId(fkIdNotaryPerson.getTaxId());
        dtoPerson.setEmail(fkIdNotaryPerson.getEmail());
        dtoPerson.setIsClient(fkIdNotaryPerson.getIsClient());
        dtoPerson.setMaritalStatus(fkIdNotaryPerson.getMaritalStatus());
        dtoPerson.setBirthDate(fkIdNotaryPerson.getBirthDate());
        dtoPerson.setNationality(fkIdNotaryPerson.getNationality());
        dtoPerson.setIdentificationNumber(fkIdNotaryPerson.getIdentificationNumber());
        dtoPerson.setMarriageCount(fkIdNotaryPerson.getMarriageCount());
        dtoPerson.setOccupation(fkIdNotaryPerson.getOccupation());
        dtoPerson.setAddress(fkIdNotaryPerson.getAddress());
        dtoPerson.setNotaryRegistrationNumber(fkIdNotaryPerson.getNotaryRegistrationNumber());
        dtoPerson.setSex(fkIdNotaryPerson.getSex());
        dtoPerson.setPhone(fkIdNotaryPerson.getPhone());

        DtoIdentificationType dtoIdentificationType = new DtoIdentificationType();
        dtoIdentificationType
                .setIdIdentificationType(fkIdNotaryPerson.getFkIdIdentificationType().getIdIdentificationType());

        dtoPerson.setDtoIdentificationType(dtoIdentificationType);

        // Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoIdentificationType.setName(BusinessController.getInstancia().asociarNameIdentificationType(dtoPerson));

        return dtoPerson;
    }

    public DtoProcedure getDtoProcedure(Procedure miProcedure) {

        DtoProcedure miDto = new DtoProcedure();

        miDto.setIdProcedure(miProcedure.getIdProcedure());
        miDto.setNotes(miProcedure.getNotes());
        miDto.setTiposDeProcedure(miProcedure.getFkIdProcedureType().getDto());

        if (miProcedure.getFkIdProperty() != null) {
            miDto.setProperty(miProcedure.getFkIdProperty().getDto());
        } else {
            miDto.setProperty(null);
        }

        return miDto;
    }

    public DtoPerson getDtoPersonInvolucrada(Person miPerson) {
        DtoPerson dtoPerson = new DtoPerson();

        // Version del objeto
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

        // Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoIdentificationType.setName(BusinessController.getInstancia().asociarNameIdentificationType(dtoPerson));

        return dtoPerson;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ManagementStatus getFkIdManagementStatus() {
        return fkIdManagementStatus;
    }

    public void setFkIdManagementStatus(ManagementStatus fkIdManagementStatus) {
        this.fkIdManagementStatus = fkIdManagementStatus;
    }
}
