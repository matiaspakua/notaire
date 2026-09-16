package com.licensis.notaire.adapter.out.persistence.deed;

import com.licensis.notaire.application.port.out.deed.DeedNumberingPort;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter for deed numbering outbound port.
 * Implements DeedNumberingPort by delegating to FolioRepository.
 */
@Repository
public class FolioRepositoryAdapter implements DeedNumberingPort {

    private final FolioRepository folioRepository;

    public FolioRepositoryAdapter(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    @Override
    public Optional<Integer> findMaxNumberDeedByNotaryYearAndType(
            Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId) {
        return folioRepository.findMaxNumberDeedByNotaryYearAndType(
                notaryId, year, auxiliary, excludedDeedId);
    }

    @Override
    public boolean existsNumberDeedByNotaryYearAndType(
            int number, Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId) {
        return folioRepository.existsNumberDeedByNotaryYearAndType(
                number, notaryId, year, auxiliary, excludedDeedId);
    }
}
