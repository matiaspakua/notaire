package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateRepositoryPort;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the procedure template slice.
 */
@Component
public class ProcedureTemplatePersistenceAdapter implements ProcedureTemplateRepositoryPort {

    private final ProcedureTemplateRepository procedureTemplateRepository;

    public ProcedureTemplatePersistenceAdapter(ProcedureTemplateRepository procedureTemplateRepository) {
        this.procedureTemplateRepository = procedureTemplateRepository;
    }

    @Override
    public List<ProcedureTemplate> findAll() {
        return procedureTemplateRepository.findAll();
    }

    @Override
    public Optional<ProcedureTemplate> findById(ProcedureTemplatePK id) {
        return procedureTemplateRepository.findById(id);
    }

    @Override
    public ProcedureTemplate save(ProcedureTemplate entity) {
        return procedureTemplateRepository.save(entity);
    }

    @Override
    public void deleteById(ProcedureTemplatePK id) {
        procedureTemplateRepository.deleteById(id);
    }

    @Override
    public boolean existsById(ProcedureTemplatePK id) {
        return procedureTemplateRepository.existsById(id);
    }

    @Override
    public List<ProcedureTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType) {
        return procedureTemplateRepository.findByProcedureTypeIdProcedureType(idProcedureType);
    }
}
