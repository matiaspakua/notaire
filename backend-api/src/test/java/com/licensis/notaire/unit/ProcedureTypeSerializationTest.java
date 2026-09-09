package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcedureTypeSerializationTest {

    @Test
    @DisplayName("Should not serialize lazy workflowDefinition association to JSON")
    void shouldNotSerializeLazyWorkflowDefinitionAssociationToJson() throws Exception {
        ProcedureType type = new ProcedureType();
        type.setIdProcedureType(1);
        type.setName("Compraventa");
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(1);
        definition.setName("Workflow de Gestión Estándar");
        type.setWorkflowDefinition(definition);

        String json = new ObjectMapper().writeValueAsString(type);

        assertThat(json).doesNotContain("workflowDefinition");
        assertThat(json).contains("\"name\":\"Compraventa\"");
    }
}
