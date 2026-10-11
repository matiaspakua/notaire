package com.licensis.notaire.application.usecase.budget;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.application.port.out.budget.BudgetRepositoryPort;
import com.licensis.notaire.business.Budget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for Budget domain operations.
 * Replaces the legacy PresupuestoJpaController access pattern.
 */
@Service
@Transactional
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepositoryPort budgetRepository;

    public BudgetService(BudgetRepositoryPort budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Returns all budgets.
     *
     * @return list of all budgets
     */
    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        log.debug("Finding all budgets");
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Budget> findAllPaged(Pageable pageable) {
        return budgetRepository.findAllPaged(pageable);
    }

    /**
     * Finds a budget by its primary key.
     *
     * @param id the budget ID
     * @return optional containing the budget, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Budget> findById(Integer id) {
        log.debug("Finding budget by id: {}", id);
        return budgetRepository.findById(id);
    }

    /**
     * Returns all budgets belonging to a given person (CU60).
     *
     * @param idPersona the persona ID
     * @return list of budgets for the person
     */
    @Transactional(readOnly = true)
    public List<Budget> findByPerson(Integer idPerson) {
        log.debug("Finding budgets for person id: {}", idPerson);
        return budgetRepository.findByPersonId(idPerson);
    }

    /**
     * Filters budgets by status (CU60).
     * If status is blank or null, all budgets are returned.
     *
     * @param estado the estado value to filter by (may be null or blank)
     * @return matching budgets
     */
    @Transactional(readOnly = true)
    public List<Budget> findByStatus(String status) {
        if (status == null || status.isBlank()) {
            log.debug("Returning all budgets (not status filter)");
            return budgetRepository.findAll();
        }
        log.debug("Finding budgets by status: {}", status);
        return budgetRepository.findByStatus(status);
    }

    /**
     * Persists a new budget. Applies default values for required fields when absent.
     *
     * @param budget the entity to persist
     * @return the saved budget with generated ID
     */
    public Budget create(Budget budget) {
        if (budget.getHeading() == null || budget.getHeading().isBlank()) {
            budget.setHeading("Budget");
        }
        if (budget.getNumber() == 0) {
            budget.setNumber((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        }
        log.info("Creating budget: number={}", budget.getNumber());
        return budgetRepository.create(budget);
    }

    /**
     * Updates an existing budget.
     *
     * @param id the budget ID
     * @param budget the updated state
     * @return the saved budget
     * @throws ResourceNotFoundException if not budget with the given ID exists
     */
    public Budget update(Integer id, Budget budget) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found with ID: " + id);
        }
        budget.setIdBudget(id);
        log.info("Updating budget id: {}", id);
        return budgetRepository.update(id, budget);
    }

    /**
     * Deletes a budget by ID.
     *
     * @param id the budget ID
     * @throws ResourceNotFoundException if not budget with the given ID exists
     */
    public void deleteById(Integer id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found with ID: " + id);
        }
        log.info("Deleting budget id: {}", id);
        budgetRepository.deleteById(id);
    }
}
