package com.licensis.notaire.service;

import com.licensis.notaire.exception.DuplicatePersonException;
import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<Person> findAll() {
        logger.debug("Finding all people");
        return personRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Person> findById(Integer id) {
        logger.debug("Finding person by id: {}", id);
        return personRepository.findById(id);
    }

    public Person save(Person entity) {
        rejectDuplicateDocument(entity);
        logger.info("Saving person: {} {}", entity.getFirstName(), entity.getLastName());
        return personRepository.save(entity);
    }

    private void rejectDuplicateDocument(Person entity) {
        personRepository.findByNumeroIdentificacion(entity.getIdentificationNumber())
                .filter(existing -> isSameTipoIdentificacion(existing, entity))
                .filter(existing -> !existing.getPersonId().equals(entity.getPersonId()))
                .ifPresent(existing -> {
                    throw new DuplicatePersonException(
                            "A person is already registered with document " + entity.getIdentificationNumber(),
                            existing.getPersonId());
                });
    }

    private boolean isSameTipoIdentificacion(Person a, Person b) {
        return a.getFkIdIdentificationType() != null && b.getFkIdIdentificationType() != null
                && a.getFkIdIdentificationType().getIdTipoIdentificacion()
                        .equals(b.getFkIdIdentificationType().getIdTipoIdentificacion());
    }

    public void deleteById(Integer id) {
        logger.info("Deleting person with id: {}", id);
        personRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Person> search(String firstName, String lastName, String identificationNumber,
                                 Integer idTipoIdentificacion, Boolean isClient) {
        logger.debug("Searching people with filters - firstName: {}, lastName: {}, identificationNumber: {}, "
                + "idTipoIdentificacion: {}, isClient: {}", firstName, lastName, identificationNumber,
                idTipoIdentificacion, isClient);

        List<Person> results = new ArrayList<>();

        if (identificationNumber != null && !identificationNumber.isBlank()) {
            Optional<Person> person = personRepository.findByNumeroIdentificacion(identificationNumber);
            person.ifPresent(results::add);
            return results;
        }

        if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
            return personRepository.findByNombreAndApellidoContainingIgnoreCase(firstName, lastName);
        }

        if (firstName != null && !firstName.isBlank()) {
            results.addAll(personRepository.findByNombreContainingIgnoreCase(firstName));
        }

        if (lastName != null && !lastName.isBlank()) {
            results.addAll(personRepository.findByApellidoContainingIgnoreCase(lastName));
        }

        if (idTipoIdentificacion != null) {
            results.addAll(personRepository.findByFkIdTipoIdentificacionIdTipoIdentificacion(idTipoIdentificacion));
        }

        if (isClient != null) {
            results.addAll(personRepository.findByEsCliente(isClient));
        }

        return results.stream().distinct().toList();
    }
}
