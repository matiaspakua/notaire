package com.licensis.notaire.adapter.in.web.report;

import com.licensis.notaire.application.usecase.report.ReportService;
import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST for generación of reportes PDF.
 *
 * <p>The reportes generan internamente with Apache PDFBox a partir of the modelo of
 * dominio actual (issue #567); ya no se usan plantillas JasperReports.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Validated
@Tag(name = "Reportes", description = "API for generating PDF reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reporteService;

    public ReportController(ReportService reporteService) {
        this.reporteService = reporteService;
    }

    @FunctionalInterface
    private interface ReportGenerator {
        byte[] generate() throws Exception;
    }

    private ResponseEntity<byte[]> buildPdfResponse(String filename, ReportGenerator generator) {
        try {
            byte[] pdfBytes = generator.generate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(pdfBytes);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate report '{}'", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF of the budget"),
        @ApiResponse(responseCode = "400", description = "Parámetro invalid", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado al generar el PDF (sin cuerpo)",
                content = @Content)
    })
    @GetMapping(value = "/budget/{idBudget}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate reporte of budget",
               description = "Generates a PDF with the datos of the budget (client, procedures, management), "
                       + "sus conceptos y el total y saldo pendiente calculados como en CU47")
    public ResponseEntity<byte[]> generateBudgetReport(
            @Parameter(description = "ID of the budget")
            @PathVariable("idBudget") @Positive Integer idBudget) {
        return buildPdfResponse("presupuesto_" + idBudget + ".pdf",
                () -> reporteService.generateBudgetReport(idBudget));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF of the budget with properties"),
        @ApiResponse(responseCode = "400", description = "Parámetro invalid", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado al generar el PDF (sin cuerpo)",
                content = @Content)
    })
    @GetMapping(value = "/budget-properties/{idBudget}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate reporte of budget with properties",
               description = "Generates the PDF of the budget e incluye the properties of their procedures "
                       + "(cadastral nomenclature, domicilio and valuación fiscal)")
    public ResponseEntity<byte[]> generateBudgetPropertiesReport(
            @Parameter(description = "ID of the budget")
            @PathVariable("idBudget") @Positive Integer idBudget) {
        return buildPdfResponse("presupuesto_inmuebles_" + idBudget + ".pdf",
                () -> reporteService.generateBudgetPropertiesReport(idBudget));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the documents of the procedure type"),
        @ApiResponse(responseCode = "400", description = "Parámetro invalid", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Procedure type not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado al generar el PDF (sin cuerpo)",
                content = @Content)
    })
    @GetMapping(value = "/procedure-document-list", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate reporte of lista of documents by procedure",
               description = "Generates a PDF with the documents that requires a procedure type according to its "
                       + "template (expiration, days of validez and who the entrega)")
    public ResponseEntity<byte[]> generateProcedureDocumentsListReport(
            @Parameter(description = "Name of the procedure type")
            @RequestParam("procedureTypeName") @NotBlank String nameTypeProcedure) {
        return buildPdfResponse("lista_documentos.pdf",
                () -> reporteService.generateProcedureDocumentsListReport(nameTypeProcedure));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the history of the management"),
        @ApiResponse(responseCode = "400", description = "Parámetro invalid", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Management not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado al generar el PDF (sin cuerpo)",
                content = @Content)
    })
    @GetMapping(value = "/management-history/{idManagement}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate reporte of management history",
               description = "Generates a PDF with the datos of the management and its status history, "
                       + "of the more antiguo to the more reciente")
    public ResponseEntity<byte[]> generateManagementHistoryReport(
            @Parameter(description = "ID of the management")
            @PathVariable("idManagement") @Positive Integer idManagement) {
        return buildPdfResponse("historial_gestion_" + idManagement + ".pdf",
                () -> reporteService.generateManagementHistoryReport(idManagement));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the expiration of the submitted document"),
        @ApiResponse(responseCode = "400", description = "Parámetro invalid", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Submitted document not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado al generar el PDF (sin cuerpo)",
                content = @Content)
    })
    @GetMapping(value = "/documents-due/{idSubmittedDocument}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate reporte of documents by vencer",
               description = "Generates a PDF with the expiration of a submitted document (date and days "
                       + "restantes), its status of payment and the management, procedure and client a the that belongs")
    public ResponseEntity<byte[]> generateDocumentsDueSoonReport(
            @Parameter(description = "ID of the submitted document")
            @PathVariable("idSubmittedDocument") @Positive Integer idSubmittedDocument) {
        return buildPdfResponse("documentos_vencer.pdf",
                () -> reporteService.generateDocumentsDueSoonReport(idSubmittedDocument));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the debt of documents of the management"),
        @ApiResponse(responseCode = "400", description = "Parámetro invalid", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "There is no management with that number", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado al generar el PDF (sin cuerpo)",
                content = @Content)
    })
    @GetMapping(value = "/document-debt", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de consulta de deuda de documentos",
               description = "Generates a PDF with the submitted documents in the managements with that number, "
                       + "su importe, fecha de pago o pago pendiente, y el total adeudado")
    public ResponseEntity<byte[]> generateDebtDocumentsReport(
            @Parameter(description = "Number of management")
            @RequestParam @Positive Integer numberManagement) {
        return buildPdfResponse("deuda_documentos_" + numberManagement + ".pdf",
                () -> reporteService.generateDebtDocumentsReport(numberManagement));
    }

    @GetMapping(value = "/index-book", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar libro de indice",
               description = "Endpoint base for CU24: PDF provisorio with the period solicitado; "
                       + "the contenido of the libro of índice is pendiente")
    public ResponseEntity<byte[]> generateIndexBook(
            @Parameter(description = "Year of the index book")
            @RequestParam("year") @Positive Integer year) {
        return buildPdfResponse("libro_indice_" + year + ".pdf",
                () -> reporteService.generateIndexBookReport(year));
    }

    @GetMapping(value = "/monthly-sworn-statement", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar declaracion jurada mensual",
               description = "Endpoint base for CU25: PDF provisorio with the period solicitado; "
                       + "the contenido of the DDJJ mensual is pendiente")
    public ResponseEntity<byte[]> generateMonthlySwornStatement(
            @Parameter(description = "Year of the period")
            @RequestParam("year") @Positive Integer year,
            @Parameter(description = "Month of the period")
            @RequestParam("month") @Min(1) @Max(12) Integer month) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_mensual_" + year + "_" + month + ".pdf",
                () -> reporteService.generateMonthlyTaxDeclarationReport(year, month));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "The testimony is not verified"),
    @ApiResponse(responseCode = "404", description = "Testimony not found")
})
    @GetMapping(value = "/testimonies/{idTestimony}/copies", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate copy impresa of testimony verified",
               description = "Generates a PDF with the copy impresa of a testimony, only if already was verified")
    public ResponseEntity<byte[]> generateTestimonyCopyReport(
            @Parameter(description = "ID of the testimony")
            @PathVariable("idTestimony") @Positive Integer idTestimony) {
        byte[] pdfBytes = reporteService.generateTestimonyCopyReport(idTestimony);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"testimonio_" + idTestimony + "_copia.pdf\"")
                .body(pdfBytes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Registration draft not found")
    })
    @GetMapping(value = "/registration-draft/{idRegistrationDraft}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "CU82 - Generate the formulario normalizado of the registration draft",
               description = "Generates a PDF with the formulario normalizado of a registration draft")
    public ResponseEntity<byte[]> generateRegistrationDraftReport(
            @Parameter(description = "ID of the registration draft")
            @PathVariable("idRegistrationDraft") @Positive Integer idRegistrationDraft) {
        byte[] pdfBytes = reporteService.generateRegistrationDraftReport(idRegistrationDraft);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"minuta_inscripcion_" + idRegistrationDraft + ".pdf\"")
                .body(pdfBytes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping(value = "/payment-receipt/{idPayment}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "CU15 - Emitir recibo de pago",
               description = "Genera un PDF con el recibo de un pago existente")
    public ResponseEntity<byte[]> generatePaymentReceiptReport(
            @Parameter(description = "ID of the payment")
            @PathVariable("idPayment") @Positive Integer idPayment) {
        byte[] pdfBytes = reporteService.generatePaymentReceiptReport(idPayment);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recibo_pago_" + idPayment + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping(value = "/tax-sworn-statement", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar declaracion jurada de rentas",
               description = "Endpoint base for CU50: PDF provisorio with the period solicitado; "
                       + "the contenido of the DDJJ of rentas is pendiente")
    public ResponseEntity<byte[]> generateTaxSwornStatement(
            @Parameter(description = "Year of the period")
            @RequestParam("year") @Positive Integer year,
            @Parameter(description = "Month of the period")
            @RequestParam("month") @Min(1) @Max(12) Integer month) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_rentas_" + year + "_" + month + ".pdf",
                () -> reporteService.generateIncomeTaxDeclarationReport(year, month));
    }
}
