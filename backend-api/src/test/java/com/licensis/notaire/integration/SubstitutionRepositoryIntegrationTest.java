package com.licensis.notaire.integration;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.SubstitutionRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Substitution Repository Integration Tests")
class SubstitutionRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private SubstitutionRepository substitutionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private Person substitute;
    private Person substituted;
    private IdentificationType identificationType;

    @BeforeEach
    void setUp() {
        substitutionRepository.deleteAll();
        personRepository.deleteAll();

        identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationType.setCharacters("8");
        identificationType = identificationTypeRepository.save(identificationType);

        substitute = new Person();
        substitute.setFirstName("Juan");
        substitute.setLastName("Substitute");
        substitute.setIdentificationNumber("12345678");
        substitute.setIsClient(true);
        substitute.setFkIdIdentificationType(identificationType);
        substitute = personRepository.save(substitute);

        substituted = new Person();
        substituted.setFirstName("Pedro");
        substituted.setLastName("Substituted");
        substituted.setIdentificationNumber("87654321");
        substituted.setIsClient(true);
        substituted.setFkIdIdentificationType(identificationType);
        substituted = personRepository.save(substituted);
    }

    @Test
    @DisplayName("Should persist substitution with all fields")
    void shouldPersistSubstitutionWithAllFields() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 6, 30)));
        substitution.setNotes("Test substitution");

        Substitution saved = substitutionRepository.save(substitution);

        assertThat(saved.getIdSubstitution()).isNotNull();
        assertThat(saved.getFkIdSubstitute().getPersonId()).isEqualTo(substitute.getPersonId());
        assertThat(saved.getFkIdSubstituted().getPersonId()).isEqualTo(substituted.getPersonId());
        assertThat(saved.getNotes()).isEqualTo("Test substitution");
    }

    @Test
    @DisplayName("Should retrieve substitution by ID")
    void shouldRetrieveSubstitutionById() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 3, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 9, 30)));
        Substitution saved = substitutionRepository.save(substitution);

        Optional<Substitution> found = substitutionRepository.findById(saved.getIdSubstitution());

        assertThat(found).isPresent();
        assertThat(found.get().getNotes()).isNull();
    }

    @Test
    @DisplayName("Should find all substitutions")
    void shouldFindAllSubstitutions() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 1, 15)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 12, 15)));
        substitutionRepository.save(substitution);

        List<Substitution> all = substitutionRepository.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Should update substitution")
    void shouldUpdateSubstitution() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 2, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 2, 28)));
        Substitution saved = substitutionRepository.save(substitution);

        saved.setNotes("Updated");
        saved.setDateEnd(toDate(LocalDate.of(2026, 3, 31)));
        Substitution updated = substitutionRepository.save(saved);

        assertThat(updated.getNotes()).isEqualTo("Updated");

        Optional<Substitution> fetched = substitutionRepository.findById(saved.getIdSubstitution());
        assertThat(fetched.get().getNotes()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Should delete substitution")
    void shouldDeleteSubstitution() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 4, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 4, 30)));
        Substitution saved = substitutionRepository.save(substitution);
        Integer id = saved.getIdSubstitution();

        substitutionRepository.delete(saved);

        Optional<Substitution> deleted = substitutionRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should handle null notes")
    void shouldHandleNullNotes() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 5, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 5, 31)));
        substitution.setNotes(null);

        Substitution saved = substitutionRepository.save(substitution);

        assertThat(saved.getNotes()).isNull();
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
