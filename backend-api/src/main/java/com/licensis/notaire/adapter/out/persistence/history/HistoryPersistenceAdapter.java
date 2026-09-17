package com.licensis.notaire.adapter.out.persistence.history;

import com.licensis.notaire.application.port.out.history.HistoryRepositoryPort;
import com.licensis.notaire.business.History;
import com.licensis.notaire.repository.HistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the history slice.
 */
@Component
public class HistoryPersistenceAdapter implements HistoryRepositoryPort {

    private final HistoryRepository historyRepository;

    public HistoryPersistenceAdapter(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<History> findAll() {
        return historyRepository.findAll();
    }

    @Override
    public Optional<History> findById(Integer id) {
        return historyRepository.findById(id);
    }

    @Override
    public History save(History entity) {
        return historyRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        historyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return historyRepository.existsById(id);
    }
}
