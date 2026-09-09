package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

@DisplayName("Pago REST integration tests — CU15 paymentMethod persistence")
class PaymentIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private Integer idBudget;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        IdentificationType identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationTypeRepository.save(identificationType);

        Person person = new Person();
        person.setFirstName("Cliente");
        person.setLastName("Test");
        person.setIdentificationNumber("87654321");
        person.setIsClient(true);
        person.setFkIdIdentificationType(identificationType);
        person = personRepository.save(person);

        Budget budget = new Budget();
        budget.setNumber((int) (System.currentTimeMillis() % 10000));
        budget.setDate(new Date());
        budget.setEncabezado("Presupuesto Test");
        budget.setStatus("PENDIENTE");
        budget.setPropertyAmount(500000f);
        budget.setFkIdPerson(person);
        budget = budgetRepository.save(budget);
        idBudget = budget.getIdBudget();
    }

    @Test
    @DisplayName("Should return the same paymentMethod when processing a payment")
    void shouldReturnPaymentMethodOnCreate() throws Exception {
        String json = """
                {
                    "idBudget": %d,
                    "amount": 100000.0,
                    "date": "2026-08-20",
                    "notes": "Primer payment",
                    "paymentMethod": "Efectivo"
                }
                """.formatted(idBudget);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("Efectivo"))
                .andExpect(jsonPath("$.idBudget").value(idBudget));
    }

    @Test
    @DisplayName("Should reflect the stored paymentMethod when retrieving a payment by ID")
    void shouldReturnStoredPaymentMethodOnGetById() throws Exception {
        String json = """
                {
                    "idBudget": %d,
                    "amount": 100000.0,
                    "date": "2026-08-20",
                    "notes": "Primer payment",
                    "paymentMethod": "Transferencia"
                }
                """.formatted(idBudget);

        MvcResult created = mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Integer idPayment = mapper.readTree(created.getResponse().getContentAsString())
                .get("idPayment").asInt();

        mockMvc.perform(get("/api/v1/pagos/" + idPayment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("Transferencia"));
    }

    @Test
    @DisplayName("Should persist an updated paymentMethod after editing a payment")
    void shouldPersistUpdatedPaymentMethod() throws Exception {
        String createJson = """
                {
                    "idBudget": %d,
                    "amount": 100000.0,
                    "date": "2026-08-20",
                    "notes": "Primer payment",
                    "paymentMethod": "Efectivo"
                }
                """.formatted(idBudget);

        MvcResult created = mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer idPayment = mapper.readTree(created.getResponse().getContentAsString())
                .get("idPayment").asInt();

        String updateJson = """
                {
                    "amount": 120000.0,
                    "date": "2026-08-20",
                    "notes": "Editado",
                    "paymentMethod": "Cheque"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/" + idPayment)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("Cheque"));

        mockMvc.perform(get("/api/v1/pagos/" + idPayment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("Cheque"));
    }
}
