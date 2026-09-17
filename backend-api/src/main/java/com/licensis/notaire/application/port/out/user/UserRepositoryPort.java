package com.licensis.notaire.application.port.out.user;

import com.licensis.notaire.business.User;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for user persistence operations.
 */
public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(Integer id);

    User save(User entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    Optional<User> findByName(String name);

    List<User> findByStatus(boolean status);

    boolean existsByName(String name);
}
