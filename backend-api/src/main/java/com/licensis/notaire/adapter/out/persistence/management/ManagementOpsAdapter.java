package com.licensis.notaire.adapter.out.persistence.management;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.management.ManagementOpsPort;
import com.licensis.notaire.adapter.out.persistence.history.HistoryMapper;
import com.licensis.notaire.adapter.out.persistence.procedure.ProcedureFolderService;
import com.licensis.notaire.adapter.out.persistence.procedure.RequiredDocumentationOnConfirmService;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.HistoryOrder;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoManagementCaseSummary;
import com.licensis.notaire.dto.DtoManagementFinancialSummary;
import com.licensis.notaire.dto.DtoManagementSummary;
import java.math.BigDecimal;
import com.licensis.notaire.repository.HistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.exception.FoldersInWaitException;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.PropertyRepository;

/**
 * Persistence/ops adapter for deed managements. Holds repository access previously on
 * {@code ManagementController} (issue #1772).
 */
@Component
public class ManagementOpsAdapter implements ManagementOpsPort {

    private static final Logger log = LoggerFactory.getLogger(ManagementOpsAdapter.class);

    private final DeedManagementRepository repository;
    private final HistoryRepository historyRepository;
    private final PersonRepository personRepository;
    private final ManagementStatusRepository statusRepository;
    private final BudgetRepository budgetRepository;
    private final ProcedureTypeRepository typeProcedureRepository;
    private final ProcedureRepository procedureRepository;
    private final PropertyRepository propertyRepository;
    private final ManagementQueryService managementQueryService;
    private final ManagementArchiveDebtService managementArchiveDebtService;
    private final ManagementCaseSummaryService managementCaseSummaryService;
    private final ManagementFinancialSummaryService managementFinancialSummaryService;
    private final ManagementSubstitutionService managementSubstitutionService;
    private final ManagementStatusLogService statusLogService;
    private final ManagementStatusWriteGuard managementStatusWriteGuard;
    private final ProcedureFolderService procedureFolderService;
    private final RequiredDocumentationOnConfirmService requiredDocumentationOnConfirmService;

