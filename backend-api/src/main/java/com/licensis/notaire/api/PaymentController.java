package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoPaymentResponse;
import com.licensis.notaire.exception.SaldoPendingExcedidoException;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.service.StatusPayment;
import com.licensis.notaire.service.PaymentService;
import com.licensis.notaire.service.mappers.PaymentMapper;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pago", description = "API para gestionar pagos (CU15, CU47)")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los pagos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoPaymentResponse>> getAll() {
        try {
            return ResponseEntity.ok(paymentService.findAll().stream().map(PaymentMapper::toDto).toList());
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
    @Transactional(readOnly = true)
    public ResponseEntity<DtoPaymentResponse> getById(@PathVariable Integer id) {
        try {
            return paymentService.consultarPayment(id)
                    .map(PaymentMapper::toDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al consultar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/presupuesto/{idBudget}")
    @Operation(summary = "Obtener pagos por presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoPaymentResponse>> getByBudget(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(
                    paymentService.findPaymentsByBudget(idBudget).stream().map(PaymentMapper::toDto).toList());
        } catch (Exception e) {
            log.error("Error al obtener pagos por presupuesto ID={}", idBudget, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/presupuesto/{idBudget}/saldo")
    @Operation(summary = "Calcular saldo pendiente de un presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<Float> getSaldoPending(@PathVariable Integer idBudget) {
        try {
            Float saldo = paymentService.calcularSaldoPending(idBudget);
            return ResponseEntity.ok(saldo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al calcular saldo pendiente", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/presupuesto/{idBudget}/estado")
    @Operation(summary = "CU47 - Calcular estado de pago (SIN_PAGOS, PARCIAL, SALDADO) de un presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<StatusPayment> getStatusPayment(@PathVariable Integer idBudget) {
        try {
            StatusPayment status = paymentService.calcularStatusPayment(idBudget);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al calcular estado de pago", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/fecha")
    @Operation(summary = "Obtener pagos por rango de fechas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoPaymentResponse>> getByDateRange(
            @Parameter(description = "Fecha inicio (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Fecha fin (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            return ResponseEntity.ok(
                    paymentService.findPaymentsByDateRange(startDate, endDate).stream().map(PaymentMapper::toDto).toList());
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
    public ResponseEntity<DtoPaymentResponse> procesarPayment(@RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.procesarPayment(
                    request.idBudget(),
                    request.amount(),
                    request.date(),
                    request.notes(),
                    request.paymentMethod()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(PaymentMapper.toDto(payment));
        } catch (SaldoPendingExcedidoException e) {
            log.warn("Pago rechazado por exceder el saldo pendiente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al procesar pago: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al procesar pago", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping("/params")
    @Operation(summary = "CU15 - Procesar pago (query params)")
    public ResponseEntity<DtoPaymentResponse> procesarPaymentParams(
            @Parameter(description = "ID del presupuesto") @RequestParam Integer idBudget,
            @Parameter(description = "Monto del pago") @RequestParam Float amount,
            @Parameter(description = "Fecha de pago (opcional, YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @Parameter(description = "Observaciones") @RequestParam(required = false) String notes,
            @Parameter(description = "Método de pago") @RequestParam(required = false) String paymentMethod) {
        try {
            Payment payment = paymentService.procesarPayment(idBudget, amount, date, notes, paymentMethod);
            return ResponseEntity.status(HttpStatus.CREATED).body(PaymentMapper.toDto(payment));
        } catch (SaldoPendingExcedidoException e) {
            log.warn("Pago rechazado por exceder el saldo pendiente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
    public ResponseEntity<DtoPaymentResponse> update(@PathVariable Integer id, @RequestBody Payment entity) {
        try {
            Payment updated = paymentService.editarPayment(id, entity.getAmount(), entity.getDate(),
                    entity.getNotes(), entity.getPaymentMethod());
            return ResponseEntity.ok(PaymentMapper.toDto(updated));
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
            paymentService.deletePayment(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al eliminar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public record PaymentRequest(
            Integer idBudget,
            Float amount,
            Date date,
            String notes,
            String paymentMethod
    ) {}
}
