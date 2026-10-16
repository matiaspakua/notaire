package com.licensis.notaire.adapter.out.persistence.history;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.history.HistoryOpsPort;
import com.licensis.notaire.application.usecase.history.HistoryMapper;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.HistoryOrder;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;

/**
 * Persistence/ops adapter for management history. Holds repository access previously on
 * {@code HistoryController} (issue #1775).
 */
@Component
public class HistoryOpsAdapter implements HistoryOpsPort {

    private static final Logger log = LoggerFactory.getLogger(HistoryOpsAdapter.class);

    private final HistoryRepository repository;
    private final ManagementStatusRepository managementStatusRepository;
    private final DeedManagementRepository deedManagementRepository;

    public HistoryOpsAdapter(
            HistoryRepository repository,
            ManagementStatusRepository managementStatusRepository,
            DeedManagementRepository deedManagementRepository) {
        this.repository = repository;
        this.managementStatusRepository = managementStatusRepository;
        this.deedManagementRepository = deedManagementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        Sort springSort = parseSort(sort);
        var pageable = PageRequest.of(Math.max(page, 0), size > 0 ? size : 20, springSort);
        var result = repository.findAll(pageable).map(HistoryMapper::toDto);
        return new PageResult(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    /**
     * Parses {@code property} or {@code property,asc|desc}; default idHistory ascending
     * (matches {@code @PageableDefault(sort = "idHistory")} on the controller).
     */
    static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "idHistory");
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (property.isEmpty()) {
            property = "idHistory";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoHistorySummary> findById(Integer id) {
        return repository.findById(id).map(HistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoHistorySummary> findByManagementId(Integer idManagement) {
        return repository.findByFkIdManagementIdManagement(idManagement).stream()
                .sorted(HistoryOrder.CHRONOLOGICAL)
                .map(HistoryMapper::toDto)
                .toList();
    }

    @Override
    public SaveResult create(HistoryCommand command) {
        try {
            History entity = new History();
            if (!applyRequest(entity, command)) {
                return new SaveResult.BadRequest();
            }
            entity = repository.save(entity);
            return new SaveResult.Created(HistoryMapper.toDto(entity));
        } catch (Exception e) {
            log.error("Failed to create history", e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public SaveResult update(Integer id, HistoryCommand command) {
        History existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return new SaveResult.NotFound();
        }
        try {
            if (!applyRequest(existing, command)) {
                return new SaveResult.BadRequest();
            }
            repository.save(existing);
            return new SaveResult.Updated();
        } catch (Exception e) {
            log.error("Failed to update history id {}", id, e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    @Transactional
    public DeleteResult delete(Integer id) {
        History entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return new DeleteResult.NotFound();
        }
        try {
            entity.getFkIdManagementStatus().getHistoryList().remove(entity);
            repository.delete(entity);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            log.error("Failed to delete history id {}", id, e);
            return new DeleteResult.Conflict();
        }
    }

    private boolean applyRequest(History entity, HistoryCommand command) {
        ManagementStatus status = managementStatusRepository.findById(command.managementStatusId()).orElse(null);
        DeedManagement management = deedManagementRepository.findById(command.managementId()).orElse(null);
        if (status == null || management == null) {
            return false;
        }
        entity.setDate(command.date() != null ? command.date() : new Date());
        entity.setNotes(command.notes());
        entity.setFkIdManagementStatus(status);
        entity.setFkIdManagement(management);
        return true;
    }
}
