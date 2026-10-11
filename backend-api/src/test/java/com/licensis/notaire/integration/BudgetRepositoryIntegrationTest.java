package com.licensis.notaire.integration;


import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Budget Repository Integration Tests")
class BudgetRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private Person person;

    @BeforeEach
    void setUp() {
        IdentificationType typeId = new IdentificationType();
        typeId.setName("DNI");
        typeId.setCharacters("8");
        typeId = identificationTypeRepository.save(typeId);

        person = new Person();
        person.setFirstName("Cliente");
        person.setLastName("Test");
        person.setIdentificationNumber("99999999");
        person.setIsClient(true);
        person.setFkIdIdentificationType(typeId);
        person = personRepository.save(person);
    }

    @Test
    @DisplayName("Should create budget")
    void shouldCreateBudget() {
        Budget budget = new Budget();
        budget.setNumber(1001);
        budget.setDate(new Date());
        budget.setHeading("Budget Test");
        budget.setStatus("BORRADOR");
        budget.setPropertyAmount(new java.math.BigDecimal("100000.0"));
        budget.setFkIdPerson(person);

        Budget saved = budgetRepository.save(budget);

        assertThat(saved.getIdBudget()).isNotNull();
        assertThat(saved.getNumber()).isEqualTo(1001);
        assertThat(saved.getStatus()).isEqualTo("BORRADOR");
    }

    @Test
    @DisplayName("Should retrieve budget by ID")
    void shouldRetrieveBudgetById() {
        Budget budget = new Budget();
        budget.setNumber(2001);
        budget.setDate(new Date());
        budget.setHeading("Budget Recuperación");
        budget.setStatus("PENDIENTE");
        budget.setFkIdPerson(person);
        Budget saved = budgetRepository.save(budget);

        Optional<Budget> found = budgetRepository.findById(saved.getIdBudget());

        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo(2001);
        assertThat(found.get().getStatus()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Should update budget state")
    void shouldUpdateBudgetState() {
        Budget budget = new Budget();
        budget.setNumber(3001);
        budget.setDate(new Date());
        budget.setHeading("Budget Actualizable");
        budget.setStatus("BORRADOR");
        budget.setFkIdPerson(person);
        Budget saved = budgetRepository.save(budget);

        saved.setStatus("APROBADO");
        saved.setNotes("Aprobado by the client");
        Budget updated = budgetRepository.save(saved);

        assertThat(updated.getStatus()).isEqualTo("APROBADO");
        assertThat(updated.getNotes()).isEqualTo("Aprobado by the client");
    }

    @Test
    @DisplayName("Should support multiple budgets")
    void shouldSupportMultiplePresupuestos() {
        String headingUnico = "Pres" + System.nanoTime();
        String[] estados = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "CANCELADO"};
        for (int i = 0; i < estados.length; i++) {
            Budget budget = new Budget();
            budget.setNumber(4000 + i);
            budget.setDate(new Date());
            budget.setHeading(headingUnico + " " + estados[i]);
            budget.setStatus(estados[i]);
            budget.setFkIdPerson(person);
            budgetRepository.save(budget);
        }

        List<Budget> creados = budgetRepository.findAll().stream()
                .filter(p -> p.getHeading() != null && p.getHeading().startsWith(headingUnico))
                .toList();
        assertThat(creados).hasSize(5);

        List<Budget> aprobados = creados.stream()
                .filter(p -> "APROBADO".equals(p.getStatus()))
                .toList();
        assertThat(aprobados).hasSize(1);
    }
}
