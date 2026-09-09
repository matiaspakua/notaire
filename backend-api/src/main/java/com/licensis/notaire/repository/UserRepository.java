package com.licensis.notaire.repository;

import com.licensis.notaire.business.User;
import com.licensis.notaire.business.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByName(String name);

    Optional<User> findByFkIdPerson(Person person);

    Optional<User> findByFkIdPersonIdPerson(Integer idPerson);

    // A persona may be linked to more than one usuario; "findFirst" avoids a
    // NonUniqueResult 500 when several rows match.
    Optional<User> findFirstByFkIdPersonIdPerson(Integer idPerson);

    List<User> findByStatus(boolean status);

    boolean existsByName(String name);
}
