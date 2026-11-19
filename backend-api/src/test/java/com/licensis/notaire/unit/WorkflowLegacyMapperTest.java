package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.workflow.WorkflowLegacyMapper;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.business.WorkflowTransition;

class WorkflowLegacyMapperTest {

    @Test
    @DisplayName("definition mapping copies id, name, description, active, and version")
    void definitionCopiesFields() {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(4);
        definition.setName("protocol");
        definition.setDescription("notes");
        definition.setActive(true);
        definition.setVersion(2);

        var dto = WorkflowLegacyMapper.toDto(definition);

        assertThat(dto.getId()).isEqualTo(4);
        assertThat(dto.getName()).isEqualTo("protocol");
        assertThat(dto.getDescription()).isEqualTo("notes");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("node mapping copies links and leaves them null when absent")
    void nodeCopiesLinks() {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(4);
        ManagementStatus status = new ManagementStatus();
        status.setIdManagementStatus(8);
        status.setName("Open");
        WorkflowNode node = new WorkflowNode();
        node.setId(5);
        node.setWorkflowDefinition(definition);
        node.setManagementStatus(status);
        node.setType(WorkflowNodeType.INITIAL);
        node.setPositionX(1.5f);
        node.setPositionY(2.5f);
        node.setVersion(3);

        var dto = WorkflowLegacyMapper.toDto(node);

        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getWorkflowDefinitionId()).isEqualTo(4);
        assertThat(dto.getStatusManagementId()).isEqualTo(8);
        assertThat(dto.getStatusManagementName()).isEqualTo("Open");
        assertThat(dto.getType()).isEqualTo("INITIAL");
        assertThat(dto.getPositionX()).isEqualTo(1.5f);
        assertThat(dto.getVersion()).isEqualTo(3);

        WorkflowNode bare = new WorkflowNode();
        bare.setId(6);
        var empty = WorkflowLegacyMapper.toDto(bare);
        assertThat(empty.getWorkflowDefinitionId()).isNull();
        assertThat(empty.getStatusManagementId()).isNull();
        assertThat(empty.getType()).isNull();
    }

    @Test
    @DisplayName("transition mapping copies endpoints and leaves them null when absent")
    void transitionCopiesEndpoints() {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(4);
        WorkflowNode origin = new WorkflowNode();
        origin.setId(1);
        WorkflowNode destination = new WorkflowNode();
        destination.setId(2);
        WorkflowTransition transition = new WorkflowTransition();
        transition.setId(9);
        transition.setWorkflowDefinition(definition);
        transition.setOriginNode(origin);
        transition.setDestinationNode(destination);
        transition.setCondition("ready");
        transition.setDescription("move");
        transition.setVersion(1);

        var dto = WorkflowLegacyMapper.toDto(transition);

        assertThat(dto.getId()).isEqualTo(9);
        assertThat(dto.getWorkflowDefinitionId()).isEqualTo(4);
        assertThat(dto.getOriginNodeId()).isEqualTo(1);
        assertThat(dto.getDestinationNodeId()).isEqualTo(2);
        assertThat(dto.getCondition()).isEqualTo("ready");
        assertThat(dto.getDescription()).isEqualTo("move");

        WorkflowTransition bare = new WorkflowTransition();
        assertThat(WorkflowLegacyMapper.toDto(bare).getOriginNodeId()).isNull();
    }

    @Test
    @DisplayName("the entities no longer map themselves")
    void entitiesDoNotMapThemselves() throws Exception {
        for (String name : new String[] {"WorkflowDefinition.java", "WorkflowNode.java", "WorkflowTransition.java"}) {
            String source = Files.readString(Path.of("src/main/java/com/licensis/notaire/business", name));
            assertThat(source).doesNotContain("toDto");
        }
    }
}
