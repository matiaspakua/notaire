package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@RequirementCoverage({"CU71"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Presupuesto — agregar ítems existentes del catálogo (CU71)")
class PresupuestoCatalogoItemsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ItemRepository itemRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona() throws Exception {
        String body = """
                {"nombre": "Cliente", "apellido": "Catalogo IT", "numeroIdentificacion": "%s",
                 "esCliente": true, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted("70" + (System.nanoTime() % 1000000));
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createPresupuesto(Integer clienteId) throws Exception {
        String body = """
                {"numero": %d, "fecha": "2026-01-01", "encabezado": "Presupuesto Catalogo IT",
                 "estado": "PENDIENTE", "monto": 1000.00, "persona": {"idPersona": %d}}
                """.formatted((int) (System.nanoTime() % 100000), clienteId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private Item createCatalogItem(String nombre, float valor) {
        Item item = new Item(null, nombre, valor);
        item.setPorcentaje(15);
        item.setObservaciones("Item de catálogo IT");
        return itemRepository.save(item);
    }

    @Test
    @DisplayName("Should add a copy of a catalog item to the presupuesto")
    void shouldAddCatalogItemToPresupuesto() throws Exception {
        Integer clienteId = createPersona();
        Integer presupuestoId = createPresupuesto(clienteId);
        Item catalogItem = createCatalogItem("Sellado IT", 500f);

        mockMvc.perform(post("/api/v1/presupuestos/" + presupuestoId + "/items-desde-catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(List.of(catalogItem.getIdItem()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Sellado IT"))
                .andExpect(jsonPath("$[0].valor").value(500.0));
    }

    @Test
    @DisplayName("Should return 404 when a referenced catalog item does not exist")
    void shouldReturnNotFoundForUnknownCatalogItem() throws Exception {
        Integer clienteId = createPersona();
        Integer presupuestoId = createPresupuesto(clienteId);

        mockMvc.perform(post("/api/v1/presupuestos/" + presupuestoId + "/items-desde-catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(List.of(999999))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when the presupuesto does not exist")
    void shouldReturnNotFoundForUnknownPresupuesto() throws Exception {
        Item catalogItem = createCatalogItem("Honorarios IT", 1000f);

        mockMvc.perform(post("/api/v1/presupuestos/999999/items-desde-catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(List.of(catalogItem.getIdItem()))))
                .andExpect(status().isNotFound());
    }
}
