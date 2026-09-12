package com.licensis.notaire.integration;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PersonService Integration Tests")
class PersonServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private Person testPerson;
    private IdentificationType identificationType;

    @BeforeEach
    void setUp() {
        identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationTypeRepository.save(identificationType);

        testPerson = new Person();
        testPerson.setFirstName("Juan");
        testPerson.setLastName("Pérez");
        testPerson.setIdentificationNumber("DNI-" + UUID.randomUUID().toString().substring(0, 8));
        testPerson.setIsClient(false);
        testPerson.setFkIdIdentificationType(identificationType);
    }

    @Test
    @DisplayName("Should persist person through service")
    void shouldPersistPersonThroughService() {
        Person saved = personService.save(testPerson);

        assertThat(saved.getPersonId()).isNotNull();
        assertThat(personRepository.findById(saved.getPersonId())).isPresent();
    }

    @Test
    @DisplayName("Should find person by id through service")
    void shouldFindPersonByIdThroughService() {
        Person saved = personService.save(testPerson);

        Optional<Person> found = personService.findById(saved.getPersonId());

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getFirstName()).isEqualTo("Juan"));
    }

    @Test
    @DisplayName("Should find all people through service")
    void shouldFindAllPeopleThroughService() {
        personService.save(testPerson);

        List<Person> all = personService.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getFirstName().equals("Juan"));
    }

    @Test
    @DisplayName("Should delete person through service")
    void shouldDeletePersonThroughService() {
        Person saved = personService.save(testPerson);

        personService.deleteById(saved.getPersonId());

        Optional<Person> deleted = personRepository.findById(saved.getPersonId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should search people with all filters")
    void shouldSearchPeopleWithAllFilters() {
        Person saved = personService.save(testPerson);

        List<Person> found = personService.search(
                "Juan",
                "Pérez",
                saved.getIdentificationNumber(),
                identificationType.getIdIdentificationType(),
                false
        );

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId()));
    }

    @Test
    @DisplayName("Should search people with firstName filter only")
    void shouldSearchPeopleWithFirstNameOnly() {
        Person saved = personService.save(testPerson);

        List<Person> found = personService.search("Juan", null, null, null, null);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId()));
    }

    @Test
    @DisplayName("Should search people with isClient filter")
    void shouldSearchPeopleWithIsClientFilter() {
        Person client = new Person();
        client.setFirstName("Carlos");
        client.setLastName("Lopez");
        client.setIdentificationNumber("ID-" + UUID.randomUUID().toString().substring(0, 8));
        client.setIsClient(true);
        client.setFkIdIdentificationType(identificationType);
        Person saved = personService.save(client);

        List<Person> found = personService.search(null, null, null, null, true);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId())
                        && p.getIsClient() == true);
    }

    @Test
    @DisplayName("Should update person through service")
    void shouldUpdatePersonThroughService() {
        Person saved = personService.save(testPerson);

        saved.setFirstName("Pedro");
        saved.setLastName("Garcia");
        personService.save(saved);

        Optional<Person> updated = personRepository.findById(saved.getPersonId());
        assertThat(updated).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getFirstName()).isEqualTo("Pedro");
                    assertThat(p.getLastName()).isEqualTo("Garcia");
                });
    }

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        List<Person> found = personService.search(
                "NonexistentName",
                null,
                null,
                null,
                null
        );

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should maintain transaction consistency")
    void shouldMaintainTransactionConsistency() {
        Person saved = personService.save(testPerson);
        Integer savedId = saved.getPersonId();

        personService.deleteById(savedId);
        Optional<Person> found = personService.findById(savedId);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should search people with lastName filter only")
    void shouldSearchPeopleWithLastNameOnly() {
        Person saved = personService.save(testPerson);

        List<Person> found = personService.search(null, "Pérez", null, null, null);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId()));
    }

    @Test
    @DisplayName("Should search people with identificationNumber filter only")
    void shouldSearchPeopleWithIdentificationNumberOnly() {
        Person saved = personService.save(testPerson);

        List<Person> found = personService.search(null, null, saved.getIdentificationNumber(), null, null);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId()));
    }

    @Test
    @DisplayName("Should search people with identificationType filter only")
    void shouldSearchPeopleWithIdentificationTypeOnly() {
        Person saved = personService.save(testPerson);

        List<Person> found = personService.search(
                null,
                null,
                null,
                identificationType.getIdIdentificationType(),
                null
        );

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId()));
    }

    @Test
    @DisplayName("Should return empty list when searching with no matches")
    void shouldReturnEmptyListWhenSearchingWithNoMatches() {
        List<Person> found = personService.search(
                "NonexistentName",
                "NonexistentLastName",
                "99999999",
                null,
                null
        );

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should search people with combination of filters")
    void shouldSearchPeopleWithCombinationOfFilters() {
        Person saved = personService.save(testPerson);

        List<Person> found = personService.search(
                "Juan",
                "Pérez",
                saved.getIdentificationNumber(),
                identificationType.getIdIdentificationType(),
                false
        );

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId()));
    }

    @Test
    @DisplayName("Should create multiple people with different data")
    void shouldCreateMultiplePeopleWithDifferentData() {
        Person p1 = new Person();
        p1.setFirstName("Juan");
        p1.setLastName("Pérez");
        p1.setIdentificationNumber("ID-" + UUID.randomUUID().toString().substring(0, 8));
        p1.setIsClient(true);
        p1.setFkIdIdentificationType(identificationType);

        Person p2 = new Person();
        p2.setFirstName("María");
        p2.setLastName("García");
        p2.setIdentificationNumber("ID-" + UUID.randomUUID().toString().substring(0, 8));
        p2.setIsClient(false);
        p2.setFkIdIdentificationType(identificationType);

        Person saved1 = personService.save(p1);
        Person saved2 = personService.save(p2);

        assertThat(saved1.getPersonId()).isNotNull();
        assertThat(saved2.getPersonId()).isNotNull();
        assertThat(saved1.getPersonId()).isNotEqualTo(saved2.getPersonId());

        List<Person> all = personService.findAll();
        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved1.getPersonId()))
                .anyMatch(p -> p.getPersonId().equals(saved2.getPersonId()));
    }

    @Test
    @DisplayName("Should update person fields")
    void shouldUpdatePersonFields() {
        Person saved = personService.save(testPerson);

        saved.setFirstName("Carlos");
        saved.setLastName("López");
        saved.setIsClient(true);
        Person updated = personService.save(saved);

        assertThat(updated)
                .hasFieldOrPropertyWithValue("name", "Carlos")
                .hasFieldOrPropertyWithValue("lastName", "López")
                .hasFieldOrPropertyWithValue("isClient", true);
    }

    @Test
    @DisplayName("Should find person by id even after update")
    void shouldFindPersonByIdEvenAfterUpdate() {
        Person saved = personService.save(testPerson);
        Integer id = saved.getPersonId();

        saved.setFirstName("UpdatedName");
        personService.save(saved);

        Optional<Person> found = personService.findById(id);

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getFirstName()).isEqualTo("UpdatedName"));
    }

    @Test
    @DisplayName("Should search people filtering by isClient = false")
    void shouldSearchPeopleFilteringByIsClientFalse() {
        Person notClient = new Person();
        notClient.setFirstName("Abogado");
        notClient.setLastName("Penal");
        notClient.setIdentificationNumber("ID-" + UUID.randomUUID().toString().substring(0, 8));
        notClient.setIsClient(false);
        notClient.setFkIdIdentificationType(identificationType);

        Person saved = personService.save(notClient);

        List<Person> found = personService.search(null, null, null, null, false);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getPersonId().equals(saved.getPersonId())
                        && p.getIsClient() == false);
    }
}
