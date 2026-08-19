package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.dto.DtoPresupuesto;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Tramite business logic.
 */
public class TramiteTest {

    @Test
    public void testDtoConversion() {
        // Arrange
        Integer id = 100;
        String observaciones = "Tramite de prueba";

        Tramite tramite = new Tramite(id);
        tramite.setObservaciones(observaciones);

        TipoDeTramite tipoTramite = new TipoDeTramite(1, "Compraventa", true, true, true);
        tipoTramite.setObservaciones("Obs tipo");
        tramite.setFkIdTipoTramite(tipoTramite);

        // Act
        DtoTramite dto = tramite.getDto();

        // Assert
        assertNotNull("Dto should not be null", dto);
        assertEquals("Id matches", id, dto.getIdTramite());
        assertEquals("Observaciones match", observaciones, dto.getObservaciones());
        assertNotNull("TipoTramite DTO not null", dto.getTipoDeTramite());
        assertEquals("TipoTramite ID matches", (Integer) 1, dto.getTipoDeTramite().getIdTipoTramite());
    }

    @Test
    public void testSetAtributosFromDto() {
        // Arrange
        Tramite tramite = new Tramite();
        DtoTramite dto = new DtoTramite();
        dto.setIdTramite(200);
        dto.setObservaciones("Updated Obs");

        DtoTipoDeTramite dtoTipo = new DtoTipoDeTramite();
        dtoTipo.setIdTipoTramite(2);
        dtoTipo.setNombre("Hipoteca");
        dtoTipo.setHabilitado(true); // Inicializar habilitado para evitar NPE
        dto.setTiposDeTramite(dtoTipo);

        // Act
        tramite.setAtributos(dto);

        // Assert
        assertEquals("Id updated", (Integer) 200, tramite.getIdTramite());
        assertEquals("Observaciones updated", "Updated Obs", tramite.getObservaciones());
        assertNotNull("TipoTramite updated", tramite.getFkIdTipoTramite());
        assertEquals("TipoTramite ID updated", (Integer) 2, tramite.getFkIdTipoTramite().getIdTipoTramite());
    }
}
