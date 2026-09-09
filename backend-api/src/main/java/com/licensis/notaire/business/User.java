/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoUser;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "usuarios")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "Usuario.findByIdUsuario", query = "SELECT u FROM User u WHERE u.idUser = :idUsuario"),
        @NamedQuery(name = "Usuario.findByEstado", query = "SELECT u FROM User u WHERE u.status = :estado"),
        @NamedQuery(name = "Usuario.findByFkIdPersona", query = "SELECT u FROM User u WHERE u.fkIdPerson.idPerson = :idPersona")
})
public class User implements Serializable, Persistable<Integer> {

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_usuario")
    private Integer idUser;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Basic(optional = false)
    @Column(name = "contrasenia")
    private String password;
    @Basic(optional = false)
    @Column(name = "estado")
    private boolean status;
    @Basic(optional = false)
    @Column(name = "tipo")
    private String type;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdUser", fetch = FetchType.LAZY)
    private List<AuditRecord> auditRecordList;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "usuariosList", "presupuestosList", "tramiteList", "suplenciaEscribanoList", "suplenciaReemplazadoList"})
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Person fkIdPerson;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "fk_id_rol", referencedColumnName = "id_rol")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Role role;

    public User() {
    }

    public User(Integer idUser) {
        this.idUser = idUser;
    }

    public User(Integer idUser, String name, String password, boolean status, String type) {
        this.idUser = idUser;
        this.name = name;
        this.password = password;
        this.status = status;
        this.type = type;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idUser;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idUser == null || idUser.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlTransient
    @JsonIgnore
    public List<AuditRecord> getAuditRecordList() {
        return auditRecordList;
    }

    public void setAuditRecordList(List<AuditRecord> auditRecordList) {
        this.auditRecordList = auditRecordList;
    }

    public Person getFkIdPerson() {
        return fkIdPerson;
    }

    public void setFkIdPerson(Person fkIdPerson) {
        this.fkIdPerson = fkIdPerson;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idUser != null ? idUser.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.idUser == null && other.idUser != null)
                || (this.idUser != null && !this.idUser.equals(other.idUser))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Usuarios[ version=" + version + " ]"
                + "[ idUsuario=" + idUser + " ]"
                + "[ nombre=" + name + " ]"
                + "[ tipo=" + type + " ] "
                + "[ estado=" + status + " ]";
    }

    public void setAtributos(DtoUser dtoUser) {

        // Ref persona de Usuario
        Person miPerson = new Person();
        miPerson.setAtributos(dtoUser.getPersons());
        setFkIdPerson(miPerson);

        // Atributos usuario
        setPassword(dtoUser.getPassword());
        ;
        setStatus(dtoUser.isStatus());
        setIdUser(dtoUser.getIdUser());
        setName(dtoUser.getName());
        setType(dtoUser.getType());

        if (dtoUser.getVersion() != null) {
            // Controlo Version Objeto
            setVersion(dtoUser.getVersion());
        }

    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoUser getDto() {
        DtoUser miDto = new DtoUser();
        try {
            miDto.setPassword(password);
            miDto.setStatus(status);
            miDto.setIdUser(idUser);
            miDto.setName(name);

            DtoPerson miDtoPerson = new DtoPerson();
            miDtoPerson = this.getFkIdPerson().getDto();

            miDto.setPersons(miDtoPerson);
            miDto.setType(type);

            if (role != null) {
                miDto.setRoleId(role.getIdRole());
                miDto.setRoleName(role.getName());
            }

            // Controlo la version del objeto
            miDto.setVersion(version);

        } catch (NullPointerException e) {
            System.out.println("Error Metodo : getDtoUsuario");
        }
        return miDto;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
