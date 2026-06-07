package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("TipoDeTramite — referential integrity checks")
class TipoDeTramiteReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PlantillaPresupuestoRepository plantillaPresupuestoRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return inUse=false when tipo de tramite is not referenced")
    void shouldReturnInUseFalseWhenNotReferenced() throws Exception {
        int id = createTipoTramite("Tramite_unused_" + System.nanoTime());

        mockMvc.perform(get("/api/v1/tipo-tramite/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when tipo de tramite is referenced by plantilla presupuesto")
    void shouldReturnInUseTrueWhenReferencedByPlantillaPresupuesto() throws Exception {
        int id = createTipoTramite("Tramite_used_" + System.nanoTime());
        linkToPlantillaPresupuesto(id);

        mockMvc.perform(get("/api/v1/tipo-tramite/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing tipo de tramite that is in use")
    void shouldReturn409WhenEditingTipoTramiteInUse() throws Exception {
        int id = createTipoTramite("Tramite_edit_conflict_" + System.nanoTime());
        linkToPlantillaPresupuesto(id);

        String updateBody = """
                {"nombre": "Nombre actualizado", "seArchiva": false, "seInscribe": false, "asociaInmuebles": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-tramite/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing tipo de tramite that is not in use")
    void shouldAllowEditingTipoTramiteNotInUse() throws Exception {
        int id = createTipoTramite("Tramite_edit_ok_" + System.nanoTime());

        String updateBody = """
                {"nombre": "Nombre actualizado ok", "seArchiva": false, "seInscribe": false, "asociaInmuebles": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-tramite/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting tipo de tramite that is in use")
    void shouldReturn409WhenDeletingTipoTramiteInUse() throws Exception {
        int id = createTipoTramite("Tramite_delete_conflict_" + System.nanoTime());
        linkToPlantillaPresupuesto(id);

        mockMvc.perform(delete("/api/v1/tipo-tramite/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search tipos de tramite by nombre")
    void shouldSearchTiposTramiteByNombre() throws Exception {
        String uniqueName = "SearchableTramite_" + System.nanoTime();
        createTipoTramite(uniqueName);

        mockMvc.perform(get("/api/v1/tipo-tramite/search")
                        .param("nombre", "SearchableTramite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre =~ /.*SearchableTramite.*/)]").exists());
    }

    private int createTipoTramite(String nombre) throws Exception {
        String body = String.format("""
                {"nombre": "%s", "seArchiva": false, "seInscribe": false, "asociaInmuebles": false}
                """, nombre);

        MvcResult result = mockMvc.perform(post("/api/v1/tipo-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .get("idTipoTramite").asInt();
    }

    private void linkToPlantillaPresupuesto(int idTipoTramite) {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(idTipoTramite, 1);
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto(pk);
        plantillaPresupuestoRepository.save(plantilla);
    }
}
