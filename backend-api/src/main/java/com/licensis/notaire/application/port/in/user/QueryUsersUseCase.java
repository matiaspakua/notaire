package com.licensis.notaire.application.port.in.user;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.user.User;

public interface QueryUsersUseCase {

    List<User> findAll();

    Optional<User> findById(int id);

    Optional<User> findByPersonId(int personId);
}
