package com.licensis.notaire.repository;

import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SubmittedDocumentRepository extends JpaRepository<SubmittedDocument, Integer> {

    List<SubmittedDocument> findByFkIdProcedure(Procedure procedure);

    List<SubmittedDocument> findByFkIdProcedureIdProcedure(Integer idProcedure);

    List<SubmittedDocument> findByReleased(Boolean released);

    List<SubmittedDocument> findByFlagged(Boolean flagged);

    List<SubmittedDocument> findByPrepared(Boolean prepared);

    boolean existsByDocumentTypeIdDocumentType(Integer idDocumentType);

    List<SubmittedDocument> findByFkIdProcedureFkIdManagementIdManagement(Integer idManagement);

    List<SubmittedDocument> findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(Integer idManagement,
            String deliveredBy);

    @Query("SELECT d FROM SubmittedDocument d LEFT JOIN FETCH d.fkIdProcedure p LEFT JOIN FETCH p.fkIdManagement "
            + "WHERE d.expires = true AND d.released = false AND d.dateDue BETWEEN :from AND :to "
            + "ORDER BY d.dateDue ASC, d.idSubmittedDocument ASC")
    List<SubmittedDocument> findUpcomingExpirations(@Param("from") Date from, @Param("to") Date to);
}
