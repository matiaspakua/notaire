package com.licensis.notaire.adapter.out.persistence.testimony;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort;
import com.licensis.notaire.application.usecase.testimony.TestimonyReentry;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.repository.TestimonyMovementRepository;

/**
 * Persistence/ops adapter for testimony movements. Holds repository access previously on
 * {@code TestimonyMovementController} and delegates the CU11/CU12/CU44 circuit to
 * {@link TestimonyMovementService} (issue #1787).
 */
@Component
public class TestimonyMovementOpsAdapter implements TestimonyMovementOpsPort {

    private static final String DELETE_CONFLICT =
            "Cannot delete: the testimony movement is referenced by other records.";

    private final TestimonyMovementRepository repository;
    private final TestimonyMovementService testimonyMovementService;

    public TestimonyMovementOpsAdapter(
            TestimonyMovementRepository repository, TestimonyMovementService testimonyMovementService) {
        this.repository = repository;
        this.testimonyMovementService = testimonyMovementService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoTestimonyMovement> findAll() {
        return repository.findAll().stream().map(TestimonyMovementLegacyMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoTestimonyMovement> findById(Integer id) {
        return repository.findById(id).map(TestimonyMovementLegacyMapper::toDto);
    }

    @Override
    public SaveResult create(DtoTestimonyMovement dto) {
        // No outer @Transactional: catching constraint failures into SaveResult.Failed must not
        // mark the Spring TX rollback-only (UnexpectedRollbackException → 500 instead of 400).
        try {
            TestimonyMovement entity = new TestimonyMovement();
            TestimonyMovementLegacyMapper.apply(entity, dto);
            entity = repository.save(entity);
            return new SaveResult.Created(TestimonyMovementLegacyMapper.toDto(entity));
        } catch (Exception e) {
            return new SaveResult.Failed(e);
        }
    }

    @Override
    public SaveResult update(Integer id, DtoTestimonyMovement dto) {
        Optional<TestimonyMovement> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return new SaveResult.NotFound();
        }
        TestimonyMovement entity = existing.get();
        if (entity.getVersion() != dto.getVersion()) {
            return new SaveResult.StaleVersion();
        }
        try {
            dto.setIdTestimonyMovement(id);
        TestimonyMovementLegacyMapper.apply(entity, dto);
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
        try {
            repository.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            return new DeleteResult.Conflict(DELETE_CONFLICT);
        }
    }

    @Override
    public DtoTestimonyMovement registerEntry(Integer idTestimony) {
        return TestimonyMovementLegacyMapper.toDto(testimonyMovementService.registerEntry(idTestimony));
    }

    @Override
    public DtoTestimonyMovement registerInscription(Integer idTestimony) {
        return TestimonyMovementLegacyMapper.toDto(testimonyMovementService.registerInscription(idTestimony));
    }

    @Override
    public DtoTestimonyMovement withdraw(Integer idTestimony, int cardNumber) {
        return TestimonyMovementLegacyMapper.toDto(testimonyMovementService.withdraw(idTestimony, cardNumber));
    }

    @Override
    public DtoTestimonyMovement reenter(Integer idTestimony, TestimonyReentry reentry) {
        return TestimonyMovementLegacyMapper.toDto(testimonyMovementService.reenter(idTestimony, reentry));
    }
}
