package com.licensis.notaire.repository;

import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetTemplateRepository extends JpaRepository<BudgetTemplate, Integer> {

    List<BudgetTemplate> findByProcedureType(ProcedureType procedureType);

    List<BudgetTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType);

    List<BudgetTemplate> findByConcept(Concept concept);

    List<BudgetTemplate> findByConceptIdConcept(Integer idConcept);
}
