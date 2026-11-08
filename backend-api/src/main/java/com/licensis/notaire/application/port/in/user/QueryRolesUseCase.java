package com.licensis.notaire.application.port.in.user;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.user.Role;

public interface QueryRolesUseCase {

    List<Role> findAll();

    Optional<Role> findById(int id);
}
