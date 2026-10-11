package com.licensis.notaire.unit;


import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.application.port.out.report.ReportRenderer;
import com.licensis.notaire.application.usecase.report.ReportDocumentFactory;
import com.licensis.notaire.application.usecase.report.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("ReportService payment-receipt unit tests (CU15/RF-21, issue #23)")
@ExtendWith(MockitoExtension.class)
class ReportServicePaymentReceiptTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReportDocumentFactory reportDocuments;

    @Mock
    private ReportRenderer reportRenderer;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        // Provide all constructor arguments; mock objects are sufficient for
        // the tests that rely only on paymentRepository and itemRepository.
        reportService = new ReportService(
                null,
                null,
                null,
                paymentRepository,
                itemRepository,
                reportDocuments,
                reportRenderer
        );
    }

    private Payment buildPayment(Integer idPayment, java.math.BigDecimal amount, Person client) {
        Budget budget = new Budget();
        budget.setIdBudget(10);
        budget.setFkIdPerson(client);

        Payment payment = new Payment();
        payment.setIdPayment(idPayment);
        payment.setAmount(amount);
        payment.setDate(new Date());
        payment.setBudget(budget);
        return payment;
    }

    private Person buildClient() {
        Person person = new Person();
        person.setFirstName("Ana");
        person.setLastName("Gomez");
        return person;
    }

    @Test
    @DisplayName("Should generate a PDF receipt with client, date, concept and total for a simple payment")
    void shouldGenerateReceiptWithPaymentData() throws Exception {
        Person client = buildClient();
        Payment payment = buildPayment(1, new java.math.BigDecimal("500000"), client);

        Item item = new Item();
        item.setName("Compraventa deed");

        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(itemRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of(item));

        byte[] pdf = reportService.generatePaymentReceiptReport(1);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(5, pdf.length))).startsWith("%PDF-");
        String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
        assertThat(content).contains("Ana Gomez");
        assertThat(content).contains("Compraventa deed");
        assertThat(content).contains("500000");
    }

    @Test
    @DisplayName("Should print the amount of a partial payment, not the budget total")
    void shouldGenerateReceiptForPartialPayment() throws Exception {
        Person client = buildClient();
        Payment partialPayment = buildPayment(2, new java.math.BigDecimal("100000"), client);

        when(paymentRepository.findById(2)).thenReturn(Optional.of(partialPayment));
        when(itemRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of());

        byte[] pdf = reportService.generatePaymentReceiptReport(2);

        String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
        assertThat(content).contains("100000");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when idPayment does not exist")
    void shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.generatePaymentReceiptReport(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
