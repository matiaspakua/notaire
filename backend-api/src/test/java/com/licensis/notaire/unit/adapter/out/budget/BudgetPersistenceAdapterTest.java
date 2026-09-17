package com.licensis.notaire.unit.adapter.out.budget;

import com.licensis.notaire.adapter.out.persistence.budget.BudgetPersistenceAdapter;
import com.licensis.notaire.application.port.out.budget.BudgetRepositoryPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Budget Persistence Adapter")
class BudgetPersistenceAdapterTest {

    @Mock
    private BudgetRepository repository;

    private BudgetRepositoryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new BudgetPersistenceAdapter(repository);
    }

    @Test
    @DisplayName("Should find all budgets")
    void shouldFindAll() {
        Budget budget1 = new Budget();
        budget1.setIdBudget(1);
        Budget budget2 = new Budget();
        budget2.setIdBudget(2);

        when(repository.findAll()).thenReturn(List.of(budget1, budget2));

        List<Budget> result = adapter.findAll();

        assertThat(result).hasSize(2).containsExactly(budget1, budget2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should find budget by ID")
    void shouldFindById() {
        Budget budget = new Budget();
        budget.setIdBudget(1);

        when(repository.findById(1)).thenReturn(Optional.of(budget));

        Optional<Budget> result = adapter.findById(1);

        assertThat(result).isPresent().contains(budget);
        verify(repository).findById(1);
    }

    @Test
    @DisplayName("Should return empty when budget not found")
    void shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        Optional<Budget> result = adapter.findById(999);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find budgets by person ID")
    void shouldFindByPersonId() {
        Budget budget = new Budget();
        budget.setIdBudget(1);

        when(repository.findByFkIdPersonIdPerson(1)).thenReturn(List.of(budget));

        List<Budget> result = adapter.findByPersonId(1);

        assertThat(result).hasSize(1).contains(budget);
        verify(repository).findByFkIdPersonIdPerson(1);
    }

    @Test
    @DisplayName("Should check if budget exists")
    void shouldCheckExists() {
        when(repository.existsById(1)).thenReturn(true);

        boolean exists = adapter.existsById(1);

        assertThat(exists).isTrue();
        verify(repository).existsById(1);
    }

    @Test
    @DisplayName("Should create budget")
    void shouldCreate() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        Budget savedBudget = new Budget();
        savedBudget.setIdBudget(1);

        when(repository.save(budget)).thenReturn(savedBudget);

        Budget result = adapter.create(budget);

        assertThat(result).isNotNull().isEqualTo(savedBudget);
        verify(repository).save(budget);
    }

    @Test
    @DisplayName("Should update budget")
    void shouldUpdate() {
        Budget budget = new Budget();
        Budget updated = new Budget();
        updated.setIdBudget(1);

        when(repository.save(budget)).thenReturn(updated);

        Budget result = adapter.update(1, budget);

        assertThat(result.getIdBudget()).isEqualTo(1);
        verify(repository).save(budget);
    }

    @Test
    @DisplayName("Should delete budget by ID")
    void shouldDeleteById() {
        adapter.deleteById(1);

        verify(repository).deleteById(1);
    }

    @Test
    @DisplayName("Should find budgets paginated")
    void shouldFindAllPaged() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        Page<Budget> page = new PageImpl<>(List.of(budget));
        PageRequest pageable = PageRequest.of(0, 20);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<Budget> result = adapter.findAllPaged(pageable);

        assertThat(result).hasSize(1);
        verify(repository).findAll(pageable);
    }
}
