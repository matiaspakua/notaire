package com.licensis.notaire.adapter.in.web.payment;

import com.licensis.notaire.application.port.in.payment.DeletePaymentUseCase;
import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.application.port.in.payment.EditPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.domain.payment.PaymentStatus;
import com.licensis.notaire.dto.DtoPaymentResponse;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.Date;
import java.util.List;

/**
 * Inbound web adapter for the payment slice (CU15, CU47).
 *
 * <p>It only translates: HTTP in, use-case command out, DTO back. Every business rule
 * lives behind the inbound ports, so this class holds no transaction, no repository and
 * no entity. The URLs, status codes and JSON shapes are unchanged from the previous
 * layered implementation.
 */
@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pago", description = "API para gestionar pagos (CU15, CU47)")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final EditPaymentUseCase editPaymentUseCase;
    private final DeletePaymentUseCase deletePaymentUseCase;
    private final QueryPaymentsUseCase queryPaymentsUseCase;
    private final GetPaymentStatusUseCase paymentStatusUseCase;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase,
            EditPaymentUseCase editPaymentUseCase,
            DeletePaymentUseCase deletePaymentUseCase,
            QueryPaymentsUseCase queryPaymentsUseCase,
            GetPaymentStatusUseCase paymentStatusUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.editPaymentUseCase = editPaymentUseCase;
        this.deletePaymentUseCase = deletePaymentUseCase;
        this.queryPaymentsUseCase = queryPaymentsUseCase;
        this.paymentStatusUseCase = paymentStatusUseCase;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los pagos")
    public ResponseEntity<List<DtoPaymentResponse>> getAll() {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDtoList(queryPaymentsUseCase.findAll()));
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
    public ResponseEntity<DtoPaymentResponse> getById(@PathVariable Integer id) {
        try {
            return queryPaymentsUseCase.findById(id)
                    .map(PaymentWebMapper::toDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al consultar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/presupuesto/{idBudget}")
    @Operation(summary = "Obtener pagos por presupuesto")
    public ResponseEntity<List<DtoPaymentResponse>> getByBudget(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDtoList(queryPaymentsUseCase.findByBudget(idBudget)));
        } catch (Exception e) {
            log.error("Error al obtener pagos por presupuesto ID={}", idBudget, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/presupuesto/{idBudget}/saldo")
    @Operation(summary = "Calcular saldo pendiente de un presupuesto")
    public ResponseEntity<Float> getSaldoPending(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(paymentStatusUseCase.pendingBalance(idBudget));
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
    public ResponseEntity<PaymentStatus> getStatusPayment(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(paymentStatusUseCase.status(idBudget));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al calcular estado de pago", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/fecha")
    @Operation(summary = "Obtener pagos por rango de fechas")
    public ResponseEntity<List<DtoPaymentResponse>> getByDateRange(
            @Parameter(description = "Fecha inicio (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Fecha fin (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            return ResponseEntity.ok(
                    PaymentWebMapper.toDtoList(queryPaymentsUseCase.findByDateRange(startDate, endDate)));
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
    public ResponseEntity<DtoPaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return register(new ProcessPaymentCommand(
                request.idBudget(), request.amount(), request.date(),
                request.notes(), request.paymentMethod()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    @PostMapping("/params")
    @Operation(summary = "CU15 - Procesar pago (query params)")
    public ResponseEntity<DtoPaymentResponse> processPaymentParams(
            @Parameter(description = "ID del presupuesto") @RequestParam Integer idBudget,
            @Parameter(description = "Monto del pago") @RequestParam Float amount,
            @Parameter(description = "Fecha de pago (opcional, YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @Parameter(description = "Observaciones") @RequestParam(required = false) String notes,
            @Parameter(description = "Método de pago") @RequestParam(required = false) String paymentMethod) {
        return register(new ProcessPaymentCommand(idBudget, amount, date, notes, paymentMethod));
    }

    private ResponseEntity<DtoPaymentResponse> register(ProcessPaymentCommand command) {
        try {
            PaymentDetails payment = processPaymentUseCase.process(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(PaymentWebMapper.toDto(payment));
        } catch (PendingBalanceExceededException e) {
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
    public ResponseEntity<DtoPaymentResponse> update(@PathVariable Integer id,
            @RequestBody PaymentUpdateRequest request) {
        try {
            PaymentDetails updated = editPaymentUseCase.edit(new EditPaymentCommand(
                    id, request.amount(), request.date(), request.notes(), request.paymentMethod()));
            return ResponseEntity.ok(PaymentWebMapper.toDto(updated));
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
            deletePaymentUseCase.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al eliminar pago ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request body of {@code POST /api/v1/pagos} (CU15).
     */
    public record PaymentRequest(
            Integer idBudget,
            Float amount,
            Date date,
            String notes,
            String paymentMethod
    ) { }

    /**
     * Request body of {@code PUT /api/v1/pagos/{id}}. Replaces the previous use of the
     * JPA {@code Payment} entity as a request body; the accepted JSON fields are the
     * same, and any null field leaves the stored value untouched.
     */
    public record PaymentUpdateRequest(
            Float amount,
            Date date,
            String notes,
            String paymentMethod
    ) { }
}
