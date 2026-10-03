package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

/**
 * CU17 / CU18 — database uniqueness of person identification type + number
 * (Issue #799). Bypasses {@code PersonService} so the persistence constraint
 * is proven independently of the #835 service-layer check.
 */
@RequirementCoverage({"CU17", "CU18"})
@DisplayName("Person identification uniqueness at persistence (CU17/CU18)")
class PersonIdentificationUniquenessIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private IdentificationType identificationType;

    @BeforeEach
    void setUp() {
        identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationType.setCharacters("8");
        identificationType = identificationTypeRepository.saveAndFlush(identificationType);
    }

    @Test
    @DisplayName("Should reject a second insert with the same type and identification number")
    void shouldRejectSecondInsertWithSameTypeAndNumber() {
        Person first = newPerson("799-UNIQUE-001");
        personRepository.saveAndFlush(first);

        Person duplicate = newPerson("799-UNIQUE-001");

        assertThatThrownBy(() -> personRepository.saveAndFlush(duplicate))
                .isInstanceOfAny(DataIntegrityViolationException.class, JpaSystemException.class);
    }

    private Person newPerson(String identificationNumber) {
        Person person = new Person();
        person.setFirstName("Unique");
        person.setLastName("Constraint");
        person.setIdentificationNumber(identificationNumber);
        person.setIsClient(true);
        person.setFkIdIdentificationType(identificationType);
        return person;
    }
}
