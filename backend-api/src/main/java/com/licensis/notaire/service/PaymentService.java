package com.licensis.notaire.service;

import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.repository.BudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BudgetRepository budgetRepository;

    public PaymentService(PaymentRepository paymentRepository, BudgetRepository budgetRepository) {
        this.paymentRepository = paymentRepository;
        this.budgetRepository = budgetRepository;
    }

    /**
     * CU15 - Procesar pago: Registra un nuevo pago para un presupuesto.
     * Calcula el saldo pendiente y valida que el monto no exceda el total.
     */
    @Transactional
    public Payment processPayment(Integer idBudget, Float amount, Date date, String notes) {
        return processPayment(idBudget, amount, date, notes, null);
    }

    /**
     * CU15 - Procesar pago: Registra un nuevo pago para un presupuesto.
     * Calcula el saldo pendiente y valida que el monto no exceda el total.
     */
    @Transactional
    public Payment processPayment(Integer idBudget, Float amount, Date date, String notes,
            String paymentMethod) {
        log.info("Procesando pago para presupuesto {}: monto={}", idBudget, amount);

        Budget budget = budgetRepository.findById(idBudget)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idBudget));

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }

        Float pendingBalance = calculatePendingBalance(idBudget);
        log.info("Saldo pendiente para presupuesto {}: {}", idBudget, pendingBalance);

        if (amount > pendingBalance) {
            throw new PendingBalanceExceededException(
                    String.format("El monto del pago ($%.2f) no puede exceder el saldo pendiente ($%.2f)",
                            amount, pendingBalance));
        }

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setDate(date != null ? date : new Date());
        payment.setNotes(notes);
        payment.setPaymentMethod(paymentMethod);
        payment.setBudget(budget);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago registrado exitosamente: ID={}", savedPayment.getIdPayment());

        return savedPayment;
    }

    /**
     * CU47 - Consultar Pago: Obtiene un pago por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getPayment(Integer idPayment) {
        log.debug("Consultando pago con ID: {}", idPayment);
        return paymentRepository.findById(idPayment);
    }

    /**
     * Obtiene todos los pagos de un presupuesto.
     */
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByBudget(Integer idBudget) {
        return paymentRepository.findByFkIdBudgetIdBudget(idBudget);
    }

    /**
     * Calcula el saldo pendiente de un presupuesto.
     */
    @Transactional(readOnly = true)
    public Float calculatePendingBalance(Integer idBudget) {
        Budget budget = budgetRepository.findById(idBudget)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idBudget));

        Float totalBudget = calculateBudgetTotal(budget);
        Float totalPaid = paymentRepository.sumAmountByBudgetId(idBudget);
        Float pendingBalance = totalBudget - (totalPaid != null ? totalPaid : 0f);

        log.debug("Saldo pendiente para presupuesto {}: {}", idBudget, pendingBalance);
        return pendingBalance;
    }

    /**
     * CU15/CU47 - Calcula el estado de pago agregado de un presupuesto (Issue #821):
     * SIN_PAGOS si no se registró ningún pago, PAID si el saldo pendiente es cero,
     * PARTIAL en cualquier otro caso.
     */
    @Transactional(readOnly = true)
    public StatusPayment calculatePaymentStatus(Integer idBudget) {
        budgetRepository.findById(idBudget)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idBudget));

        Float totalPaid = paymentRepository.sumAmountByBudgetId(idBudget);
        if (totalPaid == null || totalPaid == 0f) {
            return StatusPayment.NoPayments;
        }

        Float pendingBalance = calculatePendingBalance(idBudget);
        return pendingBalance <= 0f ? StatusPayment.PAID : StatusPayment.PARTIAL;
    }

    /**
     * CU45 - Calcula el total de un presupuesto sumando los items normales y de recargo,
     * restando los items de descuento, y sumando los costos de documentos presentados
     * en sus trámites (Issue #823).
     */
    private Float calculateBudgetTotal(Budget budget) {
        float total;
        if (budget.getItemList() == null || budget.getItemList().isEmpty()) {
            total = budget.getPropertyAmount() != null ? budget.getPropertyAmount() : 0f;
        } else {
            total = 0f;
            for (Item item : budget.getItemList()) {
                total += item.getType() == TypeItem.DESCUENTO ? -item.getValue() : item.getValue();
                if (item.getPercentage() != null && item.getPercentage() > 0) {
                    total += total * (item.getPercentage() / 100.0f);
                }
            }
        }
        return total + sumSubmittedDocumentCosts(budget);
    }

    private float sumSubmittedDocumentCosts(Budget budget) {
        if (budget.getProcedureList() == null) {
            return 0f;
        }
        float total = 0f;
        for (var procedure : budget.getProcedureList()) {
            if (procedure.getSubmittedDocumentList() == null) {
                continue;
            }
            for (var document : procedure.getSubmittedDocumentList()) {
                if (document.getAmountToPay() != null) {
                    total += document.getAmountToPay();
                }
            }
        }
        return total;
    }

    /**
     * Obtiene todos los pagos.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    /**
     * Obtiene pagos en un rango de fechas.
     */
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByDateRange(Date startDate, Date endDate) {
        return paymentRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * Elimina un pago por su ID.
     */
    @Transactional
    public void deletePayment(Integer idPayment) {
        log.info("Eliminando pago con ID: {}", idPayment);
        if (!paymentRepository.existsById(idPayment)) {
            throw new IllegalArgumentException("Pago no encontrado con ID: " + idPayment);
        }
        paymentRepository.deleteById(idPayment);
        log.info("Pago eliminado exitosamente: ID={}", idPayment);
    }

    @Transactional
    public Payment editPayment(Integer idPayment, Float amount, Date date, String notes) {
        return editPayment(idPayment, amount, date, notes, null);
    }

    /**
     * Edita un pago existente.
     */
    @Transactional
    public Payment editPayment(Integer idPayment, Float amount, Date date, String notes, String paymentMethod) {
        log.info("Editando pago con ID: {}", idPayment);
        Payment payment = paymentRepository.findById(idPayment)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + idPayment));

        if (amount != null) {
            if (amount <= 0) {
                throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
            }
            payment.setAmount(amount);
        }
        if (date != null) {
            payment.setDate(date);
        }
        if (notes != null) {
            payment.setNotes(notes);
        }
        if (paymentMethod != null) {
            payment.setPaymentMethod(paymentMethod);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago editado exitosamente: ID={}", savedPayment.getIdPayment());
        return savedPayment;
    }
}
