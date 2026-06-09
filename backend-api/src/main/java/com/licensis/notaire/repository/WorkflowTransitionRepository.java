package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Integer> {

    List<WorkflowTransition> findByWorkflowDefinitionId(Integer workflowDefinitionId);

    boolean existsByNodoOrigenIdOrNodoDestinoId(Integer nodoOrigenId, Integer nodoDestinoId);
}
