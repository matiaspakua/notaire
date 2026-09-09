/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoIdentificationType;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "tipos_identificacion")
@XmlRootElement
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NamedQueries({
        @NamedQuery(name = "TipoIdentificacion.findAll", query = "SELECT t FROM IdentificationType t"),
        @NamedQuery(name = "TipoIdentificacion.findByIdTipoIdentificacion", query = "SELECT t FROM IdentificationType t WHERE t.idIdentificationType = :idTipoIdentificacion")
})
public class IdentificationType implements Serializable, Persistable<Integer> {

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_identificacion")
    private Integer idIdentificationType;
    @Basic(optional = false)
    @Column(name = "nombre")
    @NotBlank
    private String name;
    // Column is NOT NULL in the schema; map it so inserts populate it
    // (previously missing → "null value in column caracteres" 500 on create).
    @Column(name = "caracteres")
    private String characters;
    @XmlTransient
    @JsonIgnore
    @JsonIgnoreProperties("fkIdTipoIdentificacion")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdIdentificationType", fetch = FetchType.LAZY)
    private List<Person> personList;

    public IdentificationType() {
    }

    public IdentificationType(Integer idIdentificationType) {
        this.idIdentificationType = idIdentificationType;
    }

    public IdentificationType(Integer idIdentificationType, String name) {
        this.idIdentificationType = idIdentificationType;
        this.name = name;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idIdentificationType;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idIdentificationType == null || idIdentificationType.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdIdentificationType() {
        return idIdentificationType;
    }

    public void setIdIdentificationType(Integer idIdentificationType) {
        this.idIdentificationType = idIdentificationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCharacters() {
        return characters;
    }

    public void setCharacters(String characters) {
        this.characters = characters;
    }

    @XmlTransient
    @JsonIgnore
    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idIdentificationType != null ? idIdentificationType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IdentificationType)) {
            return false;
        }
        IdentificationType other = (IdentificationType) object;
        if ((this.idIdentificationType == null && other.idIdentificationType != null)
                || (this.idIdentificationType != null
                        && !this.idIdentificationType.equals(other.idIdentificationType))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TipoIdentificacion[ idTipoIdentificacion=" + idIdentificationType + " ]"
                + "[ nombre=" + name + " ]";
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoIdentificationType getDto() {

        DtoIdentificationType miDto = new DtoIdentificationType();

        try {
            miDto.setIdIdentificationType(this.getIdIdentificationType());
            miDto.setName(this.getName());

        } catch (NullPointerException e) {
            System.out.println("Erro getDto Tipo Identificacion");
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
