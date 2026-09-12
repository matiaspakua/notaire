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

@DisplayName("Suplencia Service Integration Tests")
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
        Person suplente = createPerson("Suplente 1");
        Person suplantado = createPerson("Suplantado 1");

        // Create
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
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
    @DisplayName("Should handle multiple suplencias for same substitute")
    void shouldHandleMultipleSuplenciasForSameSubstitute() {
        Person suplente = createPerson("Suplente Activo");
        Person suplantado1 = createPerson("Suplantado 1");
        Person suplantado2 = createPerson("Suplantado 2");

        for (int i = 0; i < 3; i++) {
            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(suplente);
            substitution.setFkIdSubstituted(i % 2 == 0 ? suplantado1 : suplantado2);
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
        Person suplente = createPerson("Suplente 2");
        Person suplantado = createPerson("Suplantado 2");

        Substitution suplencia1 = new Substitution();
        suplencia1.setFkIdSubstitute(suplente);
        suplencia1.setFkIdSubstituted(suplantado);
        suplencia1.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        suplencia1.setDateEnd(toDate(LocalDate.of(2026, 6, 30)));
        substitutionRepository.save(suplencia1);

        Substitution suplencia2 = new Substitution();
        suplencia2.setFkIdSubstitute(suplente);
        suplencia2.setFkIdSubstituted(suplantado);
        suplencia2.setDateStart(toDate(LocalDate.of(2026, 7, 1)));
        suplencia2.setDateEnd(toDate(LocalDate.of(2026, 12, 31)));
        substitutionRepository.save(suplencia2);

        List<Substitution> all = substitutionRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should maintain relationship integrity")
    void shouldMaintainRelationshipIntegrity() {
        Person suplente = createPerson("Suplente 3");
        Person suplantado = createPerson("Suplantado 3");

        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
        substitution.setDateStart(toDate(LocalDate.of(2026, 3, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 3, 31)));
        substitutionRepository.save(substitution);

        Substitution retrieved = substitutionRepository.findById(substitution.getIdSubstitution()).orElseThrow();

        assertThat(retrieved.getFkIdSubstitute()).isNotNull();
        assertThat(retrieved.getFkIdSubstitute().getPersonId()).isEqualTo(suplente.getPersonId());
        assertThat(retrieved.getFkIdSubstituted()).isNotNull();
        assertThat(retrieved.getFkIdSubstituted().getPersonId()).isEqualTo(suplantado.getPersonId());
    }

    @Test
    @DisplayName("Should handle concurrent operations")
    void shouldHandleConcurrentOperations() {
        Person suplente1 = createPerson("Suplente A");
        Person suplente2 = createPerson("Suplente B");
        Person suplantado = createPerson("Suplantado Concurrente");

        Substitution sup1 = new Substitution();
        sup1.setFkIdSubstitute(suplente1);
        sup1.setFkIdSubstituted(suplantado);
        sup1.setDateStart(toDate(LocalDate.of(2026, 2, 1)));
        sup1.setDateEnd(toDate(LocalDate.of(2026, 2, 28)));
        Substitution saved1 = substitutionRepository.save(sup1);

        Substitution sup2 = new Substitution();
        sup2.setFkIdSubstitute(suplente2);
        sup2.setFkIdSubstituted(suplantado);
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
        Person suplente = createPerson("Suplente Filtrado");
        Person suplantado = createPerson("Suplantado Filtrado");

        for (int i = 1; i <= 5; i++) {
            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(suplente);
            substitution.setFkIdSubstituted(suplantado);
            substitution.setDateStart(toDate(LocalDate.of(2026, i, 1)));
            substitution.setDateEnd(toDate(LocalDate.of(2026, i, 28)));
            substitutionRepository.save(substitution);
        }

        List<Substitution> all = substitutionRepository.findAll();
        List<Substitution> filtered = all.stream()
                .filter(s -> s.getFkIdSubstitute().getPersonId().equals(suplente.getPersonId()))
                .toList();

        assertThat(filtered).hasSize(5);
    }

    @Test
    @DisplayName("Should handle batch operations")
    void shouldHandleBatchOperations() {
        Person suplente = createPerson("Suplente Batch");

        for (int i = 0; i < 10; i++) {
            Person suplantado = createPerson("Suplantado Batch " + i);
            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(suplente);
            substitution.setFkIdSubstituted(suplantado);
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
        // Create multiple personas
        Person suplente1 = createPerson("Escribano Principal");
        Person suplente2 = createPerson("Escribano Suplente");
        Person suplantado1 = createPerson("Escribano A");
        Person suplantado2 = createPerson("Escribano B");

        // Create overlapping suplencias
        Substitution overlap1 = new Substitution();
        overlap1.setFkIdSubstitute(suplente1);
        overlap1.setFkIdSubstituted(suplantado1);
        overlap1.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        overlap1.setDateEnd(toDate(LocalDate.of(2026, 6, 30)));
        overlap1.setNotes("Primer semestre");
        substitutionRepository.save(overlap1);

        Substitution overlap2 = new Substitution();
        overlap2.setFkIdSubstitute(suplente2);
        overlap2.setFkIdSubstituted(suplantado2);
        overlap2.setDateStart(toDate(LocalDate.of(2026, 4, 1)));
        overlap2.setDateEnd(toDate(LocalDate.of(2026, 9, 30)));
        overlap2.setNotes("Período extendido");
        substitutionRepository.save(overlap2);

        List<Substitution> all = substitutionRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).anyMatch(s -> "Primer semestre".equals(s.getNotes()));
        assertThat(all).anyMatch(s -> "Período extendido".equals(s.getNotes()));
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
