package com.licensis.notaire.adapter.out.persistence.procedure;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.PropertyRepository;

/**
 * Persistence/ops adapter for procedures. Holds entity/repository access previously on
 * {@code ProcedureController} (issues #1801 / #1906 — Spring Data PropertyRepository locally).
 */
@Component
public class ProcedureOpsAdapter implements ProcedureOpsPort {

    private static final Logger log = LoggerFactory.getLogger(ProcedureOpsAdapter.class);

    private final ProcedureRepository repository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final PropertyRepository propertyRepository;
    private final BudgetRepository budgetRepository;
    private final DeedRepository deedRepository;
    private final DeedManagementRepository deedManagementRepository;

    public ProcedureOpsAdapter(
            ProcedureRepository repository,
            ProcedureTypeRepository procedureTypeRepository,
            PropertyRepository propertyRepository,
            BudgetRepository budgetRepository,
            DeedRepository deedRepository,
            DeedManagementRepository deedManagementRepository) {
        this.repository = repository;
        this.procedureTypeRepository = procedureTypeRepository;
        this.propertyRepository = propertyRepository;
        this.budgetRepository = budgetRepository;
        this.deedRepository = deedRepository;
        this.deedManagementRepository = deedManagementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        var order = parseSort(sort);
        var pageable = PageRequest.of(page, size, order);
        var result = repository.findAll(pageable).map(this::toView);
        return new PageResult(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "idProcedure");
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcedureView> findById(Integer id) {
        return repository.findById(id).map(this::toView);
    }

    @Override
    public SaveResult create(ProcedureCommand command) {
        // No outer @Transactional: catching failures into ServerError must not mark the
        // Spring TX rollback-only (UnexpectedRollbackException → 500 instead of mapped status).
        try {
            Procedure entity = hydrate(new Procedure(), command);
            entity = repository.save(entity);
            return new SaveResult.Created(toView(entity));
        } catch (NoSuchElementException e) {
            return new SaveResult.NotFound();
        } catch (Exception e) {
            log.error("Failed to create procedure", e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public SaveResult update(Integer id, ProcedureCommand command) {
        Procedure existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return new SaveResult.NotFound();
        }
        try {
            Procedure entity = hydrate(existing, command);
            entity = repository.save(entity);
            return new SaveResult.Updated(toView(entity));
        } catch (NoSuchElementException e) {
            return new SaveResult.NotFound();
        } catch (Exception e) {
            log.error("Failed to update procedure id {}", id, e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        if (!repository.existsById(id)) {
            return new DeleteResult.NotFound();
        }
        try {
            repository.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            log.error("Failed to delete procedure id {}", id, e);
            return new DeleteResult.Conflict();
        }
    }

    /**
     * Resolves every association id present in {@code command} against its own repository
     * and applies it to {@code entity} (issue #981).
     *
     * @throws NoSuchElementException if a provided id does not resolve
     */
    private Procedure hydrate(Procedure entity, ProcedureCommand command) {
        entity.setNotes(command.notes());
        if (command.idProcedureType() != null) {
            ProcedureType procedureType = procedureTypeRepository.findById(command.idProcedureType())
                    .orElseThrow();
            entity.setFkIdProcedureType(procedureType);
        }
        if (command.idProperty() != null) {
            Property property = propertyRepository.findById(command.idProperty()).orElseThrow();
            entity.setFkIdProperty(property);
        }
        if (command.idDeed() != null) {
            Deed deed = deedRepository.findById(command.idDeed()).orElseThrow();
            entity.setFkIdDeed(deed);
        }
        if (command.idManagement() != null) {
            DeedManagement management = deedManagementRepository.findById(command.idManagement()).orElseThrow();
            entity.setFkIdManagement(management);
        }
        if (command.idBudget() != null) {
            Budget budget = budgetRepository.findById(command.idBudget()).orElseThrow();
            entity.setFkIdBudget(budget);
        }
        return entity;
    }

    private ProcedureView toView(Procedure procedure) {
        Budget budget = procedure.getFkIdBudget();
        ProcedureType type = procedure.getFkIdProcedureType();
        Property property = procedure.getFkIdProperty();
        Deed deed = procedure.getFkIdDeed();
        DeedManagement management = procedure.getFkIdManagement();
        return new ProcedureView(
                procedure.getIdProcedure(),
                procedure.getNotes(),
                type == null
                        ? null
                        : new ProcedureTypeView(
                                type.getIdProcedureType(),
                                type.getName(),
                                type.getNotes(),
                                type.getIsArchived(),
                                type.getIsRegistered(),
                                type.getAssociatesProperties(),
                                type.getEnabled()),
                property == null
                        ? null
                        : new PropertyView(
                                property.getIdProperty(),
                                property.getCadastralDesignation(),
                                property.getAddress(),
                                property.getRegistrationNumber()),
                budget == null ? null : new BudgetView(budget.getIdBudget()),
                deed == null ? null : new DeedView(deed.getIdDeed(), deed.getNumber(), deed.getStatus()),
                management == null
                        ? null
                        : new ManagementView(
                                management.getIdManagement(), management.getNumber(), management.getHeading()),
                procedure.getVersion());
    }
}
