package com.licensis.notaire.repository;

import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureTemplateRepository extends JpaRepository<ProcedureTemplate, ProcedureTemplatePK> {

    List<ProcedureTemplate> findByProcedureType(ProcedureType procedureType);

    List<ProcedureTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType);

    List<ProcedureTemplate> findByDocumentType(DocumentType documentType);

    List<ProcedureTemplate> findByDocumentTypeIdDocumentType(Integer idDocumentType);
}
