package com.licensis.notaire.repository;

import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentCostTemplatePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCostTemplateRepository
        extends JpaRepository<DocumentCostTemplate, DocumentCostTemplatePK> {

    List<DocumentCostTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType);
}
