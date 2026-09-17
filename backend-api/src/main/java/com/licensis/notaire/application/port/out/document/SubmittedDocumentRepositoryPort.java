package com.licensis.notaire.application.port.out.document;

import com.licensis.notaire.business.SubmittedDocument;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for submitted document persistence operations.
 */
public interface SubmittedDocumentRepositoryPort {

    SubmittedDocument save(SubmittedDocument entity);

    Optional<SubmittedDocument> findById(Integer id);

    List<SubmittedDocument> findByManagementAndDeliveredBy(Integer managementId, String deliveredBy);

    boolean existsById(Integer id);
}
