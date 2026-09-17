package com.licensis.notaire.adapter.out.persistence.folio;

import com.licensis.notaire.application.port.out.folio.FolioRepositoryPort;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for folio slice.
 */
@Component
public class FolioPersistenceAdapter implements FolioRepositoryPort {

    private final FolioRepository folioRepository;
    private final FolioTypeRepository folioTypeRepository;

    public FolioPersistenceAdapter(FolioRepository folioRepository, FolioTypeRepository folioTypeRepository) {
        this.folioRepository = folioRepository;
        this.folioTypeRepository = folioTypeRepository;
    }

    @Override
    public Folio save(Folio entity) {
        return folioRepository.save(entity);
    }

    @Override
    public Optional<Folio> findById(Integer id) {
        return folioRepository.findById(id);
    }

    @Override
    public List<Folio> findAll() {
        return folioRepository.findAll();
    }

    @Override
    public List<Folio> findAvailableAuxiliaryFolios() {
        return folioRepository.findAvailableAuxiliaryFolios();
    }

    @Override
    public Optional<Integer> findMaxNumberDeedAuxiliary() {
        return folioRepository.findMaxNumberDeedAuxiliary();
    }

    @Override
    public List<Folio> findAllByIdIn(List<Integer> ids) {
        return folioRepository.findAllByIdFolioIn(ids);
    }

    @Override
    public Optional<FolioType> findFolioTypeById(Integer id) {
        return folioTypeRepository.findById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return folioRepository.existsById(id);
    }

    @Override
    public void deleteById(Integer id) {
        folioRepository.deleteById(id);
    }
}
