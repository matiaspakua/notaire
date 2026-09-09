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

@DisplayName("Presupuesto Repository Integration Tests")
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
        budget.setEncabezado("Presupuesto Test");
        budget.setStatus("BORRADOR");
        budget.setPropertyAmount(100000.0f);
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
        budget.setEncabezado("Presupuesto Recuperación");
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
        budget.setEncabezado("Presupuesto Actualizable");
        budget.setStatus("BORRADOR");
        budget.setFkIdPerson(person);
        Budget saved = budgetRepository.save(budget);

        saved.setStatus("APROBADO");
        saved.setNotes("Aprobado por el cliente");
        Budget updated = budgetRepository.save(saved);

        assertThat(updated.getStatus()).isEqualTo("APROBADO");
        assertThat(updated.getNotes()).isEqualTo("Aprobado por el cliente");
    }

    @Test
    @DisplayName("Should support multiple presupuestos")
    void shouldSupportMultiplePresupuestos() {
        String encabezadoUnico = "Pres" + System.nanoTime();
        String[] estados = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "CANCELADO"};
        for (int i = 0; i < estados.length; i++) {
            Budget budget = new Budget();
            budget.setNumber(4000 + i);
            budget.setDate(new Date());
            budget.setEncabezado(encabezadoUnico + " " + estados[i]);
            budget.setStatus(estados[i]);
            budget.setFkIdPerson(person);
            budgetRepository.save(budget);
        }

        List<Budget> creados = budgetRepository.findAll().stream()
                .filter(p -> p.getEncabezado() != null && p.getEncabezado().startsWith(encabezadoUnico))
                .toList();
        assertThat(creados).hasSize(5);

        List<Budget> aprobados = creados.stream()
                .filter(p -> "APROBADO".equals(p.getStatus()))
                .toList();
        assertThat(aprobados).hasSize(1);
    }
}
