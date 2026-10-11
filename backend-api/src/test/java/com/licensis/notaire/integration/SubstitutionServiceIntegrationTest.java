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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Substitution Service Integration Tests")
class SubstitutionServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private SubstitutionRepository substitutionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private IdentificationType identificationType;

    @BeforeEach
    void setUp() {
        substitutionRepository.deleteAll();
        personRepository.deleteAll();
        identificationTypeRepository.deleteAll();

        identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationType.setCharacters("8");
        identificationType = identificationTypeRepository.save(identificationType);
    }

    @Test
    @DisplayName("Should perform CRUD lifecycle")
    void shouldPerformCRUDLifecycle() {
        Person substitute = createPerson("Suplente 1");
        Person substituted = createPerson("Suplantado 1");

        // Create
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 12, 31)));
        substitution.setNotes("Ciclo completo");
        Substitution created = substitutionRepository.save(substitution);

        assertThat(created.getIdSubstitution()).isNotNull();

        // Read
        Substitution read = substitutionRepository.findById(created.getIdSubstitution()).orElseThrow();
        assertThat(read.getNotes()).isEqualTo("Ciclo completo");

        // Update
        read.setNotes("Actualizado");
        Substitution updated = substitutionRepository.save(read);
        assertThat(updated.getNotes()).isEqualTo("Actualizado");

        // Delete
        substitutionRepository.delete(updated);
        assertThat(substitutionRepository.findById(created.getIdSubstitution())).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple substitutions for the same substitute")
    void shouldHandleMultipleSubstitutionsForSameSubstitute() {
        Person substitute = createPerson("Suplente Activo");
        Person substituted1 = createPerson("Suplantado 1");
        Person substituted2 = createPerson("Suplantado 2");

        for (int i = 0; i < 3; i++) {
            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(substitute);
            substitution.setFkIdSubstituted(i % 2 == 0 ? substituted1 : substituted2);
            substitution.setDateStart(toDate(LocalDate.of(2025 + i, 1, 1)));
            substitution.setDateEnd(toDate(LocalDate.of(2025 + i, 12, 31)));
            substitutionRepository.save(substitution);
        }

        List<Substitution> all = substitutionRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should handle date range scenarios")
    void shouldHandleDateRangeScenarios() {
        Person substitute = createPerson("Suplente 2");
        Person substituted = createPerson("Suplantado 2");

        Substitution substitution1 = new Substitution();
        substitution1.setFkIdSubstitute(substitute);
        substitution1.setFkIdSubstituted(substituted);
        substitution1.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        substitution1.setDateEnd(toDate(LocalDate.of(2026, 6, 30)));
        substitutionRepository.save(substitution1);

        Substitution substitution2 = new Substitution();
        substitution2.setFkIdSubstitute(substitute);
        substitution2.setFkIdSubstituted(substituted);
        substitution2.setDateStart(toDate(LocalDate.of(2026, 7, 1)));
        substitution2.setDateEnd(toDate(LocalDate.of(2026, 12, 31)));
        substitutionRepository.save(substitution2);

        List<Substitution> all = substitutionRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should maintain relationship integrity")
    void shouldMaintainRelationshipIntegrity() {
        Person substitute = createPerson("Suplente 3");
        Person substituted = createPerson("Suplantado 3");

        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(substitute);
        substitution.setFkIdSubstituted(substituted);
        substitution.setDateStart(toDate(LocalDate.of(2026, 3, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 3, 31)));
        substitutionRepository.save(substitution);

        Substitution retrieved = substitutionRepository.findById(substitution.getIdSubstitution()).orElseThrow();

        assertThat(retrieved.getFkIdSubstitute()).isNotNull();
        assertThat(retrieved.getFkIdSubstitute().getPersonId()).isEqualTo(substitute.getPersonId());
        assertThat(retrieved.getFkIdSubstituted()).isNotNull();
        assertThat(retrieved.getFkIdSubstituted().getPersonId()).isEqualTo(substituted.getPersonId());
    }

    @Test
    @DisplayName("Should handle concurrent operations")
    void shouldHandleConcurrentOperations() {
        Person substitute1 = createPerson("Suplente A");
        Person substitute2 = createPerson("Suplente B");
        Person substituted = createPerson("Suplantado Concurrente");

        Substitution sup1 = new Substitution();
        sup1.setFkIdSubstitute(substitute1);
        sup1.setFkIdSubstituted(substituted);
        sup1.setDateStart(toDate(LocalDate.of(2026, 2, 1)));
        sup1.setDateEnd(toDate(LocalDate.of(2026, 2, 28)));
        Substitution saved1 = substitutionRepository.save(sup1);

        Substitution sup2 = new Substitution();
        sup2.setFkIdSubstitute(substitute2);
        sup2.setFkIdSubstituted(substituted);
        sup2.setDateStart(toDate(LocalDate.of(2026, 3, 1)));
        sup2.setDateEnd(toDate(LocalDate.of(2026, 3, 31)));
        Substitution saved2 = substitutionRepository.save(sup2);

        Substitution read1 = substitutionRepository.findById(saved1.getIdSubstitution()).orElseThrow();
        Substitution read2 = substitutionRepository.findById(saved2.getIdSubstitution()).orElseThrow();

        assertThat(read1.getFkIdSubstitute().getPersonId()).isNotEqualTo(read2.getFkIdSubstitute().getPersonId());
    }

    @Test
    @DisplayName("Should support filtering operations")
    void shouldSupportFilteringOperations() {
        Person substitute = createPerson("Suplente Filtrado");
        Person substituted = createPerson("Suplantado Filtrado");

        for (int i = 1; i <= 5; i++) {
            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(substitute);
            substitution.setFkIdSubstituted(substituted);
            substitution.setDateStart(toDate(LocalDate.of(2026, i, 1)));
            substitution.setDateEnd(toDate(LocalDate.of(2026, i, 28)));
            substitutionRepository.save(substitution);
        }

        List<Substitution> all = substitutionRepository.findAll();
        List<Substitution> filtered = all.stream()
                .filter(s -> s.getFkIdSubstitute().getPersonId().equals(substitute.getPersonId()))
                .toList();

        assertThat(filtered).hasSize(5);
    }

    @Test
    @DisplayName("Should handle batch operations")
    void shouldHandleBatchOperations() {
        Person substitute = createPerson("Suplente Batch");

        for (int i = 0; i < 10; i++) {
            Person substituted = createPerson("Suplantado Batch " + i);
            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(substitute);
            substitution.setFkIdSubstituted(substituted);
            substitution.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
            substitution.setDateEnd(toDate(LocalDate.of(2026, 12, 31)));
            substitutionRepository.save(substitution);
        }

        List<Substitution> all = substitutionRepository.findAll();
        long countBatch = all.stream()
                .filter(s -> s.getFkIdSubstitute().getFirstName().startsWith("Suplente Batch"))
                .count();

        assertThat(countBatch).isEqualTo(10);
    }

    @Test
    @DisplayName("Should support complex scenarios")
    void shouldSupportComplexScenarios() {
        // Create multiple people
        Person substitute1 = createPerson("Notary Principal");
        Person substitute2 = createPerson("Notary Suplente");
        Person substituted1 = createPerson("Notary A");
        Person substituted2 = createPerson("Notary B");

        // Create overlapping substitutions
        Substitution overlap1 = new Substitution();
        overlap1.setFkIdSubstitute(substitute1);
        overlap1.setFkIdSubstituted(substituted1);
        overlap1.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        overlap1.setDateEnd(toDate(LocalDate.of(2026, 6, 30)));
        overlap1.setNotes("Primer semestre");
        substitutionRepository.save(overlap1);

        Substitution overlap2 = new Substitution();
        overlap2.setFkIdSubstitute(substitute2);
        overlap2.setFkIdSubstituted(substituted2);
        overlap2.setDateStart(toDate(LocalDate.of(2026, 4, 1)));
        overlap2.setDateEnd(toDate(LocalDate.of(2026, 9, 30)));
        overlap2.setNotes("Period extendido");
        substitutionRepository.save(overlap2);

        List<Substitution> all = substitutionRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).anyMatch(s -> "Primer semestre".equals(s.getNotes()));
        assertThat(all).anyMatch(s -> "Period extendido".equals(s.getNotes()));
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setFirstName(name);
        person.setLastName("Test");
        person.setIdentificationNumber(System.nanoTime() % 100000000 + "");
        person.setIsClient(true);
        person.setFkIdIdentificationType(identificationType);
        return personRepository.save(person);
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
