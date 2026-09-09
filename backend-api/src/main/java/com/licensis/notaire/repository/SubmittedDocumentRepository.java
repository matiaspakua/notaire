package com.licensis.notaire.repository;

import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmittedDocumentRepository extends JpaRepository<SubmittedDocument, Integer> {

    List<SubmittedDocument> findByFkIdProcedure(Procedure procedure);

    List<SubmittedDocument> findByFkIdProcedureIdProcedure(Integer idProcedure);

    List<SubmittedDocument> findByReleased(Boolean released);

    List<SubmittedDocument> findByFlagged(Boolean flagged);

    List<SubmittedDocument> findByPrepared(Boolean prepared);

    boolean existsByFkIdDocumentType(Integer fkIdDocumentType);

    List<SubmittedDocument> findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(Integer idManagement,
            String deliveredBy);
}
