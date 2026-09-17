package com.licensis.notaire.adapter.out.persistence.audit;

import com.licensis.notaire.application.port.out.audit.AuditRecordRepositoryPort;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.repository.AuditRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the audit slice.
 * The only place where {@link AuditRecordRepository} is touched for this slice.
 */
@Component
public class AuditRecordPersistenceAdapter implements AuditRecordRepositoryPort {

    private final AuditRecordRepository auditRecordRepository;

    public AuditRecordPersistenceAdapter(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Override
    public List<AuditRecord> findAll() {
        return auditRecordRepository.findAll();
    }

    @Override
    public Optional<AuditRecord> findById(Integer id) {
        return auditRecordRepository.findById(id);
    }

    @Override
    public AuditRecord save(AuditRecord entity) {
        return auditRecordRepository.save(entity);
    }

    @Override
    public Page<AuditRecord> findAll(Pageable pageable) {
        return auditRecordRepository.findAll(pageable);
    }

    @Override
    public Page<AuditRecord> findByModule(String module, Pageable pageable) {
        return auditRecordRepository.findByModule(module, pageable);
    }

    @Override
    public Page<AuditRecord> findByFkIdUserIdUser(Integer idUser, Pageable pageable) {
        return auditRecordRepository.findByFkIdUserIdUser(idUser, pageable);
    }

    @Override
    public List<AuditRecord> findByFkIdUserIdUser(Integer idUser) {
        return auditRecordRepository.findByFkIdUserIdUser(idUser);
    }

    @Override
    public List<AuditRecord> findAllWithUser() {
        return auditRecordRepository.findAllWithUser();
    }

    @Override
    public Optional<AuditRecord> findByIdWithUser(Integer id) {
        return auditRecordRepository.findByIdWithUser(id);
    }

    @Override
    public List<AuditRecord> findByUserIdWithUser(Integer idUser) {
        return auditRecordRepository.findByUserIdWithUser(idUser);
    }
}
