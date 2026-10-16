package com.licensis.notaire.application.usecase.testimony;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.testimony.QueryTestimoniesUseCase;
import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort;
import com.licensis.notaire.dto.DtoTestimony;

/**
 * Read-side use case for testimonies — delegates to {@link TestimonyOpsPort} (issue #1783).
 */
@Service
public class TestimonyQueryUseCaseService implements QueryTestimoniesUseCase {

    private final TestimonyOpsPort ops;

    public TestimonyQueryUseCaseService(TestimonyOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoTestimony> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoTestimony> findById(Integer id) {
        return ops.findById(id);
    }
}
