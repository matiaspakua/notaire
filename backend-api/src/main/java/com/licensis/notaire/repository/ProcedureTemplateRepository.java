package com.licensis.notaire.repository;

import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureTemplateRepository extends JpaRepository<ProcedureTemplate, ProcedureTemplatePK> {

    List<ProcedureTemplate> findByProcedureType(ProcedureType procedureType);

    List<ProcedureTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType);

    List<ProcedureTemplate> findByDocumentType(DocumentType documentType);

    List<ProcedureTemplate> findByDocumentTypeIdDocumentType(Integer idDocumentType);

    /**
     * Deletes one checklist row with a bulk statement (#1362). {@code deleteById} loads the row, and
     * its eagerly loaded document type keeps it in a cascading collection, so the flush re-persisted
     * the row and the delete silently did nothing.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ProcedureTemplate p WHERE p.procedureTemplatePK = :pk")
    int deleteByPk(@Param("pk") ProcedureTemplatePK pk);
}
