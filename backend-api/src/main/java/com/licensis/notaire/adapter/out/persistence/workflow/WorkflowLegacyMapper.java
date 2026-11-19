package com.licensis.notaire.adapter.out.persistence.workflow;

import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.dto.DtoWorkflowTransition;

/** Transport mapping for the workflow entities. Those entities do not import the DTOs. */
public final class WorkflowLegacyMapper {

    private WorkflowLegacyMapper() {
    }

    public static DtoWorkflowDefinition toDto(WorkflowDefinition definition) {
        DtoWorkflowDefinition dto = new DtoWorkflowDefinition();
        dto.setId(definition.getId());
        dto.setName(definition.getName());
        dto.setDescription(definition.getDescription());
        dto.setActive(definition.isActive());
        dto.setVersion(definition.getVersion());
        return dto;
    }

    public static DtoWorkflowNode toDto(WorkflowNode node) {
        DtoWorkflowNode dto = new DtoWorkflowNode();
        dto.setId(node.getId());
        dto.setWorkflowDefinitionId(node.getWorkflowDefinition() != null ? node.getWorkflowDefinition().getId() : null);
        dto.setStatusManagementId(node.getManagementStatus() != null ? node.getManagementStatus().getIdManagementStatus() : null);
        dto.setStatusManagementName(node.getManagementStatus() != null ? node.getManagementStatus().getName() : null);
        dto.setType(node.getType() != null ? node.getType().name() : null);
        dto.setPositionX(node.getPositionX());
        dto.setPositionY(node.getPositionY());
        dto.setVersion(node.getVersion());
        return dto;
    }

    public static DtoWorkflowTransition toDto(WorkflowTransition transition) {
        DtoWorkflowTransition dto = new DtoWorkflowTransition();
        dto.setId(transition.getId());
        dto.setWorkflowDefinitionId(
                transition.getWorkflowDefinition() != null ? transition.getWorkflowDefinition().getId() : null);
        dto.setOriginNodeId(transition.getOriginNode() != null ? transition.getOriginNode().getId() : null);
        dto.setDestinationNodeId(
                transition.getDestinationNode() != null ? transition.getDestinationNode().getId() : null);
        dto.setCondition(transition.getCondition());
        dto.setDescription(transition.getDescription());
        dto.setVersion(transition.getVersion());
        return dto;
    }
}
