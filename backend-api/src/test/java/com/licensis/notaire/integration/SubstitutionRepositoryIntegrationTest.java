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

@DisplayName("Suplencia Repository Integration Tests")
class SubstitutionRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private SubstitutionRepository substitutionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private Person suplente;
    private Person suplantado;
    private IdentificationType identificationType;

    @BeforeEach
    void setUp() {
        substitutionRepository.deleteAll();
        personRepository.deleteAll();

        identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationType.setCharacters("8");
        identificationType = identificationTypeRepository.save(identificationType);

        suplente = new Person();
        suplente.setFirstName("Juan");
        suplente.setLastName("Suplente");
        suplente.setIdentificationNumber("12345678");
        suplente.setIsClient(true);
        suplente.setFkIdIdentificationType(identificationType);
        suplente = personRepository.save(suplente);

        suplantado = new Person();
        suplantado.setFirstName("Pedro");
        suplantado.setLastName("Suplantado");
        suplantado.setIdentificationNumber("87654321");
        suplantado.setIsClient(true);
        suplantado.setFkIdIdentificationType(identificationType);
        suplantado = personRepository.save(suplantado);
    }

    @Test
    @DisplayName("Should persist suplencia with all fields")
    void shouldPersistSubstitutionWithAllFields() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
        substitution.setDateStart(toDate(LocalDate.of(2026, 1, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 6, 30)));
        substitution.setNotes("Suplencia de prueba");

        Substitution saved = substitutionRepository.save(substitution);

        assertThat(saved.getIdSubstitution()).isNotNull();
        assertThat(saved.getFkIdSubstitute().getPersonId()).isEqualTo(suplente.getPersonId());
        assertThat(saved.getFkIdSubstituted().getPersonId()).isEqualTo(suplantado.getPersonId());
        assertThat(saved.getNotes()).isEqualTo("Suplencia de prueba");
    }

    @Test
    @DisplayName("Should retrieve suplencia by ID")
    void shouldRetrieveSubstitutionById() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
        substitution.setDateStart(toDate(LocalDate.of(2026, 3, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 9, 30)));
        Substitution saved = substitutionRepository.save(substitution);

        Optional<Substitution> found = substitutionRepository.findById(saved.getIdSubstitution());

        assertThat(found).isPresent();
        assertThat(found.get().getNotes()).isNull();
    }

    @Test
    @DisplayName("Should find all suplencias")
    void shouldFindAllSuplencias() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
        substitution.setDateStart(toDate(LocalDate.of(2026, 1, 15)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 12, 15)));
        substitutionRepository.save(substitution);

        List<Substitution> all = substitutionRepository.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Should update suplencia")
    void shouldUpdateSubstitution() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
        substitution.setDateStart(toDate(LocalDate.of(2026, 2, 1)));
        substitution.setDateEnd(toDate(LocalDate.of(2026, 2, 28)));
        Substitution saved = substitutionRepository.save(substitution);

        saved.setNotes("Actualizado");
        saved.setDateEnd(toDate(LocalDate.of(2026, 3, 31)));
        Substitution updated = substitutionRepository.save(saved);

        assertThat(updated.getNotes()).isEqualTo("Actualizado");

        Optional<Substitution> fetched = substitutionRepository.findById(saved.getIdSubstitution());
        assertThat(fetched.get().getNotes()).isEqualTo("Actualizado");
    }

    @Test
    @DisplayName("Should delete suplencia")
    void shouldDeleteSubstitution() {
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
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
        substitution.setFkIdSubstitute(suplente);
        substitution.setFkIdSubstituted(suplantado);
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
