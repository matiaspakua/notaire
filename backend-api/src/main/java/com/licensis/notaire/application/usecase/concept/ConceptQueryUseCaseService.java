package com.licensis.notaire.application.usecase.concept;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.concept.QueryConceptsUseCase;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.InUseResult;
import com.licensis.notaire.dto.DtoConcept;

/**
 * Read-side use case for concepts — delegates to {@link ConceptOpsPort} (issue #1815).
 */
@Service
public class ConceptQueryUseCaseService implements QueryConceptsUseCase {

    private final ConceptOpsPort ops;

    public ConceptQueryUseCaseService(ConceptOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoConcept> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoConcept> searchByName(String name) {
        return ops.searchByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoConcept> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public InUseResult isInUse(Integer id) {
        return ops.isInUse(id);
    }
}
