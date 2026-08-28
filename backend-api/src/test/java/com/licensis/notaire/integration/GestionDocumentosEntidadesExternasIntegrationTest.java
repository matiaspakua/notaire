package com.licensis.notaire.integration;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.InmuebleRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;

/**
 * CU10 - Registrar movimientos de documentación de entidades externas.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion documentos de entidades externas (CU10)")
class GestionDocumentosEntidadesExternasIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private DocumentoPresentadoRepository documentoPresentadoRepository;

    @Autowired
    private InmuebleRepository inmuebleRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Escribano IT", "apellido": "CU10", "numeroIdentificacion": "%s",
                 "esCliente": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createGestionConTramite(Integer numero) throws Exception {
        Integer escribanoId = createPersona("42100" + numero);
        String body = """
                {"encabezado": "Gestion CU10", "fechaInicio": "2026-01-01", "numero": %d,
                 "fkIdPersonaEscribano": {"idPersona": %d}}
                """.formatted(numero, escribanoId);
        MvcResult result = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer gestionId = mapper.readTree(result.getResponse().getContentAsString()).get("idGestion").asInt();

        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tramite CU10 " + numero);
        tipo.setHabilitado(true);
        tipo.setSeArchiva(false);
        tipo.setSeInscribe(false);
        tipo.setAsociaInmuebles(false);
        tipo = tipoDeTramiteRepository.save(tipo);

        GestionDeEscritura gestionRef = new GestionDeEscritura();
        gestionRef.setIdGestion(gestionId);

        Tramite tramite = new Tramite();
        tramite.setFkIdTipoTramite(tipo);
        tramite.setFkIdGestion(gestionRef);
        tramiteRepository.save(tramite);

        return gestionId;
    }

    private DocumentoPresentado createDocumentoEntidadExterna(Integer idGestion, String nombre) {
        Tramite tramite = tramiteRepository.findByFkIdGestionIdGestion(idGestion).get(0);
        DocumentoPresentado documento = new DocumentoPresentado();
        documento.setNombre(nombre);
        documento.setQuienEntrega(ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA);
        documento.setFkIdTramite(tramite);
        documento.setEntregado(false);
        documento.setPreparado(false);
        documento.setVence(false);
        return documentoPresentadoRepository.save(documento);
    }

    @Test
    @DisplayName("Should return 404 when the gestión does not exist")
    void shouldReturn404WhenGestionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/documentos-entidades-externas"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list the entidad externa documents and nomenclatura catastral of a gestión")
    void shouldListDocumentosEntidadesExternas() throws Exception {
        Integer gestionId = createGestionConTramite(9301);
        createDocumentoEntidadExterna(gestionId, "Certificado de Dominio");

        Inmueble inmueble = new Inmueble();
        inmueble.setDomicilio("Calle Falsa 123");
        inmueble.setNomenclaturaCatastral("11-22-33");
        inmueble = inmuebleRepository.save(inmueble);
        Tramite tramite = tramiteRepository.findByFkIdGestionIdGestion(gestionId).get(0);
        tramite.setFkIdInmueble(inmueble);
        tramite = tramiteRepository.save(tramite);

        try {
            mockMvc.perform(get("/api/v1/gestiones/" + gestionId + "/documentos-entidades-externas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idGestion").value(gestionId))
                    .andExpect(jsonPath("$.nomenclaturaCatastral").value("11-22-33"))
                    .andExpect(jsonPath("$.documentos[0].nombre").value("Certificado de Dominio"));
        } finally {
            tramite.setFkIdInmueble(null);
            tramiteRepository.save(tramite);
            inmuebleRepository.delete(inmueble);
        }
    }

    @Test
    @DisplayName("Should register the movement of a document and return it updated")
    void shouldRegistrarMovimiento() throws Exception {
        Integer gestionId = createGestionConTramite(9302);
        DocumentoPresentado documento = createDocumentoEntidadExterna(gestionId, "Informe de Dominio");

        String body = """
                {"preparado": true, "numeroCarton": 5, "observaciones": "Retirado", "entregado": false}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + gestionId + "/documentos-entidades-externas/"
                        + documento.getIdDocumentoPresentado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preparado").value(true))
                .andExpect(jsonPath("$.numeroCarton").value(5))
                .andExpect(jsonPath("$.observaciones").value("Retirado"));
    }

    @Test
    @DisplayName("Should return 400 when the document does not belong to the gestión")
    void shouldReturn400WhenDocumentDoesNotBelongToGestion() throws Exception {
        Integer gestionId = createGestionConTramite(9303);
        Integer otraGestionId = createGestionConTramite(9304);
        DocumentoPresentado documento = createDocumentoEntidadExterna(otraGestionId, "Informe de Dominio");

        String body = """
                {"entregado": true}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + gestionId + "/documentos-entidades-externas/"
                        + documento.getIdDocumentoPresentado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the document does not exist")
    void shouldReturn404WhenDocumentDoesNotExist() throws Exception {
        Integer gestionId = createGestionConTramite(9305);

        String body = """
                {"entregado": true}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + gestionId + "/documentos-entidades-externas/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should keep gestión open when the workflow does not define the completion transition")
    void shouldMarkAllDocumentsDeliveredWithoutFailingWhenNoWorkflow() throws Exception {
        Integer gestionId = createGestionConTramite(9306);
        DocumentoPresentado documento = createDocumentoEntidadExterna(gestionId, "Libre de Deuda");

        String body = """
                {"entregado": true}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + gestionId + "/documentos-entidades-externas/"
                        + documento.getIdDocumentoPresentado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entregado").value(true));
    }
}
