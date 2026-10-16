package com.licensis.notaire.application.port.in.concept;

import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoConcept;

/**
 * Write-side inbound port for concepts (issue #1815).
 */
public interface ManageConceptsUseCase {

    SaveResult create(DtoConcept dto);

    SaveResult update(Integer id, DtoConcept dto);

    DeleteResult delete(Integer id);
}
