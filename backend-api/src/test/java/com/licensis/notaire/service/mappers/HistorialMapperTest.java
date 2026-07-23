package com.licensis.notaire.service.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.dto.DtoHistorialSummary;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;

class HistorialMapperTest {

    @Test
    @DisplayName("Should map all fields when gestion and estado are present")
    void shouldMapAllFieldsWhenGestionAndEstadoArePresent() {
        Date fecha = new Date();
        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setIdGestion(7);
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setIdEstadoGestion(3);
        estado.setNombre("En proceso");

        Historial historial = new Historial(1, fecha);
        historial.setObservaciones("Cambio de estado inicial");
        historial.setFkIdGestion(gestion);
        historial.setFkIdEstadoGestion(estado);

        DtoHistorialSummary dto = HistorialMapper.toDto(historial);

        assertThat(dto.idHistorial()).isEqualTo(1);
        assertThat(dto.fecha()).isEqualTo(fecha);
        assertThat(dto.observaciones()).isEqualTo("Cambio de estado inicial");
        assertThat(dto.gestionId()).isEqualTo(7);
        assertThat(dto.estadoGestionId()).isEqualTo(3);
        assertThat(dto.estadoGestionNombre()).isEqualTo("En proceso");
    }

    @Test
    @DisplayName("Should map a null gestion ID when fkIdGestion is null")
    void shouldMapNullGestionIdWhenFkIdGestionIsNull() {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setIdEstadoGestion(3);
        estado.setNombre("En proceso");

        Historial historial = new Historial(1, new Date());
        historial.setFkIdGestion(null);
        historial.setFkIdEstadoGestion(estado);

        DtoHistorialSummary dto = HistorialMapper.toDto(historial);

        assertThat(dto.gestionId()).isNull();
        assertThat(dto.estadoGestionId()).isEqualTo(3);
        assertThat(dto.estadoGestionNombre()).isEqualTo("En proceso");
    }

    @Test
    @DisplayName("Should map null estado fields when fkIdEstadoGestion is null")
    void shouldMapNullEstadoFieldsWhenFkIdEstadoGestionIsNull() {
        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setIdGestion(7);

        Historial historial = new Historial(1, new Date());
        historial.setFkIdGestion(gestion);
        historial.setFkIdEstadoGestion(null);

        DtoHistorialSummary dto = HistorialMapper.toDto(historial);

        assertThat(dto.gestionId()).isEqualTo(7);
        assertThat(dto.estadoGestionId()).isNull();
        assertThat(dto.estadoGestionNombre()).isNull();
    }

    @Test
    @DisplayName("Should map both gestion and estado as null when both FKs are absent")
    void shouldMapBothNullWhenBothForeignKeysAreAbsent() {
        Historial historial = new Historial(1, new Date());
        historial.setFkIdGestion(null);
        historial.setFkIdEstadoGestion(null);

        DtoHistorialSummary dto = HistorialMapper.toDto(historial);

        assertThat(dto.gestionId()).isNull();
        assertThat(dto.estadoGestionId()).isNull();
        assertThat(dto.estadoGestionNombre()).isNull();
    }
}
