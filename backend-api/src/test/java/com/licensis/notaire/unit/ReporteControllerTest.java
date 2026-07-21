package com.licensis.notaire.unit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.licensis.notaire.api.ReporteController;
import com.licensis.notaire.service.ReporteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("ReporteController unit tests")
@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private ReporteService reporteService;

    private ReporteController controller;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        controller = new ReporteController(reporteService);

        Logger logger = (Logger) LoggerFactory.getLogger(ReporteController.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(ReporteController.class);
        logger.detachAppender(logAppender);
    }

    @Test
    @DisplayName("Should return PDF bytes with correct headers when report generation succeeds")
    void shouldReturnPdfBytesWithCorrectHeadersWhenReportGenerationSucceeds() throws Exception {
        byte[] pdfBytes = {1, 2, 3};
        when(reporteService.generarReportePresupuesto(42)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = controller.generarReportePresupuesto(42);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"presupuesto_42.pdf\"");
    }

    @Test
    @DisplayName("Should log the exception when report generation fails")
    void shouldLogExceptionWhenReportGenerationFails() throws Exception {
        RuntimeException cause = new RuntimeException("jasper compile failure");
        when(reporteService.generarReportePresupuesto(42)).thenThrow(cause);

        ResponseEntity<byte[]> response = controller.generarReportePresupuesto(42);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(logAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getThrowableProxy().getMessage()).isEqualTo("jasper compile failure");
                });
    }

    @Test
    @DisplayName("Should return 500 and log when lista-documentos-tramite report generation fails")
    void shouldReturn500AndLogWhenListaDocumentosTramiteReportFails() throws Exception {
        when(reporteService.generarReporteListaDocumentosTramite("Venta")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<byte[]> response = controller.generarReporteListaDocumentosTramite("Venta");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(logAppender.list).isNotEmpty();
    }
}
