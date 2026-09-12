package com.licensis.notaire.integration;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Person Repository Integration Tests")
class PersonRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private IdentificationType typeId;

    @BeforeEach
    void setUp() {
        typeId = new IdentificationType();
        typeId.setName("DNI");
        typeId.setCharacters("8");
        typeId = identificationTypeRepository.save(typeId);
    }

    @Test
    @DisplayName("Should create person with required fields")
    void shouldCreatePersonWithRequiredFields() {
        Person person = new Person();
        person.setFirstName("Juan");
        person.setLastName("Pérez");
        person.setIdentificationNumber("12345678");
        person.setIsClient(true);
        person.setFkIdIdentificationType(typeId);

        Person saved = personRepository.save(person);

        assertThat(saved.getPersonId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Juan");
        assertThat(saved.getLastName()).isEqualTo("Pérez");
        assertThat(saved.getIdentificationNumber()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Should retrieve person by ID")
    void shouldRetrievePersonById() {
        Person person = new Person();
        person.setFirstName("María");
        person.setLastName("García");
        person.setIdentificationNumber("87654321");
        person.setIsClient(false);
        person.setFkIdIdentificationType(typeId);
        Person saved = personRepository.save(person);

        Optional<Person> found = personRepository.findById(saved.getPersonId());

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("María");
        assertThat(found.get().getFkIdIdentificationType()).isNotNull();
    }

    @Test
    @DisplayName("Should update person data")
    void shouldUpdatePersonData() {
        Person person = new Person();
        person.setFirstName("Carlos");
        person.setLastName("López");
        person.setIdentificationNumber("11111111");
        person.setIsClient(true);
        person.setFkIdIdentificationType(typeId);
        Person saved = personRepository.save(person);

        saved.setFirstName("Carlos Alberto");
        saved.setIsClient(false);
        Person updated = personRepository.save(saved);

        assertThat(updated.getFirstName()).isEqualTo("Carlos Alberto");
        assertThat(updated.getIsClient()).isFalse();
    }

    @Test
    @DisplayName("Should handle optional fields")
    void shouldHandleOptionalFields() {
        Person person = new Person();
        person.setFirstName("Ana");
        person.setLastName("Martínez");
        person.setIdentificationNumber("22222222");
        person.setIsClient(true);
        person.setFkIdIdentificationType(typeId);
        person.setAddress("Calle Falsa 123");
        person.setPhone("123-4567");
        person.setEmail("ana@example.com");

        Person saved = personRepository.save(person);

        assertThat(saved.getAddress()).isEqualTo("Calle Falsa 123");
        assertThat(saved.getPhone()).isEqualTo("123-4567");
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    @DisplayName("Should support multiple people")
    void shouldSupportMultiplePeople() {
        String uniqueLastName = "LastName" + System.nanoTime();
        for (int i = 0; i < 5; i++) {
            Person person = new Person();
            person.setFirstName("FirstName" + i);
            person.setLastName(uniqueLastName + i);
            person.setIdentificationNumber("ID" + (10000000 + i));
            person.setIsClient(i % 2 == 0);
            person.setFkIdIdentificationType(typeId);
            personRepository.save(person);
        }

        List<Person> created = personRepository.findAll().stream()
                .filter(p -> p.getLastName() != null && p.getLastName().startsWith(uniqueLastName))
                .toList();
        assertThat(created).hasSize(5);

        long clients = created.stream().filter(Person::getIsClient).count();
        assertThat(clients).isEqualTo(3);
    }
}
