package com.licensis.notaire.application.usecase.concept;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.concept.ManageConceptsUseCase;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoConcept;

/**
 * Write-side use case for concepts — delegates to {@link ConceptOpsPort} (issue #1815).
 */
@Service
public class ConceptWriteService implements ManageConceptsUseCase {

    private final ConceptOpsPort ops;

    public ConceptWriteService(ConceptOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(DtoConcept dto) {
        return ops.create(dto);
    }

    @Override
    public SaveResult update(Integer id, DtoConcept dto) {
        return ops.update(id, dto);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
