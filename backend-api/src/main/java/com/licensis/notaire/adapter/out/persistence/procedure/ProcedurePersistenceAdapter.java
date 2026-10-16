package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureLookupPort;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Component;

/**
 * Thin lookup adapter — boolean existence only (issue #1726 / #1867).
 */
@Component
public class ProcedurePersistenceAdapter implements ProcedureLookupPort {

    private final ProcedureRepository procedureRepository;

    public ProcedurePersistenceAdapter(ProcedureRepository procedureRepository) {
        this.procedureRepository = procedureRepository;
    }

    @Override
    public boolean existsById(Integer id) {
        return procedureRepository.existsById(id);
    }
}
