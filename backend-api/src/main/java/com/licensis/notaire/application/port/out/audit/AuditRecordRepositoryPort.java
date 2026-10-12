package com.licensis.notaire.application.port.out.audit;

import com.licensis.notaire.business.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
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

    /**
     * Paged search; every filter is optional (null = no filter). #1545 / CU23.
     *
     * @param fromInclusive records at or after this instant
     * @param toExclusive records strictly before this instant
     */
    Page<AuditRecord> search(String module, Integer idUser, Date fromInclusive, Date toExclusive,
            Pageable pageable);

    Page<AuditRecord> findByFkIdUserIdUser(Integer idUser, Pageable pageable);

    List<AuditRecord> findByFkIdUserIdUser(Integer idUser);

    List<AuditRecord> findAllWithUser();

    Optional<AuditRecord> findByIdWithUser(Integer id);

    List<AuditRecord> findByUserIdWithUser(Integer idUser);
}
