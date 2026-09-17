package com.licensis.notaire.application.port.in.history;

import com.licensis.notaire.business.History;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for history operations.
 */
public interface HistoryUseCase {

    List<History> findAll();

    Optional<History> findById(Integer id);

    History save(History entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
