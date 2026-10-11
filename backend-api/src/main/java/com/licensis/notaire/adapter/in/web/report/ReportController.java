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
 * REST controller for PDF report generation.
 *
 * <p>Reports are generated internally with Apache PDFBox from the current domain
 * model (issue #567); JasperReports templates are no longer used.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Validated
@Tag(name = "Reports", description = "API for generating PDF reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
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
        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error generating the PDF (no body)",
                content = @Content)
    })
    @GetMapping(value = "/budget/{idBudget}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the budget report",
               description = "Generates a PDF with the budget data (client, procedures, management), "
                       + "its concepts, and the total and pending balance calculated as in CU47")
    public ResponseEntity<byte[]> generateBudgetReport(
            @Parameter(description = "ID of the budget")
            @PathVariable("idBudget") @Positive Integer idBudget) {
        return buildPdfResponse("presupuesto_" + idBudget + ".pdf",
                () -> reportService.generateBudgetReport(idBudget));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF of the budget with properties"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error generating the PDF (no body)",
                content = @Content)
    })
    @GetMapping(value = "/budget-properties/{idBudget}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the budget report with properties",
               description = "Generates the budget PDF and includes the properties of its procedures "
                       + "(cadastral designation, address, and fiscal appraisal)")
    public ResponseEntity<byte[]> generateBudgetPropertiesReport(
            @Parameter(description = "ID of the budget")
            @PathVariable("idBudget") @Positive Integer idBudget) {
        return buildPdfResponse("presupuesto_inmuebles_" + idBudget + ".pdf",
                () -> reportService.generateBudgetPropertiesReport(idBudget));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the documents of the procedure type"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Procedure type not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error generating the PDF (no body)",
                content = @Content)
    })
    @GetMapping(value = "/procedure-document-list", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the document list report for a procedure",
               description = "Generates a PDF with the documents a procedure type requires according to its "
                       + "template (expiration, validity days, and who delivers them)")
    public ResponseEntity<byte[]> generateProcedureDocumentsListReport(
            @Parameter(description = "Name of the procedure type")
            @RequestParam("procedureTypeName") @NotBlank String nameTypeProcedure) {
        return buildPdfResponse("lista_documentos.pdf",
                () -> reportService.generateProcedureDocumentsListReport(nameTypeProcedure));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the history of the management"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Management not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error generating the PDF (no body)",
                content = @Content)
    })
    @GetMapping(value = "/management-history/{idManagement}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the management history report",
               description = "Generates a PDF with the management data and its status history, "
                       + "from the oldest to the most recent")
    public ResponseEntity<byte[]> generateManagementHistoryReport(
            @Parameter(description = "ID of the management")
            @PathVariable("idManagement") @Positive Integer idManagement) {
        return buildPdfResponse("historial_gestion_" + idManagement + ".pdf",
                () -> reportService.generateManagementHistoryReport(idManagement));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the expiration of the submitted document"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Submitted document not found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error generating the PDF (no body)",
                content = @Content)
    })
    @GetMapping(value = "/documents-due/{idSubmittedDocument}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the documents-due report",
               description = "Generates a PDF with the expiration of a submitted document (date and days "
                       + "remaining), its payment status, and the management, procedure, and client it belongs to")
    public ResponseEntity<byte[]> generateDocumentsDueSoonReport(
            @Parameter(description = "ID of the submitted document")
            @PathVariable("idSubmittedDocument") @Positive Integer idSubmittedDocument) {
        return buildPdfResponse("documentos_vencer.pdf",
                () -> reportService.generateDocumentsDueSoonReport(idSubmittedDocument));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF with the debt of documents of the management"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "There is no management with that number", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error generating the PDF (no body)",
                content = @Content)
    })
    @GetMapping(value = "/document-debt", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the document-debt report",
               description = "Generates a PDF with the submitted documents in the managements with that number, "
                       + "its amount, payment date or pending payment, and the total owed")
    public ResponseEntity<byte[]> generateDebtDocumentsReport(
            @Parameter(description = "Number of management")
            @RequestParam @Positive Integer numberManagement) {
        return buildPdfResponse("deuda_documentos_" + numberManagement + ".pdf",
                () -> reportService.generateDebtDocumentsReport(numberManagement));
    }

    @GetMapping(value = "/index-book", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the index book",
               description = "Base endpoint for CU24: a provisional PDF for the requested period; "
                       + "the index-book content is still pending")
    public ResponseEntity<byte[]> generateIndexBook(
            @Parameter(description = "Year of the index book")
            @RequestParam("year") @Positive Integer year) {
        return buildPdfResponse("libro_indice_" + year + ".pdf",
                () -> reportService.generateIndexBookReport(year));
    }

    @GetMapping(value = "/monthly-sworn-statement", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the monthly sworn statement",
               description = "Base endpoint for CU25: a provisional PDF for the requested period; "
                       + "the monthly sworn-statement content is still pending")
    public ResponseEntity<byte[]> generateMonthlySwornStatement(
            @Parameter(description = "Year of the period")
            @RequestParam("year") @Positive Integer year,
            @Parameter(description = "Month of the period")
            @RequestParam("month") @Min(1) @Max(12) Integer month) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_mensual_" + year + "_" + month + ".pdf",
                () -> reportService.generateMonthlyTaxDeclarationReport(year, month));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "The testimony is not verified"),
    @ApiResponse(responseCode = "404", description = "Testimony not found")
})
    @GetMapping(value = "/testimonies/{idTestimony}/copies", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the printed copy of a verified testimony",
               description = "Generates a PDF with the printed copy of a testimony, only when it has been verified")
    public ResponseEntity<byte[]> generateTestimonyCopyReport(
            @Parameter(description = "ID of the testimony")
            @PathVariable("idTestimony") @Positive Integer idTestimony) {
        byte[] pdfBytes = reportService.generateTestimonyCopyReport(idTestimony);
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
    @Operation(summary = "CU82 - Generate the standard form of the registration draft",
               description = "Generates a PDF with the standard form of a registration draft")
    public ResponseEntity<byte[]> generateRegistrationDraftReport(
            @Parameter(description = "ID of the registration draft")
            @PathVariable("idRegistrationDraft") @Positive Integer idRegistrationDraft) {
        byte[] pdfBytes = reportService.generateRegistrationDraftReport(idRegistrationDraft);
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
    @Operation(summary = "CU15 - Issue a payment receipt",
               description = "Generates a PDF with the receipt of an existing payment")
    public ResponseEntity<byte[]> generatePaymentReceiptReport(
            @Parameter(description = "ID of the payment")
            @PathVariable("idPayment") @Positive Integer idPayment) {
        byte[] pdfBytes = reportService.generatePaymentReceiptReport(idPayment);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recibo_pago_" + idPayment + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping(value = "/tax-sworn-statement", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the tax sworn statement",
               description = "Base endpoint for CU50: a provisional PDF for the requested period; "
                       + "the tax sworn-statement content is still pending")
    public ResponseEntity<byte[]> generateTaxSwornStatement(
            @Parameter(description = "Year of the period")
            @RequestParam("year") @Positive Integer year,
            @Parameter(description = "Month of the period")
            @RequestParam("month") @Min(1) @Max(12) Integer month) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_rentas_" + year + "_" + month + ".pdf",
                () -> reportService.generateIncomeTaxDeclarationReport(year, month));
    }
}
