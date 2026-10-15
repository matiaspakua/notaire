package com.licensis.notaire.adapter.out.persistence.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.user.RoleCatalogPort;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.RoleNotFoundException;
import com.licensis.notaire.repository.RoleRepository;

/** Maps legacy {@code business.Role} to the domain type (issue #1731). */
@Component
public class RoleCatalogPersistenceAdapter implements RoleCatalogPort {

    private final RoleRepository roles;

    public RoleCatalogPersistenceAdapter(RoleRepository roles) {
        this.roles = roles;
    }

    @Override
    public List<Role> findAll() {
        return roles.findAll().stream().map(RoleCatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<Role> findById(int id) {
        return roles.findById(id).map(RoleCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roles.findByName(name).map(RoleCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return roles.existsById(id);
    }

    @Override
    public Role save(Role role) {
        com.licensis.notaire.business.Role entity = role.id() == null
                ? new com.licensis.notaire.business.Role()
                : roles.findById(role.id()).orElseThrow(() -> new RoleNotFoundException(role.id()));
        entity.setName(role.name());
        entity.setDescription(role.description());
        entity.setActive(role.active());
        // Hibernate ElementCollection needs a mutable list (domain Role uses an immutable copy).
        entity.setModulos(role.modules() == null ? new ArrayList<>() : new ArrayList<>(role.modules()));
        return toDomain(roles.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!roles.existsById(id)) {
            return false;
        }
        roles.deleteById(id);
        return true;
    }

    private static Role toDomain(com.licensis.notaire.business.Role entity) {
        return Role.restore(
                entity.getIdRole(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getModulos(),
                entity.getVersion());
    }
}
