package com.licensis.notaire.adapter.out.persistence.audit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort;
import com.licensis.notaire.dto.DtoAuditRecord;

/**
 * Ops adapter for audit queries — maps Spring Data pages to port views (issue #1890).
 */
@Component
public class AuditRecordOpsAdapter implements AuditRecordOpsPort {

    private final AuditRecordService auditRecordService;

    public AuditRecordOpsAdapter(AuditRecordService auditRecordService) {
        this.auditRecordService = auditRecordService;
    }

    @Override
    public List<DtoAuditRecord> findAll() {
        return auditRecordService.findAllAsDto();
    }

    @Override
    public PageResult search(
            String module, Integer idUser, LocalDate from, LocalDate to, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<DtoAuditRecord> result = auditRecordService.searchAsDto(module, idUser, from, to, pageable);
        return new PageResult(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast());
    }

    @Override
    public Optional<DtoAuditRecord> findById(Integer id) {
        return auditRecordService.findByIdAsDto(id);
    }

    @Override
    public List<DtoAuditRecord> findByUserId(Integer idUser) {
        return auditRecordService.findByUserIdAsDto(idUser);
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "date");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1) {
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ex) {
                direction = Sort.Direction.DESC;
            }
        }
        return Sort.by(direction, property);
    }
}
