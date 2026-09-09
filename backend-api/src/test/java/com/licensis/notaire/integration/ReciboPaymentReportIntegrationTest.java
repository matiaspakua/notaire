package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU15 - Emitir recibo de pago (issue #23).
 */
@RequirementCoverage({"CU15"})
@DisplayName("ReportController#generarReporteReciboPago — CU15 integration tests")
class ReciboPaymentReportIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createClient() throws Exception {
        String dni = String.valueOf(System.currentTimeMillis() % 1_000_000_000);
        String body = """
                {
                  "firstName": "Ana",
                  "lastName": "Gomez",
                  "dni": "%s",
                  "identificationNumber": "%s",
                  "email": "ana.gomez.%s@example.com",
                  "isClient": true,
                  "fkIdIdentificationType": {"idIdentificationType": 1}
                }
                """.formatted(dni, dni, dni);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private int createBudget(int idPerson) throws Exception {
        String body = """
                {
                  "fkIdPerson": {"personId": %d},
                  "date": "2026-09-05",
                  "encabezado": "Budget recibo E2E",
                  "status": "Pending",
                  "amount": 10000.0
                }
                """.formatted(idPerson);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private void createItem(int idBudget, String name) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "value": 10000.0,
                  "type": "NORMAL",
                  "fkIdBudget": {"idBudget": %d}
                }
                """.formatted(name, idBudget);
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private int createPayment(int idBudget, float amount) throws Exception {
        String body = """
                {
                  "idBudget": %d,
                  "amount": %s,
                  "date": "2026-09-05",
                  "notes": "Payment recibo E2E",
                  "paymentMethod": "Transferencia"
                }
                """.formatted(idBudget, amount);
        MvcResult result = mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPayment").asInt();
    }

    @Test
    @DisplayName("Should return a PDF recibo for an existing pago")
    void shouldReturnPdfForRecibo() throws Exception {
        int idPerson = createClient();
        int idBudget = createBudget(idPerson);
        createItem(idBudget, "Escritura de compraventa");
        int idPayment = createPayment(idBudget, 4000f);

        MvcResult result = mockMvc.perform(get("/api/v1/reportes/recibo-pago/" + idPayment))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 404 for a non-existing pago")
    void shouldReturn404ForNonExistingPayment() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/recibo-pago/999999"))
                .andExpect(status().isNotFound());
    }
}
