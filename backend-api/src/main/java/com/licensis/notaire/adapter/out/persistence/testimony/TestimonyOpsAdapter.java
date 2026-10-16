package com.licensis.notaire.adapter.out.persistence.testimony;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.CopyRepository;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;

/**
 * Persistence/ops adapter for testimonies. Holds repository access previously on
 * {@code TestimonyController} (issue #1783).
 */
@Component
public class TestimonyOpsAdapter implements TestimonyOpsPort {

    private final TestimonyRepository repository;
    private final DeedRepository deedRepository;
    private final TestimonyMovementRepository movementRepository;
    private final CopyRepository copyRepository;
    private final TestimonyGenerationVerificationService generationVerificationService;

    public TestimonyOpsAdapter(
            TestimonyRepository repository,
            DeedRepository deedRepository,
            TestimonyMovementRepository movementRepository,
            CopyRepository copyRepository,
            TestimonyGenerationVerificationService generationVerificationService) {
        this.repository = repository;
        this.deedRepository = deedRepository;
        this.movementRepository = movementRepository;
        this.copyRepository = copyRepository;
        this.generationVerificationService = generationVerificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoTestimony> findAll() {
        return repository.findAll().stream().map(Testimony::getDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoTestimony> findById(Integer id) {
        return repository.findById(id).map(Testimony::getDto);
    }

    @Override
    public SaveResult create(DtoTestimony dto) {
        Integer idDeed = dto.getDeed() != null ? dto.getDeed().getIdDeed() : null;
        if (idDeed == null) {
            throw new BusinessValidationException("deed.idDeed: is required");
        }
        if (!deedRepository.existsById(idDeed)) {
            throw new ResourceNotFoundException("Deed not found: " + idDeed);
        }
        try {
            Testimony entity = new Testimony();
            entity.setAttributes(dto);
            entity = repository.save(entity);
            return new SaveResult.Created(entity.getDto());
        } catch (Exception e) {
            return new SaveResult.Failed(e);
        }
    }

    @Override
    public SaveResult update(Integer id, DtoTestimony dto) {
        Optional<Testimony> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return new SaveResult.NotFound();
        }
        Testimony entity = existing.get();
        if (entity.getVersion() != dto.getVersion()) {
            return new SaveResult.StaleVersion();
        }
        try {
            dto.setIdTestimony(id);
            entity.setAttributes(dto);
            repository.save(entity);
            return new SaveResult.Updated();
        } catch (Exception e) {
            return new SaveResult.Failed(e);
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        if (!repository.existsById(id)) {
            return new DeleteResult.NotFound();
        }
        if (movementRepository.existsByFkIdTestimonyIdTestimony(id)
                || copyRepository.existsByFkIdTestimonyIdTestimony(id)) {
            return new DeleteResult.InUse(IN_USE_MESSAGE);
        }
        try {
            repository.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            return new DeleteResult.Conflict("Cannot delete: the testimony is referenced by other records.");
        }
    }

    @Override
    @Transactional
    public DtoTestimony generate(Integer idDeed) {
        return generationVerificationService.generate(idDeed).getDto();
    }

    @Override
    @Transactional
    public DtoTestimony verify(Integer id, boolean flagged, String notes) {
        return generationVerificationService.verify(id, flagged, notes).getDto();
    }
}
