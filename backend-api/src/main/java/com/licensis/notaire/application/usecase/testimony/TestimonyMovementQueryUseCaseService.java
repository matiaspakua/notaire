package com.licensis.notaire.application.usecase.testimony;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.testimony.QueryTestimonyMovementsUseCase;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/**
 * Read-side use case for testimony movements — delegates to {@link TestimonyMovementOpsPort}
 * (issue #1787).
 */
@Service
public class TestimonyMovementQueryUseCaseService implements QueryTestimonyMovementsUseCase {

    private final TestimonyMovementOpsPort ops;

    public TestimonyMovementQueryUseCaseService(TestimonyMovementOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoTestimonyMovement> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoTestimonyMovement> findById(Integer id) {
        return ops.findById(id);
    }
}
