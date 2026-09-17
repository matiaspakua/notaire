package com.licensis.notaire.application.port.out.user;

import com.licensis.notaire.business.Role;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for role persistence operations.
 */
public interface RoleRepositoryPort {

    List<Role> findAll();

    Optional<Role> findById(Integer id);

    Role save(Role entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    Optional<Role> findByName(String name);
}
