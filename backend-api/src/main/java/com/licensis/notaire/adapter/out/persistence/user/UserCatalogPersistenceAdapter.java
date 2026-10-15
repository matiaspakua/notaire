package com.licensis.notaire.adapter.out.persistence.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Role;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.domain.user.UserNotFoundException;
import com.licensis.notaire.repository.RoleRepository;
import com.licensis.notaire.repository.UserRepository;

/** Maps legacy {@code business.User} to the domain type (issue #1731). */
@Component
public class UserCatalogPersistenceAdapter implements UserCatalogPort {

    private final UserRepository users;
    private final RoleRepository roles;

    public UserCatalogPersistenceAdapter(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    @Override
    public List<User> findAll() {
        return users.findAll().stream().map(UserCatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<User> findById(int id) {
        return users.findById(id).map(UserCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<User> findFirstByPersonId(int personId) {
        return users.findFirstByFkIdPersonIdPerson(personId).map(UserCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return users.existsById(id);
    }

    @Override
    public User save(User user) {
        com.licensis.notaire.business.User entity = user.id() == null
                ? new com.licensis.notaire.business.User()
                : users.findById(user.id()).orElseThrow(() -> new UserNotFoundException(user.id()));
        entity.setName(user.name());
        entity.setPassword(user.passwordHash());
        entity.setStatus(user.active());
        entity.setType(user.type());
        if (user.roleId() == null) {
            entity.setRole(null);
        } else {
            Role role = roles.findById(user.roleId()).orElseThrow(() -> new UserNotFoundException(user.roleId()));
            entity.setRole(role);
        }
        // Person association is not mutated by the hexagon CRUD/login paths; preserve if present.
        return toDomain(users.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!users.existsById(id)) {
            return false;
        }
        users.deleteById(id);
        return true;
    }

    private static User toDomain(com.licensis.notaire.business.User entity) {
        Person person = entity.getFkIdPerson();
        Role role = entity.getRole();
        return User.restore(
                entity.getIdUser(),
                entity.getName(),
                entity.getPassword(),
                entity.getStatus(),
                entity.getType(),
                person == null ? null : person.getPersonId(),
                person == null ? null : person.getFirstName(),
                person == null ? null : person.getLastName(),
                role == null ? null : role.getIdRole(),
                role == null ? null : role.getName(),
                entity.getVersion());
    }
}
