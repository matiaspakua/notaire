package com.licensis.notaire.application.port.in.concept;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.InUseResult;
import com.licensis.notaire.dto.DtoConcept;

/**
 * Read-side inbound port for concepts (issue #1815).
 */
public interface QueryConceptsUseCase {

    List<DtoConcept> findAll();

    List<DtoConcept> searchByName(String name);

    Optional<DtoConcept> findById(Integer id);

    InUseResult isInUse(Integer id);
}
