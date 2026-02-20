package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.negocio.Concepto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptoDtoMappingTest {

    @Test
    void shouldMapConceptoToDtoIncludingFlags() {
        Concepto concepto = new Concepto();
        concepto.setIdConcepto(7);
        concepto.setNombre("Concepto test");
        concepto.setValor(150.75f);
        concepto.setPorcentaje(12);
        concepto.setHabilitado(true);
        concepto.setConceptoFijo(false);
        concepto.setVersion(3);

        DtoConcepto dto = concepto.getDto();

        assertEquals(7, dto.getIdConcepto());
        assertEquals("Concepto test", dto.getNombre());
        assertEquals(150.75f, dto.getValor());
        assertEquals(12, dto.getPorcentaje());
        assertTrue(dto.getHabilitado());
        assertFalse(dto.isFijo());
        assertEquals(3, dto.getVersion());
    }
}
