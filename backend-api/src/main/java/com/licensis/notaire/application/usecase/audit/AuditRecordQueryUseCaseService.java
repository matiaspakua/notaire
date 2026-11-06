package com.licensis.notaire.application.usecase.audit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.audit.QueryAuditRecordsUseCase;
import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort;
import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort.PageResult;
import com.licensis.notaire.dto.DtoAuditRecord;

/**
 * Read-side use case for audit records — TX lives here, not on the controller (issue #578).
 */
@Service
public class AuditRecordQueryUseCaseService implements QueryAuditRecordsUseCase {

    private final AuditRecordOpsPort ops;

    public AuditRecordQueryUseCaseService(AuditRecordOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoAuditRecord> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult search(
            String module, Integer idUser, LocalDate from, LocalDate to, int page, int size, String sort) {
        return ops.search(module, idUser, from, to, page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoAuditRecord> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoAuditRecord> findByUserId(Integer idUser) {
        return ops.findByUserId(idUser);
    }
}
