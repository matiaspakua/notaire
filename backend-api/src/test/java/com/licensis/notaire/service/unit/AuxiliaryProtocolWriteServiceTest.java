package com.licensis.notaire.service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.DeedView;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;
import com.licensis.notaire.application.usecase.notebook.AuxiliaryProtocolQueryUseCaseService;
import com.licensis.notaire.application.usecase.notebook.AuxiliaryProtocolWriteService;
import com.licensis.notaire.domain.folio.Folio;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auxiliary protocol inbound ports (issue #1825)")
class AuxiliaryProtocolWriteServiceTest {

    @Mock
    private AuxiliaryProtocolOpsPort ops;

    private AuxiliaryProtocolWriteService writeService;
    private AuxiliaryProtocolQueryUseCaseService queryService;

    @BeforeEach
    void setUp() {
        writeService = new AuxiliaryProtocolWriteService(ops);
        queryService = new AuxiliaryProtocolQueryUseCaseService(ops);
    }

    @Test
    @DisplayName("Should delegate list of available folios to the ops port")
    void shouldDelegateListAvailableFolios() {
        Folio folio = Folio.create(1, 2026, "New", null, 10, 1, null);
        when(ops.listAvailableFolios()).thenReturn(List.of(folio));

        assertThat(queryService.listAvailableFolios()).containsExactly(folio);
        verify(ops).listAvailableFolios();
    }

    @Test
    @DisplayName("Should delegate start deed and return the created view")
    void shouldDelegateStartDeed() {
        Date when = new Date();
        DeedView view = new DeedView(7, 1, "body", "Draft", when, null, null, null, 0);
        when(ops.startDeed(3, "body", when)).thenReturn(new StartResult.Created(view));

        StartResult result = writeService.startDeed(3, "body", when);

        assertThat(result).isInstanceOf(StartResult.Created.class);
        assertThat(((StartResult.Created) result).view().idDeed()).isEqualTo(7);
        verify(ops).startDeed(3, "body", when);
    }

    @Test
    @DisplayName("Should propagate bad-request start results from the ops port")
    void shouldPropagateBadRequest() {
        Date when = new Date();
        when(ops.startDeed(null, "body", when))
                .thenReturn(new StartResult.BadRequest("The auxiliary folio where the deed starts is required"));

        StartResult result = writeService.startDeed(null, "body", when);

        assertThat(result).isInstanceOf(StartResult.BadRequest.class);
        assertThat(((StartResult.BadRequest) result).message())
                .isEqualTo("The auxiliary folio where the deed starts is required");
    }
}
