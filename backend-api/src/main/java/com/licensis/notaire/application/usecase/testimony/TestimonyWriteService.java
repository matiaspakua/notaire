package com.licensis.notaire.application.usecase.testimony;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.testimony.ManageTestimoniesUseCase;
import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort;
import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoTestimony;

/**
 * Write-side use case for testimonies — delegates to {@link TestimonyOpsPort} (issue #1783).
 */
@Service
public class TestimonyWriteService implements ManageTestimoniesUseCase {

    private final TestimonyOpsPort ops;

    public TestimonyWriteService(TestimonyOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(DtoTestimony dto) {
        return ops.create(dto);
    }

    @Override
    public SaveResult update(Integer id, DtoTestimony dto) {
        return ops.update(id, dto);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }

    @Override
    public DtoTestimony generate(Integer idDeed) {
        return ops.generate(idDeed);
    }

    @Override
    public DtoTestimony verify(Integer id, boolean flagged, String notes) {
        return ops.verify(id, flagged, notes);
    }
}
