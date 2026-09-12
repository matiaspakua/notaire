package com.licensis.notaire.repository;

import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Integer> {

    List<AuditRecord> findByFkIdUser(User user);

    List<AuditRecord> findByFkIdUserIdUser(Integer idUser);

    Page<AuditRecord> findByFkIdUserIdUser(Integer idUser, Pageable pageable);

    Page<AuditRecord> findByModule(String module, Pageable pageable);

    @Query("SELECT r FROM AuditRecord r LEFT JOIN FETCH r.fkIdUser u "
            + "LEFT JOIN FETCH u.fkIdPerson")
    List<AuditRecord> findAllWithUser();

    @Query("SELECT r FROM AuditRecord r LEFT JOIN FETCH r.fkIdUser u "
            + "LEFT JOIN FETCH u.fkIdPerson WHERE r.idAuditRecord = :id")
    Optional<AuditRecord> findByIdWithUser(@Param("id") Integer id);

    @Query("SELECT r FROM AuditRecord r LEFT JOIN FETCH r.fkIdUser u "
            + "LEFT JOIN FETCH u.fkIdPerson WHERE u.idUser = :idUsuario")
    List<AuditRecord> findByUserIdWithUser(@Param("idUsuario") Integer idUser);
}
