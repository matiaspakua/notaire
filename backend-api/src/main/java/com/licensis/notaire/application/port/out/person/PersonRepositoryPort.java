package com.licensis.notaire.application.port.out.person;

import com.licensis.notaire.business.Person;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for person persistence operations.
 */
public interface PersonRepositoryPort {

    List<Person> findAll();

    Optional<Person> findById(Integer id);

    Person save(Person entity);

    void deleteById(Integer id);

    Optional<Person> findByIdentificationNumber(String identificationNumber);

    List<Person> findByNameAndLastNameContainingIgnoreCase(String name, String lastName);

    List<Person> findByNameContainingIgnoreCase(String name);

    List<Person> findByLastNameContainingIgnoreCase(String lastName);

    List<Person> findByFkIdIdentificationTypeIdIdentificationType(Integer idIdentificationType);

    List<Person> findByIsClient(Boolean isClient);
}
