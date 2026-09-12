package com.licensis.notaire.service;

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
 * Service layer for Presupuesto domain operations.
 * Replaces the legacy PresupuestoJpaController access pattern.
 */
@Service
@Transactional
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Returns all presupuestos.
     *
     * @return list of all presupuestos
     */
    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        log.debug("Finding all presupuestos");
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Budget> findAllPaged(Pageable pageable) {
        return budgetRepository.findAll(pageable);
    }

    /**
     * Finds a presupuesto by its primary key.
     *
     * @param id the presupuesto ID
     * @return optional containing the presupuesto, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Budget> findById(Integer id) {
        log.debug("Finding presupuesto by id: {}", id);
        return budgetRepository.findById(id);
    }

    /**
     * Returns all presupuestos belonging to a given persona (CU60).
     *
     * @param idPersona the persona ID
     * @return list of presupuestos for the persona
     */
    @Transactional(readOnly = true)
    public List<Budget> findByPerson(Integer idPerson) {
        log.debug("Finding presupuestos for persona id: {}", idPerson);
        return budgetRepository.findByFkIdPersonIdPerson(idPerson);
    }

    /**
     * Filters presupuestos by estado (CU60).
     * If estado is blank or null, all presupuestos are returned.
     *
     * @param estado the estado value to filter by (may be null or blank)
     * @return matching presupuestos
     */
    @Transactional(readOnly = true)
    public List<Budget> findByStatus(String status) {
        if (status == null || status.isBlank()) {
            log.debug("Returning all presupuestos (no estado filter)");
            return budgetRepository.findAll();
        }
        log.debug("Finding presupuestos by estado: {}", status);
        return budgetRepository.findByStatus(status);
    }

    /**
     * Persists a new presupuesto. Applies default values for required fields when absent.
     *
     * @param presupuesto the entity to persist
     * @return the saved presupuesto with generated ID
     */
    public Budget create(Budget budget) {
        if (budget.getEncabezado() == null || budget.getEncabezado().isBlank()) {
            budget.setEncabezado("Presupuesto");
        }
        if (budget.getNumber() == 0) {
            budget.setNumber((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        }
        log.info("Creating presupuesto: numero={}", budget.getNumber());
        return budgetRepository.save(budget);
    }

    /**
     * Updates an existing presupuesto.
     *
     * @param id          the presupuesto ID
     * @param presupuesto the updated state
     * @return the saved presupuesto
     * @throws ResourceNotFoundException if no presupuesto with the given ID exists
     */
    public Budget update(Integer id, Budget budget) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id);
        }
        budget.setIdBudget(id);
        log.info("Updating presupuesto id: {}", id);
        return budgetRepository.save(budget);
    }

    /**
     * Deletes a presupuesto by ID.
     *
     * @param id the presupuesto ID
     * @throws ResourceNotFoundException if no presupuesto with the given ID exists
     */
    public void deleteById(Integer id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id);
        }
        log.info("Deleting presupuesto id: {}", id);
        budgetRepository.deleteById(id);
    }
}
