package com.licensis.notaire.repository;

import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Integer> {

    List<WorkflowNode> findByWorkflowDefinitionId(Integer workflowDefinitionId);

    List<WorkflowNode> findByWorkflowDefinitionIdAndType(Integer workflowDefinitionId, WorkflowNodeType type);
}
