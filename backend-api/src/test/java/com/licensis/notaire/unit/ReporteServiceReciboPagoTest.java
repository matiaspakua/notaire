package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.service.ReporteService;
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

@DisplayName("ReporteService recibo de pago unit tests (CU15/RF-21, issue #23)")
@ExtendWith(MockitoExtension.class)
class ReporteServiceReciboPagoTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ItemRepository itemRepository;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(dataSource, null, null, null, pagoRepository, itemRepository);
    }

    private Pago buildPago(Integer idPago, float monto, Persona cliente) {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(10);
        presupuesto.setFkIdPersona(cliente);

        Pago pago = new Pago();
        pago.setIdPago(idPago);
        pago.setMonto(monto);
        pago.setFecha(new Date());
        pago.setPresupuesto(presupuesto);
        return pago;
    }

    private Persona buildCliente() {
        Persona persona = new Persona();
        persona.setNombre("Ana");
        persona.setApellido("Gomez");
        return persona;
    }

    @Test
    @DisplayName("Should generate a PDF recibo with cliente, fecha, concepto and total for a simple pago")
    void shouldGenerarReciboConDatosDelPago() throws Exception {
        Persona cliente = buildCliente();
        Pago pago = buildPago(1, 500000f, cliente);

        Item item = new Item();
        item.setNombre("Escritura de compraventa");

        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago));
        when(itemRepository.findByFkIdPresupuestoIdPresupuesto(10)).thenReturn(List.of(item));

        byte[] pdf = reporteService.generarReporteReciboPago(1);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(5, pdf.length))).startsWith("%PDF-");
        String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
        assertThat(content).contains("Ana Gomez");
        assertThat(content).contains("Escritura de compraventa");
        assertThat(content).contains("500000");
    }

    @Test
    @DisplayName("Should print the monto of a partial/installment pago, not the presupuesto total")
    void shouldGenerarReciboParaPagoParcial() throws Exception {
        Persona cliente = buildCliente();
        Pago pagoParcial = buildPago(2, 100000f, cliente);

        when(pagoRepository.findById(2)).thenReturn(Optional.of(pagoParcial));
        when(itemRepository.findByFkIdPresupuestoIdPresupuesto(10)).thenReturn(List.of());

        byte[] pdf = reporteService.generarReporteReciboPago(2);

        String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
        assertThat(content).contains("100000");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when idPago does not exist")
    void shouldThrowWhenPagoNoExiste() {
        when(pagoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.generarReporteReciboPago(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
