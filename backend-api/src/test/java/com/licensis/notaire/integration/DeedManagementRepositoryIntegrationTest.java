package com.licensis.notaire.integration;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GestionDeEscrituraRepository Integration Tests")
class DeedManagementRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private DeedManagementRepository managementRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    @Autowired
    private ManagementStatusRepository statusRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    private DeedManagement testManagement;
    private Person testNotary;
    private ManagementStatus testStatus;

    @BeforeEach
    void setUp() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setName("Profesional");
        identificationTypeRepository.save(identificationType);

        testNotary = new Person();
        testNotary.setFirstName("Escribano");
        testNotary.setLastName("Test");
        testNotary.setIdentificationNumber("87654321");
        testNotary.setIsClient(false);
        testNotary.setFkIdIdentificationType(identificationType);
        personRepository.save(testNotary);

        testStatus = new ManagementStatus();
        testStatus.setName("INICIADO");
        testStatus = statusRepository.save(testStatus);

        testManagement = new DeedManagement();
        testManagement.setNumber((int) (System.currentTimeMillis() % 10000));
        testManagement.setEncabezado("Venta Inmueble");
        testManagement.setDateStart(new Date());
        testManagement.setFkIdNotaryPerson(testNotary);
        testManagement.setFkIdManagementStatus(testStatus);
    }

    @Test
    @DisplayName("Should persist and retrieve gestion")
    void shouldPersistAndRetrieveManagement() {
        DeedManagement saved = managementRepository.save(testManagement);

        assertThat(saved.getIdManagement()).isNotNull();

        Optional<DeedManagement> retrieved = managementRepository.findById(saved.getIdManagement());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getNumber()).isEqualTo(saved.getNumber());
                    assertThat(g.getEncabezado()).isEqualTo("Venta Inmueble");
                });
    }

    @Test
    @DisplayName("Should find gestion by number")
    void shouldFindByNumber() {
        DeedManagement saved = managementRepository.save(testManagement);

        Optional<DeedManagement> found = managementRepository.findByNumber(saved.getNumber());

        assertThat(found).isPresent()
                .hasValueSatisfying(g -> assertThat(g.getEncabezado()).isEqualTo("Venta Inmueble"));
    }

    @Test
    @DisplayName("Should find gestiones by notary")
    void shouldFindByNotary() {
        managementRepository.save(testManagement);

        List<DeedManagement> found = managementRepository.findByFkIdNotaryPersonIdPerson(testNotary.getPersonId());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdNotaryPerson().getPersonId().equals(testNotary.getPersonId()));
    }

    @Test
    @DisplayName("Should find gestiones by status")
    void shouldFindByStatus() {
        managementRepository.save(testManagement);

        List<DeedManagement> found = managementRepository.findByFkIdManagementStatusIdManagementStatus(testStatus.getIdManagementStatus());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdManagementStatus().getIdManagementStatus().equals(testStatus.getIdManagementStatus()));
    }

    @Test
    @DisplayName("Should find gestiones by notary and status")
    void shouldFindByNotaryAndStatus() {
        managementRepository.save(testManagement);

        List<DeedManagement> found = managementRepository
                .findByFkIdNotaryPersonIdPersonAndFkIdStatusIdManagementStatus(
                        testNotary.getPersonId(),
                        testStatus.getIdManagementStatus());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdNotaryPerson().getPersonId().equals(testNotary.getPersonId())
                        && g.getFkIdManagementStatus().getIdManagementStatus().equals(testStatus.getIdManagementStatus()));
    }

    @Test
    @DisplayName("Should find gestiones by date range")
    void shouldFindByDateStartBetween() {
        DeedManagement saved = managementRepository.save(testManagement);

        Date startDate = new Date(saved.getDateStart().getTime() - 86400000);
        Date endDate = new Date(saved.getDateStart().getTime() + 86400000);

        List<DeedManagement> found = managementRepository.findByDateStartBetween(startDate, endDate);

        assertThat(found).isNotEmpty()
                .anyMatch(g -> g.getIdManagement().equals(saved.getIdManagement()));
    }

    @Test
    @DisplayName("Should find gestiones by notes containing")
    void shouldFindByNotesContaining() {
        testManagement.setNotes("Venta de propiedad");
        managementRepository.save(testManagement);

        List<DeedManagement> found = managementRepository.findByNotesContaining("propiedad");

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getNotes().contains("propiedad"));
    }

    @Test
    @DisplayName("Should update gestion")
    void shouldUpdateManagement() {
        DeedManagement saved = managementRepository.save(testManagement);

        saved.setNotes("Actualizado");
        managementRepository.save(saved);

        Optional<DeedManagement> updated = managementRepository.findById(saved.getIdManagement());

        assertThat(updated).isPresent()
                .hasValueSatisfying(g -> assertThat(g.getNotes()).isEqualTo("Actualizado"));
    }

    @Test
    @DisplayName("Should delete gestion")
    void shouldDeleteManagement() {
        DeedManagement saved = managementRepository.save(testManagement);

        managementRepository.deleteById(saved.getIdManagement());

        Optional<DeedManagement> deleted = managementRepository.findById(saved.getIdManagement());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should maintain referential integrity with notary")
    void shouldMaintainReferentialIntegrityWithNotary() {
        DeedManagement saved = managementRepository.save(testManagement);

        Optional<DeedManagement> retrieved = managementRepository.findById(saved.getIdManagement());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getFkIdNotaryPerson()).isNotNull();
                    assertThat(g.getFkIdNotaryPerson().getPersonId()).isEqualTo(testNotary.getPersonId());
                });
    }

    @Test
    @DisplayName("Should find all gestiones paginated")
    void shouldFindAllPaginated() {
        DeedManagement saved = managementRepository.save(testManagement);

        Page<DeedManagement> page = managementRepository.findAll(PageRequest.of(0, 10));

        assertThat(page).isNotEmpty()
                .anyMatch(g -> g.getIdManagement().equals(saved.getIdManagement()));
    }

    @Test
    @DisplayName("Should find gestiones by cliente person id (CU19)")
    void shouldFindByClientPersonId() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationTypeRepository.save(identificationType);

        Person client = new Person();
        client.setFirstName("Cliente");
        client.setLastName("Test");
        client.setIdentificationNumber("12345678");
        client.setIsClient(true);
        client.setFkIdIdentificationType(identificationType);
        personRepository.save(client);

        Budget budget = new Budget();
        budget.setNumber((int) (System.currentTimeMillis() % 10000));
        budget.setDate(new Date());
        budget.setEncabezado("Presupuesto Test");
        budget.setStatus("PENDIENTE");
        budget.setFkIdPerson(client);
        budget = budgetRepository.save(budget);
        budgetRepository.flush();

        DeedManagement saved = managementRepository.save(testManagement);

        ProcedureType procedureType = new ProcedureType();
        procedureType.setName("Tipo Tramite Test");
        procedureTypeRepository.save(procedureType);

        Procedure procedure = new Procedure();
        procedure.setFkIdBudget(budget);
        procedure.setFkIdManagement(saved);
        procedure.setFkIdProcedureType(procedureType);
        procedureRepository.save(procedure);

        List<DeedManagement> found = managementRepository.findByClientPersonId(client.getPersonId());

        assertThat(found).isNotEmpty()
                .anyMatch(g -> g.getIdManagement().equals(saved.getIdManagement()));
    }
}
