package com.licensis.notaire.unit;


import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.business.Concept;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptDtoMappingTest {

    @Test
    void shouldMapConceptToDtoIncludingFlags() {
        Concept concept = new Concept();
        concept.setIdConcept(7);
        concept.setName("Concepto test");
        concept.setValue(new java.math.BigDecimal("150.75"));
        concept.setPercentage(12);
        concept.setEnabled(true);
        concept.setFixedConcept(false);
        concept.setVersion(3);

        DtoConcept dto = concept.getDto();

        assertEquals(7, dto.getIdConcept());
        assertEquals("Concepto test", dto.getName());
        assertEquals(new java.math.BigDecimal("150.75"), dto.getValue());
        assertEquals(12, dto.getPercentage());
        assertTrue(dto.getEnabled());
        assertFalse(dto.isFixed());
        assertEquals(3, dto.getVersion());
    }
}
