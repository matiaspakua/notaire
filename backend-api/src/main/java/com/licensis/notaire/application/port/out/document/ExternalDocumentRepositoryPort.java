package com.licensis.notaire.application.port.out.document;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.SubmittedDocument;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for external entity document operations.
 * Used by ExternalEntityDocumentService to query and manage documents
 * from external entities like registries and cadastre.
 */
public interface ExternalDocumentRepositoryPort {

    SubmittedDocument saveSubmittedDocument(SubmittedDocument entity);

    Optional<SubmittedDocument> findSubmittedDocumentById(Integer id);

    List<SubmittedDocument> findByManagementAndDeliveredBy(Integer managementId, String deliveredBy);

    Optional<DeedManagement> findManagementById(Integer id);

    List<Procedure> findProceduresByManagementId(Integer managementId);
}
