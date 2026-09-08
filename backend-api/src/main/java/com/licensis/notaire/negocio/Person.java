/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
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
        @NamedQuery(name = "Person.findByIdPersona", query = "SELECT p FROM Person p WHERE p.idPersona = :idPersona"),
        @NamedQuery(name = "Person.findByNumeroIdentificacion", query = "SELECT p FROM Person p WHERE p.numeroIdentificacion = :numeroIdentificacion"),
        @NamedQuery(name = "Person.findBySexo", query = "SELECT p FROM Person p WHERE p.sexo = :sexo"),
        @NamedQuery(name = "Person.findByFechaNacimiento", query = "SELECT p FROM Person p WHERE p.fechaNacimiento = :fechaNacimiento"),
        @NamedQuery(name = "Person.findByNumeroNupcias", query = "SELECT p FROM Person p WHERE p.numeroNupcias = :numeroNupcias"),
        @NamedQuery(name = "Person.findByRegistroEscribano", query = "SELECT p FROM Person p WHERE p.registroEscribano = :registroEscribano"),
        @NamedQuery(name = "Person.findByEsCliente", query = "SELECT p FROM Person p WHERE p.esCliente = :esCliente"),
        @NamedQuery(name = "Person.findByPersonaNombreApellido", query = "SELECT p FROM Person p WHERE p.nombre LIKE :nombre and p.apellido LIKE :apellido"),
})
public class Person implements Serializable, Persistable<Integer> {

    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "persona", fetch = FetchType.LAZY)
    private List<TramitesPersonas> tramitesPersonasList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer idPersona;
    @Basic(optional = false)
    @Column(name = "first_name")
    @NotBlank
    private String nombre;
    @Basic(optional = false)
    @Column(name = "last_name")
    @NotBlank
    private String apellido;
    @Column(name = "nationality")
    private String nacionalidad;
    @Basic(optional = false)
    @Column(name = "identification_number")
    @NotBlank
    private String numeroIdentificacion;
    @Column(name = "tax_id")
    private String cuit;
    @Column(name = "sex")
    private String sexo;
    @Column(name = "marital_status")
    private String estadoCivil;
    @Column(name = "marriage_count")
    private Integer numeroNupcias;
    @Column(name = "occupation")
    private String ocupacion;
    @Column(name = "address")
    private String domicilio;
    @Column(name = "phone")
    private String telefono;
    @Column(name = "email")
    private String eMail;
    @Column(name = "notary_registration_number")
    private Integer registroEscribano;
    @Basic(optional = false)
    @Column(name = "is_client")
    private boolean esCliente;
    @XmlTransient
    @JsonIgnore
    @ManyToMany(mappedBy = "personaList", fetch = FetchType.LAZY)
    private List<Tramite> tramiteList;
    @JoinColumn(name = "fk_id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private TipoIdentificacion fkIdTipoIdentificacion;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersona", fetch = FetchType.LAZY)
    private List<Presupuesto> presupuestoList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersonaEscribano", fetch = FetchType.LAZY)
    private List<GestionDeEscritura> GestionDeEscrituraList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersonaEscribano", fetch = FetchType.LAZY)
    private List<Folio> folioList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdSuplente", fetch = FetchType.LAZY)
    private List<Suplencia> suplenciaList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdSuplantado", fetch = FetchType.LAZY)
    private List<Suplencia> suplenciaList1;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersona", fetch = FetchType.LAZY)
    private List<Usuario> usuariosList;
    @XmlTransient
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersona", fetch = FetchType.LAZY)
    private List<Copia> copiaList;

    public Person() {
    }

    public Person(Integer idPersona) {
        this.idPersona = idPersona;
    }

    public Person(Integer idPersona, String nombre, String apellido, String numeroIdentificacion, boolean esCliente) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroIdentificacion = numeroIdentificacion;
        this.esCliente = esCliente;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idPersona;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idPersona == null || idPersona.equals(ConstantesNegocio.ID_OBJETO_NO_VALIDO);
    }


    public Integer getPersonId() {
        return idPersona;
    }

    public void setPersonId(Integer idPersona) {
        this.idPersona = idPersona;
    }

    public String getFirstName() {
        return nombre;
    }

    public void setFirstName(String nombre) {
        this.nombre = nombre;
    }

    public String getLastName() {
        return apellido;
    }

    public void setLastName(String apellido) {
        this.apellido = apellido;
    }

    public String getNationality() {
        return nacionalidad;
    }

    public void setNationality(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getIdentificationNumber() {
        return numeroIdentificacion;
    }

    @JsonAlias("dni")
    public void setIdentificationNumber(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getTaxId() {
        return cuit;
    }

    @JsonAlias("cuil")
    public void setTaxId(String cuit) {
        this.cuit = cuit;
    }

    public String getSex() {
        return sexo;
    }

    public void setSex(String sexo) {
        this.sexo = sexo;
    }

    public Date getBirthDate() {
        return fechaNacimiento;
    }

    public void setBirthDate(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getMaritalStatus() {
        return estadoCivil;
    }

    public void setMaritalStatus(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public Integer getMarriageCount() {
        return numeroNupcias;
    }

    public void setMarriageCount(Integer numeroNupcias) {
        this.numeroNupcias = numeroNupcias;
    }

    public String getOccupation() {
        return ocupacion;
    }

    public void setOccupation(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public String getAddress() {
        return domicilio;
    }

    public void setAddress(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getPhone() {
        return telefono;
    }

    public void setPhone(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return eMail;
    }

    public void setEmail(String eMail) {
        this.eMail = eMail;
    }

    public Integer getNotaryRegistrationNumber() {
        return registroEscribano;
    }

    public void setNotaryRegistrationNumber(Integer registroEscribano) {
        this.registroEscribano = registroEscribano;
    }

    public boolean getIsClient() {
        return esCliente;
    }

    public void setIsClient(boolean esCliente) {
        this.esCliente = esCliente;
    }

    @XmlTransient
    @JsonIgnore
    public List<Tramite> getTramiteList() {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList) {
        this.tramiteList = tramiteList;
    }

    public TipoIdentificacion getFkIdIdentificationType() {
        return fkIdTipoIdentificacion;
    }

    public void setFkIdIdentificationType(TipoIdentificacion fkIdTipoIdentificacion) {
        this.fkIdTipoIdentificacion = fkIdTipoIdentificacion;
    }

    @XmlTransient
    @JsonIgnore
    public List<Presupuesto> getPresupuestoList() {
        return presupuestoList;
    }

    @XmlTransient
    @JsonIgnore
    public List<GestionDeEscritura> getGestionDeEscrituraList() {
        return GestionDeEscrituraList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Folio> getFolioList() {
        return folioList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Suplencia> getSuplenciaList() {
        return suplenciaList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Suplencia> getSuplenciaList1() {
        return suplenciaList1;
    }

    @XmlTransient
    @JsonIgnore
    public List<Usuario> getUsuariosList() {
        return usuariosList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Copia> getCopiaList() {
        return copiaList;
    }

    public void setPresupuestoList(List<Presupuesto> presupuestoList) {
        this.presupuestoList = presupuestoList;
    }

    public void setGestionDeEscrituraList(List<GestionDeEscritura> GestionDeEscrituraList) {
        this.GestionDeEscrituraList = GestionDeEscrituraList;
    }

    public void setFolioList(List<Folio> folioList) {
        this.folioList = folioList;
    }

    public void setSuplenciaList(List<Suplencia> suplenciaList) {
        this.suplenciaList = suplenciaList;
    }

    public void setSuplenciaList1(List<Suplencia> suplenciaList1) {
        this.suplenciaList1 = suplenciaList1;
    }

    public void setUsuariosList(List<Usuario> usuariosList) {
        this.usuariosList = usuariosList;
    }

    public void setCopiaList(List<Copia> copiaList) {
        this.copiaList = copiaList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPersona != null ? idPersona.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;
        if ((this.idPersona == null && other.idPersona != null)
                || (this.idPersona != null && !this.idPersona.equals(other.idPersona))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Person[ idPersona=" + idPersona + " ]"
                + "[ nombre=" + nombre + " ]"
                + "[ apellido=" + apellido + " ]";
    }

    @JsonIgnore
    public DtoPerson getDto() {

        DtoPerson dtoPersona = new DtoPerson();

        // Version del objeto
        dtoPersona.setVersion(this.version);
        dtoPersona.setId(this.idPersona);
        dtoPersona.setFirstName(this.nombre);
        dtoPersona.setLastName(this.apellido);
        dtoPersona.setTaxId(this.cuit);
        dtoPersona.setEmail(this.eMail);
        dtoPersona.setIsClient(this.esCliente);
        dtoPersona.setMaritalStatus(this.estadoCivil);
        dtoPersona.setBirthDate(this.fechaNacimiento);
        dtoPersona.setNationality(this.nacionalidad);
        dtoPersona.setIdentificationNumber(this.getIdentificationNumber());
        dtoPersona.setMarriageCount(this.numeroNupcias);
        dtoPersona.setOccupation(this.ocupacion);
        dtoPersona.setAddress(this.domicilio);
        dtoPersona.setNotaryRegistrationNumber(this.registroEscribano);
        dtoPersona.setSex(this.sexo);
        dtoPersona.setPhone(this.telefono);

        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();
        dtoTipoIdentificacion.setIdTipoIdentificacion(getFkIdIdentificationType().getIdTipoIdentificacion());

        dtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);

        // Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoTipoIdentificacion.setNombre(ControllerNegocio.getInstancia().asociarNombreTipoIdentificacion(dtoPersona));

        // Asocio la lista de gestiones que tiene la persona si es Escribano
        if (this.getNotaryRegistrationNumber() != null) {
            ArrayList<DtoGestionDeEscritura> miListaDtoGestionEscribano = new ArrayList<>();

            if (!this.GestionDeEscrituraList.isEmpty()) {
                for (int i = 0; i < this.GestionDeEscrituraList.size(); i++) {
                    DtoGestionDeEscritura dtoGestionDeEscritura = this.GestionDeEscrituraList.get(i).getDto();
                    miListaDtoGestionEscribano.add(dtoGestionDeEscritura);
                }
                dtoPersona.setListDtoGestionDeEscriturasDeEscribano(miListaDtoGestionEscribano);
            }
        }

        // Asocio la lista de tramites que pertenece a la persona
        ArrayList<DtoTramite> miListaDtoTramites = new ArrayList<>();
        if (this.tramiteList != null && !this.tramiteList.isEmpty()) {
            for (int i = 0; i < this.tramiteList.size(); i++) {
                DtoTramite dtoTramite = this.tramiteList.get(i).getDto();
                miListaDtoTramites.add(dtoTramite);
            }
            dtoPersona.setListaTramitesPersona(miListaDtoTramites);
        }

        // Asocio la lista de gestiones que tiene la persona
        ArrayList<DtoGestionDeEscritura> miListaDtoGestionPersona = new ArrayList<>();
        ArrayList<Integer> listaIdGestiones = new ArrayList<>();

        try {
            if (tramiteList != null && !tramiteList.isEmpty()) {
                for (int i = 0; i < this.tramiteList.size(); i++) {
                    // esto retorna tantas gestiones como tramites tenga la persona, si hay una
                    // tramite con una persona, esa persona tiene gestion
                    // si es otro tramite pero de la misma pgestion con la misma persona, repite la
                    // gestion
                    DtoGestionDeEscritura dtoGestionDeEscritura = this.tramiteList.get(i).getFkIdGestion().getDto();

                    // Elimino Gestiones duplicadas, a causa de los tramites
                    // Si retorna entero positivo esta, sino no.
                    if (!listaIdGestiones.contains(dtoGestionDeEscritura.getIdGestion())) {
                        listaIdGestiones.add(dtoGestionDeEscritura.getIdGestion());
                        miListaDtoGestionPersona.add(dtoGestionDeEscritura);
                    }
                }
                dtoPersona.setListaDtoGestionDeEscriturasPersona(miListaDtoGestionPersona);
            }
        } catch (LazyInitializationException ex) {
            dtoPersona.setListaDtoGestionDeEscriturasPersona(miListaDtoGestionPersona);
        }
        return dtoPersona;
    }

    public ArrayList<DtoGestionDeEscritura> elimimarDuplicados(ArrayList<DtoGestionDeEscritura> listaDtoEscritura) {

        // Creamos un objeto HashSet
        HashSet hs = new HashSet();

        // Lo cargamos con los valores del array, esto hace quite los repetidos
        hs.addAll(listaDtoEscritura);

        // Limpiamos el array
        listaDtoEscritura.clear();
        listaDtoEscritura.addAll(hs);

        return listaDtoEscritura;

    }

    public void setAtributos(DtoPerson dtoPersona) {

        // Version del objeto
        this.setVersion(dtoPersona.getVersion());

        this.setFirstName(dtoPersona.getFirstName());
        this.setLastName(dtoPersona.getLastName());
        this.setPhone(dtoPersona.getPhone());
        this.setEmail(dtoPersona.getEmail());
        this.setPersonId(dtoPersona.getId());
        this.setIdentificationNumber(dtoPersona.getIdentificationNumber());

        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setIdTipoIdentificacion(dtoPersona.getDtoTipoIdentificacion().getIdTipoIdentificacion());
        tipoIdentificacion.setNombre(dtoPersona.getDtoTipoIdentificacion().getNombre());
        this.setFkIdIdentificationType(tipoIdentificacion);

        // Set atributos Cliente
        if (dtoPersona.getIsClient()) {
            this.setNationality(dtoPersona.getNationality());
            this.setBirthDate(dtoPersona.getBirthDate());
            this.setTaxId(dtoPersona.getTaxId());
            this.setMaritalStatus(dtoPersona.getMaritalStatus());
            this.setMarriageCount(dtoPersona.getMarriageCount());
            this.setSex(dtoPersona.getSex());
            this.setOccupation(dtoPersona.getOccupation());
            this.setAddress(dtoPersona.getAddress());
            this.setIsClient(dtoPersona.getIsClient());
        }

    }

    @XmlTransient
    @JsonIgnore
    public List<TramitesPersonas> getTramitesPersonasList() {
        return tramitesPersonasList;
    }

    public void setTramitesPersonasList(List<TramitesPersonas> tramitesPersonasList) {
        this.tramitesPersonasList = tramitesPersonasList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
