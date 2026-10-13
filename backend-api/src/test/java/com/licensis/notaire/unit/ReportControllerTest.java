package com.licensis.notaire.unit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.licensis.notaire.adapter.in.web.report.ReportController;
import com.licensis.notaire.application.usecase.report.ReportService;
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

@DisplayName("ReportController unit tests")
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    private ReportController controller;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        controller = new ReportController(reportService);

        Logger logger = (Logger) LoggerFactory.getLogger(ReportController.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(ReportController.class);
        logger.detachAppender(logAppender);
    }

    @Test
    @DisplayName("Should return PDF bytes with correct headers when report generation succeeds")
    void shouldReturnPdfBytesWithCorrectHeadersWhenReportGenerationSucceeds() throws Exception {
        byte[] pdfBytes = {1, 2, 3};
        when(reportService.generateBudgetReport(42)).thenReturn(pdfBytes);

        ResponseEntity<?> response = controller.generateBudgetReport(42);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"presupuesto_42.pdf\"");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL)).isEqualTo("private, no-store");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("Should log the exception when report generation fails")
    void shouldLogExceptionWhenReportGenerationFails() throws Exception {
        RuntimeException cause = new RuntimeException("pdf rendering failure");
        when(reportService.generateBudgetReport(42)).thenThrow(cause);

        ResponseEntity<?> response = controller.generateBudgetReport(42);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(logAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getThrowableProxy().getMessage()).isEqualTo("pdf rendering failure");
                });
    }

    @Test
    @DisplayName("Should return 500 and log when the procedure document-list report fails")
    void shouldReturn500AndLogWhenProcedureDocumentListReportFails() throws Exception {
        when(reportService.generateProcedureDocumentsListReport("Sale")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = controller.generateProcedureDocumentsListReport("Sale");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(logAppender.list).isNotEmpty();
    }

    @Test
    @DisplayName("CU82 - Should generate the registration draft report")
    void shouldGenerateRegistrationDraftReport() {
        byte[] pdfBytes = {1, 2, 3};
        when(reportService.generateRegistrationDraftReport(1)).thenReturn(pdfBytes);

        ResponseEntity<?> response = controller.generateRegistrationDraftReport(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"minuta_inscripcion_1.pdf\"");
    }

    @Test
    @DisplayName("CU15 - Should generate the payment receipt report")
    void shouldGeneratePaymentReceiptReport() {
        byte[] pdfBytes = {1, 2, 3};
        when(reportService.generatePaymentReceiptReport(1)).thenReturn(pdfBytes);

        ResponseEntity<?> response = controller.generatePaymentReceiptReport(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"recibo_pago_1.pdf\"");
    }
}
