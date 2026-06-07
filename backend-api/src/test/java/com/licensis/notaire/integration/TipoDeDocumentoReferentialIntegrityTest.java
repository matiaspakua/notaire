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
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.repository.PlantillaTramiteRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("TipoDeDocumento — referential integrity checks")
class TipoDeDocumentoReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PlantillaTramiteRepository plantillaTramiteRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return inUse=false when tipo de documento is not referenced")
    void shouldReturnInUseFalseWhenNotReferenced() throws Exception {
        int id = createTipoDocumento("Tipo_unused_" + System.nanoTime());

        mockMvc.perform(get("/api/v1/tipo-de-documento/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when tipo de documento is referenced by plantilla tramite")
    void shouldReturnInUseTrueWhenReferencedByPlantillaTramite() throws Exception {
        int id = createTipoDocumento("Tipo_used_" + System.nanoTime());
        linkToPlantillaTramite(id);

        mockMvc.perform(get("/api/v1/tipo-de-documento/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing tipo de documento that is in use")
    void shouldReturn409WhenEditingTipoDocumentoInUse() throws Exception {
        int id = createTipoDocumento("Tipo_edit_conflict_" + System.nanoTime());
        linkToPlantillaTramite(id);

        String updateBody = """
                {"nombre": "Nombre actualizado", "vence": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-de-documento/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing tipo de documento that is not in use")
    void shouldAllowEditingTipoDocumentoNotInUse() throws Exception {
        int id = createTipoDocumento("Tipo_edit_ok_" + System.nanoTime());

        String updateBody = """
                {"nombre": "Nombre actualizado ok", "vence": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-de-documento/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting tipo de documento that is in use")
    void shouldReturn409WhenDeletingTipoDocumentoInUse() throws Exception {
        int id = createTipoDocumento("Tipo_delete_conflict_" + System.nanoTime());
        linkToPlantillaTramite(id);

        mockMvc.perform(delete("/api/v1/tipo-de-documento/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search tipos de documento by nombre")
    void shouldSearchTiposDocumentoByNombre() throws Exception {
        String uniqueName = "SearchableDoc_" + System.nanoTime();
        createTipoDocumento(uniqueName);

        mockMvc.perform(get("/api/v1/tipo-de-documento/search")
                        .param("nombre", "SearchableDoc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre =~ /.*SearchableDoc.*/)]").exists());
    }

    private int createTipoDocumento(String nombre) throws Exception {
        String body = String.format("""
                {"nombre": "%s", "vence": false}
                """, nombre);

        MvcResult result = mockMvc.perform(post("/api/v1/tipo-de-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .get("idTipoDocumento").asInt();
    }

    private void linkToPlantillaTramite(int idTipoDocumento) {
        PlantillaTramitePK pk = new PlantillaTramitePK(1, idTipoDocumento);
        PlantillaTramite plantilla = new PlantillaTramite(pk);
        plantillaTramiteRepository.save(plantilla);
    }
}
