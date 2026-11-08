package com.licensis.notaire.adapter.out.persistence.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.repository.ProcedureTemplateRepository;

/**
 * Persistence/ops adapter for procedure templates. Holds entity access previously on
 * {@code ProcedureTemplateController} (issue #1798).
 */
@Component
public class ProcedureTemplateOpsAdapter implements ProcedureTemplateOpsPort {

    private final ProcedureTemplateRepository repository;

    public ProcedureTemplateOpsAdapter(ProcedureTemplateRepository repository) {
        this.repository = repository;
    }

    private TemplateView toView(ProcedureTemplate entity) {
        ProcedureTemplatePK pk = entity.getProcedureTemplatePK();
        return new TemplateView(
                pk != null ? pk.getFkIdProcedureType() : null,
                pk != null ? pk.getFkIdDocumentType() : null,
                entity.getNotes(),
                entity.getVersion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateView> findAll() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateView> findByProcedureType(Integer procedureTypeId) {
        return repository.findByProcedureTypeIdProcedureType(procedureTypeId).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateView> findById(Integer procedureTypeId, Integer documentTypeId) {
        return repository.findById(new ProcedureTemplatePK(procedureTypeId, documentTypeId)).map(this::toView);
    }

    @Override
    public SaveResult create(Integer procedureTypeId, Integer documentTypeId, String notes) {
        if (procedureTypeId == null || documentTypeId == null) {
            return new SaveResult.BadRequest();
        }
        ProcedureTemplate entity = new ProcedureTemplate(new ProcedureTemplatePK(procedureTypeId, documentTypeId));
        entity.setNotes(notes);
        return new SaveResult.Created(toView(repository.save(entity)));
    }

    @Override
    public SaveResult update(Integer procedureTypeId, Integer documentTypeId, String notes) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(procedureTypeId, documentTypeId);
        return repository.findById(pk).<SaveResult>map(existing -> {
            existing.setNotes(notes);
            return new SaveResult.Updated(toView(repository.save(existing)));
        }).orElseGet(SaveResult.NotFound::new);
    }

    @Override
    @Transactional
    public DeleteResult delete(Integer procedureTypeId, Integer documentTypeId) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(procedureTypeId, documentTypeId);
        if (!repository.existsById(pk)) {
            return new DeleteResult.NotFound();
        }
        repository.deleteByPk(pk);
        return new DeleteResult.Deleted();
    }
}
