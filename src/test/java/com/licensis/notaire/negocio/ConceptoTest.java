package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Concepto business logic.
 */
public class ConceptoTest {

    @Test
    public void testDtoConversion() {
        // Arrange
        Integer id = 1;
        String nombre = "Test Concepto";
        Float valor = 100.0f;
        Integer porcentaje = 10;

        Concepto concepto = new Concepto(id, nombre, valor, porcentaje);
        concepto.setHabilitado(true);
        concepto.setConceptoFijo(false);
        concepto.setVersion(1);

        // Act
        DtoConcepto dto = concepto.getDto();

        // Assert
        assertNotNull("Dto should not be null", dto);
        assertEquals("Id matches", id, dto.getIdConcepto());
        assertEquals("Nombre matches", nombre, dto.getNombre());
        assertEquals("Valor matches", valor, (Float) dto.getValor());
        assertEquals("Porcentaje matches", porcentaje, dto.getPorcentaje());
        assertTrue("Habilitado matches", dto.getHabilitado());
        assertFalse("Concepto fijo matches", dto.isFijo());
        assertEquals("Version matches", (Integer) 1, dto.getVersion());
    }

    @Test
    public void testSetAtributosFromDto() throws DtoInvalidoException {
        // Arrange
        Concepto concepto = new Concepto();
        DtoConcepto dto = new DtoConcepto();
        dto.setIdConcepto(2);
        dto.setNombre("Updated Concepto");
        dto.setValor(200.0f);
        dto.setPorcentaje(5);
        dto.setHabilitado(false);
        dto.setFijo(true);
        dto.setVersion(2);

        // Act
        concepto.setAtributos(dto);

        // Assert
        assertEquals("Id updated", (Integer) 2, concepto.getIdConcepto());
        assertEquals("Nombre updated", "Updated Concepto", concepto.getNombre());
        assertEquals("Valor updated", 200.0f, concepto.getValor(), 0.001);
        assertEquals("Porcentaje updated", 5, concepto.getPorcentaje());
        assertFalse("Habilitado updated", concepto.getHabilitado());
        assertTrue("Concepto fijo updated", concepto.isConceptoFijo());
        assertEquals("Version updated", 2, (int) concepto.getVersion());
    }

    @Test
    public void testPartialUpdate() throws DtoInvalidoException {
        // Arrange: existing concepto
        Concepto concepto = new Concepto(3, "Original", 50.0f, 0);
        concepto.setHabilitado(true);

        // Dto with only partial fields set (some nulls implied by default constructor,
        // checking null handling if applicable)
        // Checking code: setAtributos has null checks for Name, Valor, Porcentaje.

        DtoConcepto dto = new DtoConcepto();
        dto.setIdConcepto(3);
        dto.setVersion(1);
        dto.setHabilitado(true);
        dto.setFijo(false);
        // leaving nombre, valor, porcentaje null in DTO?
        // DtoConcepto fields might be primitives or objects. Let's verify DtoConcepto.
        // Assuming getters return objects or primitives. If primitives, they won't be
        // null.
        // Concepto.java:136 checks `if (nuevoDto.getNombre() != null)`.

        dto.setNombre("New Name");
        // We set name but leave others.

        // Act
        concepto.setAtributos(dto);

        // Assert
        assertEquals("Nombre should change", "New Name", concepto.getNombre());
        assertEquals("Valor should remain original", 50.0f, concepto.getValor(), 0.001);
        // Note: DtoConcepto fields are likely Objects (Integer, Float) if setAtributos
        // checks for null.
    }
}
