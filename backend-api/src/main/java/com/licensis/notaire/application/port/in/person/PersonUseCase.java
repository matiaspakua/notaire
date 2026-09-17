package com.licensis.notaire.application.port.in.person;

import com.licensis.notaire.business.Person;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for managing people in the notary system.
 */
public interface PersonUseCase {

    List<Person> findAll();

    Optional<Person> findById(Integer id);

    Person save(Person entity);

    void deleteById(Integer id);

    List<Person> search(String firstName, String lastName, String identificationNumber,
                        Integer idIdentificationType, Boolean isClient);
}
