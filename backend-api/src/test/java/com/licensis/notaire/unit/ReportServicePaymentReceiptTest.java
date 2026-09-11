package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("ReportService recibo de pago unit tests (CU15/RF-21, issue #23)")
@ExtendWith(MockitoExtension.class)
class ReportServiceReciboPaymentTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ItemRepository itemRepository;

    private ReportService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReportService(dataSource, null, null, null, paymentRepository, itemRepository);
    }

    private Payment buildPayment(Integer idPayment, float amount, Person client) {
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
    @DisplayName("Should generate a PDF recibo with cliente, date, concepto and total for a simple pago")
    void shouldGenerarReciboConDatosDelPayment() throws Exception {
        Person client = buildClient();
        Payment payment = buildPayment(1, 500000f, client);

        Item item = new Item();
        item.setName("Escritura de compraventa");

        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(itemRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of(item));

        byte[] pdf = reporteService.generatePaymentReceiptReport(1);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(5, pdf.length))).startsWith("%PDF-");
        String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
        assertThat(content).contains("Ana Gomez");
        assertThat(content).contains("Escritura de compraventa");
        assertThat(content).contains("500000");
    }

    @Test
    @DisplayName("Should print the amount of a partial/installment pago, not the budget total")
    void shouldGenerarReciboParaPaymentParcial() throws Exception {
        Person client = buildClient();
        Payment paymentParcial = buildPayment(2, 100000f, client);

        when(paymentRepository.findById(2)).thenReturn(Optional.of(paymentParcial));
        when(itemRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of());

        byte[] pdf = reporteService.generatePaymentReceiptReport(2);

        String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
        assertThat(content).contains("100000");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when idPayment does not exist")
    void shouldThrowWhenPaymentNoExiste() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.generatePaymentReceiptReport(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
