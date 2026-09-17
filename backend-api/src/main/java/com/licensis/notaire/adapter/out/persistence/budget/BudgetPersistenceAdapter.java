package com.licensis.notaire.adapter.out.persistence.budget;

import com.licensis.notaire.application.port.out.budget.BudgetRepositoryPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.BudgetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link BudgetRepositoryPort} on top the existing
 * Spring Data JPA BudgetRepository.
 *
 * <p>This is the only place where JPA Budget entity is accessed within the
 * budget slice: every value returned is a detached Budget instance, avoiding
 * lazy proxy leakage.
 */
@Component
public class BudgetPersistenceAdapter implements BudgetRepositoryPort {

    private final BudgetRepository budgetRepository;

    public BudgetPersistenceAdapter(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @Override
    public Page<Budget> findAllPaged(Pageable pageable) {
        return budgetRepository.findAll(pageable);
    }

    @Override
    public Optional<Budget> findById(Integer id) {
        return budgetRepository.findById(id);
    }

    @Override
    public List<Budget> findByPersonId(Integer idPerson) {
        return budgetRepository.findByFkIdPersonIdPerson(idPerson);
    }

    @Override
    public List<Budget> findByStatus(String status) {
        return budgetRepository.findByStatus(status);
    }

    @Override
    public boolean existsById(Integer id) {
        return budgetRepository.existsById(id);
    }

    @Override
    public void deleteById(Integer id) {
        budgetRepository.deleteById(id);
    }

    @Override
    public Budget create(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Budget update(Integer id, Budget budget) {
        budget.setIdBudget(id);
        return budgetRepository.save(budget);
    }
}
