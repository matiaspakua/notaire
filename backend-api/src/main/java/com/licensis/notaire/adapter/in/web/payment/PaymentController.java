package com.licensis.notaire.adapter.in.web.payment;

import com.licensis.notaire.application.port.in.payment.DeletePaymentUseCase;
import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.application.port.in.payment.EditPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.adapter.in.web.support.CreatedResponses;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.domain.payment.PaymentStatus;
import com.licensis.notaire.dto.DtoPaymentResponse;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

import java.math.BigDecimal;
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
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "API for managing payments (CU15, CU47)")
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
    @Operation(summary = "Get all payments")
    public ResponseEntity<List<DtoPaymentResponse>> getAll() {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDtoList(queryPaymentsUseCase.findAll()));
        } catch (Exception e) {
            log.error("Failed to list payments", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU47 - Query payment by ID")
    public ResponseEntity<DtoPaymentResponse> getById(@PathVariable Integer id) {
        try {
            return queryPaymentsUseCase.findById(id)
                    .map(PaymentWebMapper::toDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to look up payment ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/budget/{idBudget}")
    @Operation(summary = "Get payments by budget")
    public ResponseEntity<List<DtoPaymentResponse>> getByBudget(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDtoList(queryPaymentsUseCase.findByBudget(idBudget)));
        } catch (Exception e) {
            log.error("Failed to list payments for budget ID={}", idBudget, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/budget/{idBudget}/balance")
    @Operation(summary = "Calculate outstanding balance of a budget")
    public ResponseEntity<BigDecimal> getPendingBalance(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(paymentStatusUseCase.pendingBalance(idBudget));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to calculate the pending balance", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/budget/{idBudget}/status")
    @Operation(summary = "CU47 - Calculate status of payment (SIN_PAGOS, PARCIAL, SALDADO) of a budget")
    public ResponseEntity<PaymentStatus> getStatusPayment(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(paymentStatusUseCase.status(idBudget));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to calculate the payment status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/date")
    @Operation(summary = "Get payments by date range")
    public ResponseEntity<List<DtoPaymentResponse>> getByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            return ResponseEntity.ok(
                    PaymentWebMapper.toDtoList(queryPaymentsUseCase.findByDateRange(startDate, endDate)));
        } catch (Exception e) {
            log.error("Failed to list payments by date", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "CU15 - Process a payment (JSON body)")
    public ResponseEntity<DtoPaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return register(new ProcessPaymentCommand(
                request.idBudget(), request.amount(), request.date(),
                request.notes(), request.paymentMethod()));
    }

    private ResponseEntity<DtoPaymentResponse> register(ProcessPaymentCommand command) {
        try {
            PaymentDetails payment = processPaymentUseCase.process(command);
            return CreatedResponses.of(PaymentWebMapper.toDto(payment), "/api/v1/payments", payment.id());
        } catch (PendingBalanceExceededException e) {
            log.warn("Payment rejected because it exceeds the outstanding balance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Validation error while processing the payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to process the payment", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Edit a payment")
    public ResponseEntity<DtoPaymentResponse> update(@PathVariable Integer id,
            @Valid @RequestBody PaymentUpdateRequest request) {
        try {
            PaymentDetails updated = editPaymentUseCase.edit(new EditPaymentCommand(
                    id, request.amount(), request.date(), request.notes(), request.paymentMethod()));
            return ResponseEntity.ok(PaymentWebMapper.toDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to update payment ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            deletePaymentUseCase.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete payment ID={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request body of {@code POST /api/v1/payments} (CU15).
     */
    public record PaymentRequest(
            @NotNull Integer idBudget,
            @NotNull @Positive BigDecimal amount,
            Date date,
            String notes,
            String paymentMethod
    ) {
        public PaymentRequest {
            date = date == null ? null : new Date(date.getTime());
        }

        @Override
        public Date date() {
            return date == null ? null : new Date(date.getTime());
        }
    }

    /**
     * Request body of {@code PUT /api/v1/payments/{id}}. Replaces the previous use of the
     * JPA {@code Payment} entity as a request body; the accepted JSON fields are the
     * same, and any null field leaves the stored value untouched.
     */
    public record PaymentUpdateRequest(
            @Positive BigDecimal amount,
            Date date,
            String notes,
            String paymentMethod
    ) {
        public PaymentUpdateRequest {
            date = date == null ? null : new Date(date.getTime());
        }

        @Override
        public Date date() {
            return date == null ? null : new Date(date.getTime());
        }
    }
}
