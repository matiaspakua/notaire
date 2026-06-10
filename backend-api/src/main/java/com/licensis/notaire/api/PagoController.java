package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pago", description = "API para gestionar pagos (CU15, CU47)")
public class PagoController {

    private static final Logger log = LoggerFactory.getLogger(PagoController.class);

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los pagos")
    public ResponseEntity<List<Pago>> getAll() {
        try {
            return ResponseEntity.ok(pagoService.findAll());
        } catch (Exception e) {
            log.error("Error al obtener pagos", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "CU47 - Consultar pago por ID")
    public ResponseEntity<Pago> getById(@PathVariable Integer id) {
        try {
            return pagoService.consultarPago(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al consultar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/presupuesto/{idPresupuesto}")
    @Operation(summary = "Obtener pagos por presupuesto")
    public ResponseEntity<List<Pago>> getByPresupuesto(@PathVariable Integer idPresupuesto) {
        try {
            return ResponseEntity.ok(pagoService.findPagosByPresupuesto(idPresupuesto));
        } catch (Exception e) {
            log.error("Error al obtener pagos por presupuesto ID={}", idPresupuesto, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/presupuesto/{idPresupuesto}/saldo")
    @Operation(summary = "Calcular saldo pendiente de un presupuesto")
    public ResponseEntity<Float> getSaldoPendiente(@PathVariable Integer idPresupuesto) {
        try {
            Float saldo = pagoService.calcularSaldoPendiente(idPresupuesto);
            return ResponseEntity.ok(saldo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al calcular saldo pendiente", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/fecha")
    @Operation(summary = "Obtener pagos por rango de fechas")
    public ResponseEntity<List<Pago>> getByFechaRange(
            @Parameter(description = "Fecha inicio (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Fecha fin (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            return ResponseEntity.ok(pagoService.findPagosByFechaRange(startDate, endDate));
        } catch (Exception e) {
            log.error("Error al obtener pagos por fecha", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "CU15 - Procesar pago (JSON body)")
    public ResponseEntity<Pago> procesarPago(@RequestBody PagoRequest request) {
        try {
            Pago pago = pagoService.procesarPago(
                    request.idPresupuesto(),
                    request.monto(),
                    request.fecha(),
                    request.observaciones()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(pago);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al procesar pago: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al procesar pago", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/params")
    @Operation(summary = "CU15 - Procesar pago (query params)")
    public ResponseEntity<Pago> procesarPagoParams(
            @Parameter(description = "ID del presupuesto") @RequestParam Integer idPresupuesto,
            @Parameter(description = "Monto del pago") @RequestParam Float monto,
            @Parameter(description = "Fecha de pago (opcional, YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fecha,
            @Parameter(description = "Observaciones") @RequestParam(required = false) String observaciones) {
        try {
            Pago pago = pagoService.procesarPago(idPresupuesto, monto, fecha, observaciones);
            return ResponseEntity.status(HttpStatus.CREATED).body(pago);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al procesar pago: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al procesar pago", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Editar pago")
    public ResponseEntity<Pago> update(@PathVariable Integer id, @RequestBody Pago entity) {
        try {
            Pago updated = pagoService.editarPago(id, entity.getMonto(), entity.getFecha(), entity.getObservaciones());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al actualizar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pago")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            pagoService.deletePago(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al eliminar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public record PagoRequest(
            Integer idPresupuesto,
            Float monto,
            Date fecha,
            String observaciones
    ) {}
}
