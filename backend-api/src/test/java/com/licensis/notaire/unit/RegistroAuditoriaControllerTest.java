package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.RegistroAuditoriaController;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.service.RegistroAuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("RegistroAuditoriaController unit tests")
@ExtendWith(MockitoExtension.class)
class RegistroAuditoriaControllerTest {

    @Mock
    private RegistroAuditoriaService service;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DtoRegistroAuditoria sampleDto;
    private RegistroAuditoria sampleEntity;

    @BeforeEach
    void setUp() {
        RegistroAuditoriaController controller = new RegistroAuditoriaController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        DtoPersona persona = new DtoPersona();
        persona.setIdPersona(10);
        persona.setNombre("Admin");
        persona.setApellido("Sistema");

        DtoUsuario usuario = new DtoUsuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("admin");
        usuario.setTipo("Escribano");
        usuario.setEstado(true);
        usuario.setPersonas(persona);

        sampleDto = new DtoRegistroAuditoria();
        sampleDto.setIdRegistroAuditoria(100);
        sampleDto.setDetalleOperacion("Consulta de listado de escrituras");
        sampleDto.setModulo("Escrituras");
        sampleDto.setFecha(new Date());
        sampleDto.setUsuarios(usuario);

        sampleEntity = new RegistroAuditoria();
        sampleEntity.setIdRegistroAuditoria(100);
        sampleEntity.setDetalleOperacion("Consulta de listado de escrituras");
        sampleEntity.setModulo("Escrituras");
        sampleEntity.setFecha(new Date());
    }

    @Test
    @DisplayName("Should return a list of DTOs without pagination params")
    void shouldReturnFlatListWhenNoPaginationParams() throws Exception {
        when(service.findAllAsDto()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/registro-auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRegistroAuditoria").value(100))
                .andExpect(jsonPath("$[0].modulo").value("Escrituras"))
                .andExpect(jsonPath("$[0].detalleOperacion").value("Consulta de listado de escrituras"))
                .andExpect(jsonPath("$[0].usuarios.nombre").value("admin"))
                .andExpect(jsonPath("$[0].usuarios.personas.nombre").value("Admin"));
    }

    @Test
    @DisplayName("Should return a paginated response when page parameter is provided")
    void shouldReturnPagedResponseWhenPageProvided() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<DtoRegistroAuditoria> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findAllAsDto(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/registro-auditoria?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idRegistroAuditoria").value(100))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("Should filter by modulo when modulo param is set with pagination")
    void shouldFilterByModuloWhenParamSet() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<DtoRegistroAuditoria> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findByModuloAsDto(eq("Escrituras"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/registro-auditoria?page=0&size=20&modulo=Escrituras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].modulo").value("Escrituras"));

        verify(service).findByModuloAsDto(eq("Escrituras"), any(Pageable.class));
        verify(service, never()).findAllAsDto(any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by idUsuario when idUsuario param is set with pagination")
    void shouldFilterByIdUsuarioWhenParamSet() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<DtoRegistroAuditoria> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findByUsuarioIdAsDto(eq(1), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/registro-auditoria?page=0&size=20&idUsuario=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].usuarios.idUsuario").value(1));

        verify(service).findByUsuarioIdAsDto(eq(1), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return registro by id when found")
    void shouldReturnRegistroByIdWhenFound() throws Exception {
        when(service.findByIdAsDto(100)).thenReturn(Optional.of(sampleDto));

        mockMvc.perform(get("/api/v1/registro-auditoria/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRegistroAuditoria").value(100));
    }

    @Test
    @DisplayName("Should return 404 when registro id not found")
    void shouldReturn404WhenIdNotFound() throws Exception {
        when(service.findByIdAsDto(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/registro-auditoria/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return registros by usuario id")
    void shouldReturnRegistrosByUsuarioId() throws Exception {
        when(service.findByUsuarioIdAsDto(1)).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/registro-auditoria/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarios.idUsuario").value(1));
    }

    @Test
    @DisplayName("Should create a new registro and return its entity")
    void shouldCreateNewRegistroAndReturnEntity() throws Exception {
        when(service.save(any(RegistroAuditoria.class))).thenReturn(sampleEntity);

        mockMvc.perform(post("/api/v1/registro-auditoria")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRegistroAuditoria").value(100));
    }

    @Test
    @DisplayName("Should delete an existing registro and return 200")
    void shouldDeleteExistingRegistro() throws Exception {
        when(service.findById(100)).thenReturn(Optional.of(sampleEntity));

        mockMvc.perform(delete("/api/v1/registro-auditoria/100"))
                .andExpect(status().isOk());

        verify(service).deleteById(100);
    }

    @Test
    @DisplayName("Should return 404 when deleting a non-existent registro")
    void shouldReturn404WhenDeletingMissingRegistro() throws Exception {
        when(service.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/registro-auditoria/999"))
                .andExpect(status().isNotFound());

        verify(service, never()).deleteById(999);
    }

    @Test
    @DisplayName("Should fall back to default sort on invalid sort parameter")
    void shouldFallBackToDefaultSortOnInvalidParameter() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<DtoRegistroAuditoria> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findAllAsDto(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/registro-auditoria?page=0&size=20&sort=fecha,unknown"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept negative page or zero size and normalize")
    void shouldNormalizeInvalidPaginationValues() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<DtoRegistroAuditoria> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findAllAsDto(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/registro-auditoria?page=-1&size=0"))
                .andExpect(status().isOk());
    }
}
