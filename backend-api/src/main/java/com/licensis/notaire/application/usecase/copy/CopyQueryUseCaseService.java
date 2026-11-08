package com.licensis.notaire.application.usecase.copy;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.copy.QueryCopiesUseCase;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyView;

/**
 * Read-side use case for copies — delegates to {@link CopyOpsPort} (issue #1779).
 */
@Service
public class CopyQueryUseCaseService implements QueryCopiesUseCase {

    private final CopyOpsPort ops;

    public CopyQueryUseCaseService(CopyOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopyView> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyView> findById(Integer id) {
        return ops.findById(id);
    }
}
