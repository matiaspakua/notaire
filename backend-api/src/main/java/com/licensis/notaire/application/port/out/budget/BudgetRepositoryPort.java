package com.licensis.notaire.application.port.out.budget;

import com.licensis.notaire.business.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port describing budget persistence capabilities application needs.
 *
 * <p>Exposes only operations use cases actually exercise, speaks plain
 * {@link Budget} DTOs rather JPA-managed entities directly to avoid
 * lazy proxy leakage across transaction boundaries.
 */
public interface BudgetRepositoryPort {

    List<Budget> findAll();

    Page<Budget> findAllPaged(Pageable pageable);

    Optional<Budget> findById(Integer id);

    List<Budget> findByPersonId(Integer idPerson);

    List<Budget> findByStatus(String status);

    boolean existsById(Integer id);

    void deleteById(Integer id);

    Budget create(Budget budget);

    Budget update(Integer id, Budget budget);
}
