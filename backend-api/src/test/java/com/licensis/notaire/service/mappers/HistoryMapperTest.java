package com.licensis.notaire.service.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;

class HistoryMapperTest {

    @Test
    @DisplayName("Should map all fields when gestion and status are present")
    void shouldMapAllFieldsWhenManagementAndStatusArePresent() {
        Date date = new Date();
        DeedManagement management = new DeedManagement();
        management.setIdManagement(7);
        ManagementStatus status = new ManagementStatus();
        status.setIdManagementStatus(3);
        status.setName("En proceso");

        History history = new History(1, date);
        history.setNotes("Cambio de status inicial");
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(status);

        DtoHistorySummary dto = HistoryMapper.toDto(history);

        assertThat(dto.idHistory()).isEqualTo(1);
        assertThat(dto.date()).isEqualTo(date);
        assertThat(dto.notes()).isEqualTo("Cambio de status inicial");
        assertThat(dto.managementId()).isEqualTo(7);
        assertThat(dto.statusManagementId()).isEqualTo(3);
        assertThat(dto.statusManagementName()).isEqualTo("En proceso");
    }

    @Test
    @DisplayName("Should map a null gestion ID when fkIdGestion is null")
    void shouldMapNullManagementIdWhenFkIdManagementIsNull() {
        ManagementStatus status = new ManagementStatus();
        status.setIdManagementStatus(3);
        status.setName("En proceso");

        History history = new History(1, new Date());
        history.setFkIdManagement(null);
        history.setFkIdManagementStatus(status);

        DtoHistorySummary dto = HistoryMapper.toDto(history);

        assertThat(dto.managementId()).isNull();
        assertThat(dto.statusManagementId()).isEqualTo(3);
        assertThat(dto.statusManagementName()).isEqualTo("En proceso");
    }

    @Test
    @DisplayName("Should map null status fields when fkIdEstadoGestion is null")
    void shouldMapNullStatusFieldsWhenFkIdManagementStatusIsNull() {
        DeedManagement management = new DeedManagement();
        management.setIdManagement(7);

        History history = new History(1, new Date());
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(null);

        DtoHistorySummary dto = HistoryMapper.toDto(history);

        assertThat(dto.managementId()).isEqualTo(7);
        assertThat(dto.statusManagementId()).isNull();
        assertThat(dto.statusManagementName()).isNull();
    }

    @Test
    @DisplayName("Should map both gestion and status as null when both FKs are absent")
    void shouldMapBothNullWhenBothForeignKeysAreAbsent() {
        History history = new History(1, new Date());
        history.setFkIdManagement(null);
        history.setFkIdManagementStatus(null);

        DtoHistorySummary dto = HistoryMapper.toDto(history);

        assertThat(dto.managementId()).isNull();
        assertThat(dto.statusManagementId()).isNull();
        assertThat(dto.statusManagementName()).isNull();
    }
}
