package com.licensis.notaire.business;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role implements Serializable, Persistable<Integer> {

    @Version
    @Column(name = "version")
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRole;

    @Column(name = "nombre", unique = true, nullable = false)
    private String name;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "activo", nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roles_permisos", joinColumns = @JoinColumn(name = "fk_id_rol"))
    @Column(name = "modulo")
    private List<String> modulos = new ArrayList<>();

    public Role() {
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idRole;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idRole == null || idRole.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdRole() {
        return idRole;
    }

    public void setIdRole(Integer idRole) {
        this.idRole = idRole;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getModulos() {
        return modulos;
    }

    public void setModulos(List<String> modulos) {
        this.modulos = modulos != null ? modulos : new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
