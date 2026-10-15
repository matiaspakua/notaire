package com.licensis.notaire.application.port.out.user;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.user.User;

/** User storage in domain terms (issue #1731). */
public interface UserCatalogPort {

    List<User> findAll();

    Optional<User> findById(int id);

    Optional<User> findFirstByPersonId(int personId);

    boolean existsById(int id);

    User save(User user);

    boolean deleteById(int id);
}
