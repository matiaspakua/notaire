package com.licensis.notaire.adapter.out.persistence.budget;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.BudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound budget persistence helper (issue #1865). Speaks JPA entities only inside the
 * adapter layer.
 */
@Service
@Transactional
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        log.debug("Finding all budgets");
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Budget> findAllPaged(Pageable pageable) {
        return budgetRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Budget> findById(Integer id) {
        log.debug("Finding budget by id: {}", id);
        return budgetRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Budget> findByPerson(Integer idPerson) {
        log.debug("Finding budgets for person id: {}", idPerson);
        return budgetRepository.findByFkIdPersonIdPerson(idPerson);
    }

    @Transactional(readOnly = true)
    public List<Budget> findByStatus(String status) {
        if (status == null || status.isBlank()) {
            log.debug("Returning all budgets (no status filter)");
            return budgetRepository.findAll();
        }
        log.debug("Finding budgets by status: {}", status);
        return budgetRepository.findByStatus(status);
    }

    public Budget create(Budget budget) {
        if (budget.getHeading() == null || budget.getHeading().isBlank()) {
            budget.setHeading("Budget");
        }
        if (budget.getNumber() == 0) {
            budget.setNumber((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        }
        log.info("Creating budget: number={}", budget.getNumber());
        return budgetRepository.save(budget);
    }

    public Budget update(Integer id, Budget budget) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found with ID: " + id);
        }
        budget.setIdBudget(id);
        log.info("Updating budget id: {}", id);
        return budgetRepository.save(budget);
    }

    public void deleteById(Integer id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found with ID: " + id);
        }
        log.info("Deleting budget id: {}", id);
        budgetRepository.deleteById(id);
    }
}