    public ManagementOpsAdapter(
            DeedManagementRepository repository,
            HistoryRepository historyRepository,
            PersonRepository personRepository,
            ManagementStatusRepository statusRepository,
            BudgetRepository budgetRepository,
            ProcedureTypeRepository typeProcedureRepository,
            ProcedureRepository procedureRepository,
            PropertyRepository propertyRepository,
            ManagementQueryService managementQueryService,
            ManagementArchiveDebtService managementArchiveDebtService,
            ManagementCaseSummaryService managementCaseSummaryService,
            ManagementFinancialSummaryService managementFinancialSummaryService,
            ManagementSubstitutionService managementSubstitutionService,
            ManagementStatusLogService statusLogService,
            ManagementStatusWriteGuard managementStatusWriteGuard,
            ProcedureFolderService procedureFolderService,
            RequiredDocumentationOnConfirmService requiredDocumentationOnConfirmService) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.personRepository = personRepository;
        this.statusRepository = statusRepository;
        this.budgetRepository = budgetRepository;
        this.typeProcedureRepository = typeProcedureRepository;
        this.procedureRepository = procedureRepository;
        this.propertyRepository = propertyRepository;
        this.managementQueryService = managementQueryService;
        this.managementArchiveDebtService = managementArchiveDebtService;
        this.managementCaseSummaryService = managementCaseSummaryService;
        this.managementFinancialSummaryService = managementFinancialSummaryService;
        this.managementSubstitutionService = managementSubstitutionService;
        this.statusLogService = statusLogService;
        this.managementStatusWriteGuard = managementStatusWriteGuard;
        this.procedureFolderService = procedureFolderService;
        this.requiredDocumentationOnConfirmService = requiredDocumentationOnConfirmService;
    }

    private record CaseDependencies(Budget budget, Person notary, ManagementStatus status,
            ProcedureType typeProcedure, Property property) {}

    private static boolean hasRequiredFields(CompleteCaseCommand request) {
        return request.number() != null && request.budgetId() != null && request.notaryId() != null
                && request.statusManagementId() != null && request.typeProcedureId() != null;
    }

    private Optional<CaseDependencies> resolveDependencies(CompleteCaseCommand request) {
        Optional<Budget> budget = budgetRepository.findById(request.budgetId());
        Optional<Person> notary = personRepository.findById(request.notaryId());
        Optional<ManagementStatus> status = statusRepository.findById(request.statusManagementId());
        Optional<ProcedureType> typeProcedure = typeProcedureRepository.findById(request.typeProcedureId());
        if (budget.isEmpty() || notary.isEmpty() || status.isEmpty() || typeProcedure.isEmpty()) {
            return Optional.empty();
        }
        Property property = null;
        if (request.propertyId() != null) {
            Optional<Property> found = propertyRepository.findById(request.propertyId());
            if (found.isEmpty()) {
                return Optional.empty();
            }
            property = found.get();
        }
        return Optional.of(new CaseDependencies(budget.get(), notary.get(), status.get(),
                typeProcedure.get(), property));
    }

    private void applyManagementFields(DeedManagement management, CompleteCaseCommand request,
            CaseDependencies dependencies) {
        management.setNumber(request.number());
        management.setHeading(request.heading() == null ? "Management" : request.heading());
        ManagementSubstitutionService.AssignedNotary assigned =
                managementSubstitutionService.resolveNotary(dependencies.notary(), management.getDateStart());
        management.setFkIdNotaryPerson(assigned.notary());
        management.setNotes(buildNotes(request.notes(), dependencies.notary(), assigned));
        management.setFkIdManagementStatus(dependencies.status());
    }

    private String buildNotes(String requestNotes, Person requestedNotary,
            ManagementSubstitutionService.AssignedNotary assigned) {
        if (assigned.appliedSubstitution() == null) {
            return requestNotes;
        }
        String redirectionNote = managementSubstitutionService.redirectionNote(
                requestedNotary, assigned.notary());
        if (requestNotes == null || requestNotes.isBlank()) {
            return redirectionNote;
        }
        return requestNotes + " | " + redirectionNote;
    }

    private void saveProcedure(DeedManagement management, CaseDependencies dependencies) {
        Procedure procedure = new Procedure();
        procedure.setFkIdManagement(management);
        applyProcedureDependencies(procedure, dependencies);
        Procedure saved = procedureRepository.save(procedure);
        procedureFolderService.generateFolderForProcedure(saved);
        requiredDocumentationOnConfirmService.seedRequiredDocuments(saved);
    }

    private void updateProcedure(Procedure procedure, CaseDependencies dependencies) {
        applyProcedureDependencies(procedure, dependencies);
        procedureRepository.save(procedure);
    }

    private static void applyProcedureDependencies(Procedure procedure, CaseDependencies dependencies) {
        procedure.setFkIdBudget(dependencies.budget());
        procedure.setFkIdProcedureType(dependencies.typeProcedure());
        if (dependencies.property() != null) {
            procedure.setFkIdProperty(dependencies.property());
        }
    }

    private static Integer statusIdOf(DeedManagement management) {
        ManagementStatus status = management.getFkIdManagementStatus();
        return status != null ? status.getIdManagementStatus() : null;
    }

    private void registerStatusChangeIfNeeded(DeedManagement management, Integer previousStatusId) {
        Integer newStatusId = statusIdOf(management);
        if (newStatusId != null && !newStatusId.equals(previousStatusId)) {
            statusLogService.registerStatus(management, null);
        }
    }

    private boolean applyManagementRequest(DeedManagement entity, ManagementCommand request) {
        if (request.number() != null) {
            entity.setNumber(request.number());
        }
        entity.setHeading(request.heading());
        entity.setDateStart(request.dateStart() != null ? request.dateStart() : new Date());
        String notes = request.notes();
        if (request.pendingDebtAtArchiving() != null) {
            entity.setPendingDebtAtArchiving(request.pendingDebtAtArchiving());
        }
        if (request.notaryPersonId() != null) {
            Person requestedNotary = personRepository.findById(request.notaryPersonId()).orElse(null);
            if (requestedNotary == null) {
                return false;
            }
            ManagementSubstitutionService.AssignedNotary assigned =
                    managementSubstitutionService.resolveNotary(requestedNotary, entity.getDateStart());
            entity.setFkIdNotaryPerson(assigned.notary());
            notes = buildNotes(notes, requestedNotary, assigned);
        }
        entity.setNotes(notes);
        if (request.managementStatusId() != null) {
            ManagementStatus status = statusRepository.findById(request.managementStatusId())
                    .orElse(null);
            if (status == null) {
                return false;
            }
            entity.setFkIdManagementStatus(status);
        }
        return true;
    }

    private static ManagementView toView(DeedManagement entity) {
        Integer notaryId = entity.getFkIdNotaryPerson() != null
                ? entity.getFkIdNotaryPerson().getPersonId() : null;
        Integer statusId = entity.getFkIdManagementStatus() != null
                ? entity.getFkIdManagementStatus().getIdManagementStatus() : null;
        return new ManagementView(
                entity.getIdManagement(),
                entity.getNumber(),
                entity.getHeading(),
                entity.getDateStart(),
                entity.getNotes(),
                notaryId,
                statusId,
                entity.getPendingDebtAtArchiving(),
                entity.getVersion());
    }

    @Override
    @Transactional
    public CompleteCaseResult createCompleteCase(CompleteCaseCommand request) {
        if (!hasRequiredFields(request)) {
            return new CompleteCaseResult.BadRequest();
        }
        Optional<CaseDependencies> dependencies = resolveDependencies(request);
        if (dependencies.isEmpty()) {
            return new CompleteCaseResult.BadRequest();
        }
        CaseDependencies deps = dependencies.get();
        managementStatusWriteGuard.validateInitialStatus(
                deps.typeProcedure().getWorkflowDefinition(), deps.status());
        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        applyManagementFields(management, request, deps);
        management = repository.save(management);
        saveProcedure(management, deps);
        statusLogService.registerStatus(management, null);
        return new CompleteCaseResult.Created(
                managementQueryService.findById(management.getIdManagement()).orElseThrow());
    }

    @Override
    @Transactional
    public CompleteCaseResult updateCompleteCase(Integer id, CompleteCaseCommand request) {
        Optional<DeedManagement> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return new CompleteCaseResult.NotFound();
        }
        if (!hasRequiredFields(request)) {
            return new CompleteCaseResult.BadRequest();
        }
        Optional<CaseDependencies> dependencies = resolveDependencies(request);
        if (dependencies.isEmpty()) {
            return new CompleteCaseResult.BadRequest();
        }
        DeedManagement management = existing.get();
        Integer previousStatusId = statusIdOf(management);
        managementStatusWriteGuard.rejectStatusMutationOnUpdate(
                previousStatusId, request.statusManagementId());
        CaseDependencies deps = dependencies.get();
        applyManagementFields(management, request, deps);
        management = repository.save(management);
        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(id);
        if (procedures.isEmpty()) {
            saveProcedure(management, deps);
        } else {
            updateProcedure(procedures.get(0), deps);
        }
        registerStatusChangeIfNeeded(management, previousStatusId);
        return new CompleteCaseResult.Updated(
                managementQueryService.findById(management.getIdManagement()).orElseThrow());
    }

    @Override
    @Transactional
    public SaveResult create(ManagementCommand request) {
        try {
            DeedManagement entity = new DeedManagement();
            if (!applyManagementRequest(entity, request)) {
                return new SaveResult.BadRequest();
            }
            entity = repository.save(entity);
            if (entity.getFkIdManagementStatus() != null) {
                statusLogService.registerStatus(entity, null);
            }
            return new SaveResult.Created(toView(entity));
        } catch (NotaireException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create management", e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    @Transactional
    public SaveResult update(Integer id, ManagementCommand request) {
        Optional<DeedManagement> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return new SaveResult.NotFound();
        }
        DeedManagement existing = existingOpt.get();
        try {
            Integer previousStatusId = statusIdOf(existing);
            managementStatusWriteGuard.rejectStatusMutationOnUpdate(
                    previousStatusId, request.managementStatusId());
            if (!applyManagementRequest(existing, request)) {
                return new SaveResult.BadRequest();
            }
            repository.save(existing);
            registerStatusChangeIfNeeded(existing, previousStatusId);
            return new SaveResult.Updated();
        } catch (NotaireException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update management id {}", id, e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    // No outer @Transactional: FK conflict must map to 409 without UnexpectedRollbackException.
    public DeleteResult delete(Integer id) {
        if (!repository.existsById(id)) {
            return new DeleteResult.NotFound();
        }
        try {
            repository.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            log.warn("Failed to delete management id {}", id, e);
            return new DeleteResult.Conflict();
        }
    }

    @Override
    // No @Transactional here: archiving() owns its TX. Catching IllegalArgumentException /
    // FoldersInWaitException inside an outer TX marks rollback-only → 500 (#1745 pattern).
    public ArchiveResult archive(Integer id, boolean confirmed) {
        try {
            ManagementArchiveDebtService.ArchiveResult result =
                    managementArchiveDebtService.archiving(id, confirmed);
            return new ArchiveResult.Ok(new ArchivedManagementView(
                    result.management().getIdManagement(),
                    result.pendingBalance(),
                    Boolean.TRUE.equals(result.management().getPendingDebtAtArchiving())));
        } catch (IllegalArgumentException e) {
            return new ArchiveResult.NotFound();
        } catch (FoldersInWaitException e) {
            List<Integer> waitingFolderNumbers = e.getFoldersInWait().stream()
                    .map(f -> f.getNumber())
                    .toList();
            return new ArchiveResult.WaitingFolders(e.getMessage(), waitingFolderNumbers);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DtoManagementCaseSummary getCaseSummary(Integer id) {
        return managementCaseSummaryService.getSummary(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DtoManagementFinancialSummary getFinancialSummary(Integer id) {
        return managementFinancialSummaryService.getSummary(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePendingBalance(Integer id) {
        return managementArchiveDebtService.calculatePendingBalance(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        Sort springSort = parseSort(sort);
        var pageable = PageRequest.of(Math.max(page, 0), size > 0 ? size : 20, springSort);
        var result = managementQueryService.findAll(pageable);
        return new PageResult(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    /** Parses {@code property} or {@code property,asc|desc}; default newest-first (#1340). */
    static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "idManagement");
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (property.isEmpty()) {
            property = "idManagement";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        } else if (parts.length == 1) {
            // Bare property from callers that omit direction: keep newest-first default.
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoManagementSummary> findById(Integer id) {
        return managementQueryService.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoManagementSummary> findByNumber(int number) {
        return managementQueryService.findByNumber(number);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagementView> findByClientPersonId(Integer idPerson) {
        return repository.findByClientPersonId(idPerson).stream()
                .map(ManagementOpsAdapter::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoHistorySummary> findCurrentStatus(Integer id) {
        Optional<DeedManagement> managementOpt = repository.findById(id);
        if (managementOpt.isEmpty()) {
            return Optional.empty();
        }
        List<History> historyRows = historyRepository.findByFkIdManagementIdManagement(id);
        if (!historyRows.isEmpty()) {
            return historyRows.stream()
                    .max(HistoryOrder.CHRONOLOGICAL)
                    .map(HistoryMapper::toDto);
        }
        DeedManagement management = managementOpt.get();
        ManagementStatus status = management.getFkIdManagementStatus();
        if (status == null) {
            return Optional.empty();
        }
        return Optional.of(new DtoHistorySummary(
                null,
                new Date(),
                null,
                management.getIdManagement(),
                status.getIdManagementStatus(),
                status.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompleteCaseView> findCompleteCase(Integer id) {
        Optional<DeedManagement> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        DeedManagement management = existing.get();
        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(id);
        Procedure procedure = procedures.isEmpty() ? null : procedures.get(0);
        Integer statusId = management.getFkIdManagementStatus() != null
                ? management.getFkIdManagementStatus().getIdManagementStatus() : null;
        return Optional.of(new CompleteCaseView(
                management.getIdManagement(),
                management.getNumber(),
                management.getHeading(),
                management.getNotes(),
                procedure == null || procedure.getFkIdBudget() == null ? null
                        : procedure.getFkIdBudget().getIdBudget(),
                management.getFkIdNotaryPerson() == null ? null : management.getFkIdNotaryPerson().getPersonId(),
                statusId,
                procedure == null || procedure.getFkIdProcedureType() == null ? null
                        : procedure.getFkIdProcedureType().getIdProcedureType(),
                procedure == null || procedure.getFkIdProperty() == null ? null
                        : procedure.getFkIdProperty().getIdProperty()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<DtoHistorySummary>> findHistory(Integer id) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        List<DtoHistorySummary> history = statusLogService.getHistory(id).stream()
                .map(HistoryMapper::toDto)
                .toList();
        return Optional.of(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findCurrentStatusName(Integer id) {
        return repository.findById(id)
                .map(DeedManagement::getFkIdManagementStatus)
                .map(ManagementStatus::getName);
    }
}
