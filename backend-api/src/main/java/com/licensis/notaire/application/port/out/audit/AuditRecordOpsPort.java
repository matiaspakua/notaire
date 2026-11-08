package com.licensis.notaire.application.port.out.audit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoAuditRecord;

/**
 * Outbound ops port for audit queries (issue #1890).
 * View/DTO shapes only — no JPA entities or Spring Data Page/Pageable on the port.
 */
public interface AuditRecordOpsPort {

    record PageResult(
            List<DtoAuditRecord> content,
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last) {}

    List<DtoAuditRecord> findAll();

    PageResult search(
            String module, Integer idUser, LocalDate from, LocalDate to, int page, int size, String sort);

    Optional<DtoAuditRecord> findById(Integer id);

    List<DtoAuditRecord> findByUserId(Integer idUser);
}
