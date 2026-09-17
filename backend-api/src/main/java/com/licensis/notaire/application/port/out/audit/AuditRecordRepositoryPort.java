package com.licensis.notaire.application.port.out.audit;

import com.licensis.notaire.business.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for audit record persistence operations.
 */
public interface AuditRecordRepositoryPort {

    List<AuditRecord> findAll();

    Optional<AuditRecord> findById(Integer id);

    AuditRecord save(AuditRecord entity);

    Page<AuditRecord> findAll(Pageable pageable);

    Page<AuditRecord> findByModule(String module, Pageable pageable);

    Page<AuditRecord> findByFkIdUserIdUser(Integer idUser, Pageable pageable);

    List<AuditRecord> findByFkIdUserIdUser(Integer idUser);

    List<AuditRecord> findAllWithUser();

    Optional<AuditRecord> findByIdWithUser(Integer id);

    List<AuditRecord> findByUserIdWithUser(Integer idUser);
}
