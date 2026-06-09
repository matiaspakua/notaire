package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Integer> {

    List<WorkflowNode> findByWorkflowDefinitionId(Integer workflowDefinitionId);

    List<WorkflowNode> findByWorkflowDefinitionIdAndTipo(Integer workflowDefinitionId, WorkflowNodeType tipo);
}
