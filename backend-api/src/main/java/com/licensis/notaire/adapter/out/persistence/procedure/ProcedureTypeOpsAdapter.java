package com.licensis.notaire.adapter.out.persistence.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.procedure.ProcedureRepositoryPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateRepositoryPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeRepositoryPort;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;

/**
 * Persistence/ops adapter for procedure types. Holds repository/entity access previously on
 * {@code ProcedureTypeController} (issue #1791).
 */
@Component
public class ProcedureTypeOpsAdapter implements ProcedureTypeOpsPort {

    private final ProcedureTypeRepositoryPort repository;
    private final BudgetTemplateRepository budgetTemplateRepository;
    private final ProcedureRepositoryPort procedureRepository;
    private final ProcedureTemplateRepositoryPort procedureTemplateRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    public ProcedureTypeOpsAdapter(
            ProcedureTypeRepositoryPort repository,
            BudgetTemplateRepository budgetTemplateRepository,
            ProcedureRepositoryPort procedureRepository,
            ProcedureTemplateRepositoryPort procedureTemplateRepository,
            WorkflowDefinitionRepository workflowDefinitionRepository) {
        this.repository = repository;
        this.budgetTemplateRepository = budgetTemplateRepository;
        this.procedureRepository = procedureRepository;
        this.procedureTemplateRepository = procedureTemplateRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
    }

    private boolean inUse(Integer id) {
        return !procedureTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !budgetTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !procedureRepository.findByFkIdProcedureTypeIdProcedureType(id).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProcedureType> findAll() {
        return repository.findAll().stream().map(ProcedureType::getDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProcedureType> searchByName(String name) {
        return repository.findByNameContaining(name).stream().map(ProcedureType::getDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoProcedureType> findById(Integer id) {
        return repository.findById(id).map(ProcedureType::getDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Boolean> isInUse(Integer id) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        return Optional.of(inUse(id));
    }

    @Override
    public SaveResult create(DtoProcedureType dto) {
        // No outer @Transactional: catching failures into SaveResult.Failed must not mark the
        // Spring TX rollback-only (UnexpectedRollbackException → 500 instead of 400).
        try {
            dto.setEnabled(true);
            if (dto.getVersion() == null) {
                dto.setVersion(0);
            }
            if (dto.getAssociatesProperties() == null) {
                dto.setAssociatesProperties(false);
            }
            ProcedureType entity = new ProcedureType();
            entity.setAttributes(dto);
            entity = repository.save(entity);
            return new SaveResult.Created(entity.getDto());
        } catch (Exception e) {
            return new SaveResult.Failed(e);
        }
    }

    @Override
    public SaveResult update(Integer id, DtoProcedureType dto) {
        Optional<ProcedureType> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return new SaveResult.NotFound();
        }
        if (inUse(id)) {
            return new SaveResult.InUse(IN_USE_MODIFY_MESSAGE);
        }
        try {
            dto.setIdProcedureType(id);
            ProcedureType entity = existing.get();
            entity.setAttributes(dto);
            repository.save(entity);
            return new SaveResult.Updated();
        } catch (Exception e) {
            return new SaveResult.Failed(e);
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        if (!repository.existsById(id)) {
            return new DeleteResult.NotFound();
        }
        if (inUse(id)) {
            return new DeleteResult.InUse(IN_USE_DELETE_MESSAGE);
        }
        repository.deleteById(id);
        return new DeleteResult.Deleted();
    }

    @Override
    public WorkflowAssignResult assignWorkflow(Integer id, Integer workflowDefinitionId) {
        Optional<ProcedureType> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return new WorkflowAssignResult.NotFound();
        }
        ProcedureType type = existing.get();
        if (workflowDefinitionId == null) {
            type.setWorkflowDefinition(null);
            repository.save(type);
            return new WorkflowAssignResult.Ok(type.getDto());
        }
        Optional<WorkflowDefinition> wf = workflowDefinitionRepository.findById(workflowDefinitionId);
        if (wf.isEmpty()) {
            return new WorkflowAssignResult.NotFound();
        }
        if (!wf.get().isActive()) {
            return new WorkflowAssignResult.Inactive();
        }
        type.setWorkflowDefinition(wf.get());
        repository.save(type);
        return new WorkflowAssignResult.Ok(type.getDto());
    }
}
