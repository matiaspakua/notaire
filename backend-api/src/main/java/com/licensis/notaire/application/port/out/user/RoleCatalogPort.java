package com.licensis.notaire.application.port.out.user;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.user.Role;

/** Role storage in domain terms (issue #1731). Module grants are stored only; enforcement is #1683. */
public interface RoleCatalogPort {

    List<Role> findAll();

    Optional<Role> findById(int id);

    Optional<Role> findByName(String name);

    boolean existsById(int id);

    Role save(Role role);

    boolean deleteById(int id);
}
