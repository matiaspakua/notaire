package com.licensis.notaire.application.port.out.history;

import com.licensis.notaire.business.History;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for history persistence operations.
 */
public interface HistoryRepositoryPort {

    List<History> findAll();

    Optional<History> findById(Integer id);

    History save(History entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
