package com.licensis.notaire.api;

import com.licensis.notaire.servicios.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para generación de reportes
 * Expone endpoints para generar PDFs desde JasperReports
 */
@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "API para generación de reportes PDF")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping(value = "/presupuesto/{idPresupuesto}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de presupuesto",
               description = "Genera un PDF con el presupuesto especificado")
    public ResponseEntity<byte[]> generarReportePresupuesto(
            @Parameter(description = "ID del presupuesto")
            @PathVariable Integer idPresupuesto) {
        try {
            byte[] pdfBytes = reporteService.generarReportePresupuesto(idPresupuesto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"presupuesto_" + idPresupuesto + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/presupuesto-inmuebles/{idPresupuesto}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de presupuesto con inmuebles",
               description = "Genera un PDF con el presupuesto e información de inmuebles")
    public ResponseEntity<byte[]> generarReportePresupuestoInmuebles(
            @Parameter(description = "ID del presupuesto")
            @PathVariable Integer idPresupuesto) {
        try {
            byte[] pdfBytes = reporteService.generarReportePresupuestoInmuebles(idPresupuesto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"presupuesto_inmuebles_" + idPresupuesto + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/lista-documentos-tramite", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de lista de documentos por trámite",
               description = "Genera un PDF con la lista de documentos requeridos para un tipo de trámite")
    public ResponseEntity<byte[]> generarReporteListaDocumentosTramite(
            @Parameter(description = "Nombre del tipo de trámite")
            @RequestParam String nombreTipoTramite) {
        try {
            byte[] pdfBytes = reporteService.generarReporteListaDocumentosTramite(nombreTipoTramite);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"lista_documentos.pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/historial-gestion/{idGestion}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de historial de gestión",
               description = "Genera un PDF con el historial de una gestión específica")
    public ResponseEntity<byte[]> generarReporteHistorialGestion(
            @Parameter(description = "ID de la gestión")
            @PathVariable Integer idGestion) {
        try {
            byte[] pdfBytes = reporteService.generarReporteHistorialGestion(idGestion);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"historial_gestion_" + idGestion + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/documentos-por-vencer/{idDocumentoPresentado}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de documentos por vencer",
               description = "Genera un PDF con información de documentos próximos a vencer")
    public ResponseEntity<byte[]> generarReporteDocumentosPorVencer(
            @Parameter(description = "ID del documento presentado")
            @PathVariable Integer idDocumentoPresentado) {
        try {
            byte[] pdfBytes = reporteService.generarReporteDocumentosPorVencer(idDocumentoPresentado);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"documentos_vencer.pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/consultar-deuda-documentos", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de consulta de deuda de documentos",
               description = "Genera un PDF con la consulta de deuda de documentos para una gestión")
    public ResponseEntity<byte[]> generarReporteConsultarDeudaDocumentos(
            @Parameter(description = "Número de gestión")
            @RequestParam Integer numeroGestion) {
        try {
            byte[] pdfBytes = reporteService.generarReporteConsultarDeudaDocumentos(numeroGestion);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"deuda_documentos_" + numeroGestion + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
