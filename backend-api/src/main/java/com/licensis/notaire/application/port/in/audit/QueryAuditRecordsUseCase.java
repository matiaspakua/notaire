package com.licensis.notaire.application.port.in.audit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort.PageResult;
import com.licensis.notaire.dto.DtoAuditRecord;

/**
 * Read-side inbound port for audit log queries (issue #578 / CU23).
 */
public interface QueryAuditRecordsUseCase {

    List<DtoAuditRecord> findAll();

    PageResult search(
            String module, Integer idUser, LocalDate from, LocalDate to, int page, int size, String sort);

    Optional<DtoAuditRecord> findById(Integer id);

    List<DtoAuditRecord> findByUserId(Integer idUser);
}
