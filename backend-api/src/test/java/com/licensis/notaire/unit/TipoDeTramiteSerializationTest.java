package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.WorkflowDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoDeTramiteSerializationTest {

    @Test
    @DisplayName("Should not serialize lazy workflowDefinition association to JSON")
    void shouldNotSerializeLazyWorkflowDefinitionAssociationToJson() throws Exception {
        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setIdTipoTramite(1);
        tipo.setNombre("Compraventa");
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(1);
        definition.setNombre("Workflow de Gestión Estándar");
        tipo.setWorkflowDefinition(definition);

        String json = new ObjectMapper().writeValueAsString(tipo);

        assertThat(json).doesNotContain("workflowDefinition");
        assertThat(json).contains("\"nombre\":\"Compraventa\"");
    }
}
