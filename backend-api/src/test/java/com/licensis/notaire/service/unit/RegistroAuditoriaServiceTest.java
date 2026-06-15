package com.licensis.notaire.service.unit;

import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import com.licensis.notaire.service.RegistroAuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistroAuditoriaService Unit Tests")
class RegistroAuditoriaServiceTest {

    @Mock
    private RegistroAuditoriaRepository repository;

    @InjectMocks
    private RegistroAuditoriaService service;

    private RegistroAuditoria testRegistro;
    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        testUsuario = new Usuario();
        testUsuario.setIdUsuario(1);
        testUsuario.setNombre("Juan");
        testUsuario.setTipo("ADMIN");
        testUsuario.setEstado(true);

        testRegistro = new RegistroAuditoria();
        testRegistro.setIdRegistroAuditoria(1);
        testRegistro.setModulo("DOCUMENTOS");
        testRegistro.setDetalleOperacion("CREATE");
        testRegistro.setFecha(new Date());
        testRegistro.setFkIdUsuario(testUsuario);
    }

    @Test
    @DisplayName("Should find all registros auditoria")
    void shouldFindAll() {
        List<RegistroAuditoria> registros = new ArrayList<>();
        registros.add(testRegistro);

        when(repository.findAll()).thenReturn(registros);

        List<RegistroAuditoria> result = service.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testRegistro);

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no registros exist")
    void shouldReturnEmptyListWhenNoRegistrosExist() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        List<RegistroAuditoria> result = service.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find all registros as DTOs")
    void shouldFindAllAsDto() {
        List<RegistroAuditoria> registros = new ArrayList<>();
        registros.add(testRegistro);

        when(repository.findAllWithUsuario()).thenReturn(registros);

        List<DtoRegistroAuditoria> result = service.findAllAsDto();

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModulo().equals("DOCUMENTOS"));

        verify(repository, times(1)).findAllWithUsuario();
    }

    @Test
    @DisplayName("Should find registros with pagination")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<RegistroAuditoria> result = service.findAll(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testRegistro);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find registros as DTOs with pagination")
    void shouldFindAllAsPagedDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<DtoRegistroAuditoria> result = service.findAllAsDto(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModulo().equals("DOCUMENTOS"));

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find registros by modulo")
    void shouldFindByModulo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);

        when(repository.findByModulo("DOCUMENTOS", pageable)).thenReturn(page);

        Page<RegistroAuditoria> result = service.findByModulo("DOCUMENTOS", pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(reg -> reg.getModulo().equals("DOCUMENTOS"));

        verify(repository, times(1)).findByModulo("DOCUMENTOS", pageable);
    }

    @Test
    @DisplayName("Should find registros by modulo as DTOs")
    void shouldFindByModuloAsDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);

        when(repository.findByModulo("DOCUMENTOS", pageable)).thenReturn(page);

        Page<DtoRegistroAuditoria> result = service.findByModuloAsDto("DOCUMENTOS", pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModulo().equals("DOCUMENTOS"));

        verify(repository, times(1)).findByModulo("DOCUMENTOS", pageable);
    }

    @Test
    @DisplayName("Should find registros by usuario id")
    void shouldFindByUsuarioId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);

        when(repository.findByFkIdUsuarioIdUsuario(1, pageable)).thenReturn(page);

        Page<RegistroAuditoria> result = service.findByUsuarioId(1, pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(reg -> reg.getFkIdUsuario().getIdUsuario().equals(1));

        verify(repository, times(1)).findByFkIdUsuarioIdUsuario(1, pageable);
    }

    @Test
    @DisplayName("Should find registros by usuario id as DTOs")
    void shouldFindByUsuarioIdAsDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);

        when(repository.findByFkIdUsuarioIdUsuario(1, pageable)).thenReturn(page);

        Page<DtoRegistroAuditoria> result = service.findByUsuarioIdAsDto(1, pageable);

        assertThat(result).isNotNull()
                .hasSize(1);

        verify(repository, times(1)).findByFkIdUsuarioIdUsuario(1, pageable);
    }

    @Test
    @DisplayName("Should find registro by id")
    void shouldFindById() {
        when(repository.findById(1)).thenReturn(Optional.of(testRegistro));

        Optional<RegistroAuditoria> result = service.findById(1);

        assertThat(result).isPresent()
                .contains(testRegistro);

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when registro not found")
    void shouldReturnEmptyOptionalWhenRegistroNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        Optional<RegistroAuditoria> result = service.findById(999);

        assertThat(result).isEmpty();

        verify(repository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find registro by id as DTO")
    void shouldFindByIdAsDto() {
        when(repository.findByIdWithUsuario(1)).thenReturn(Optional.of(testRegistro));

        Optional<DtoRegistroAuditoria> result = service.findByIdAsDto(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.getIdRegistroAuditoria()).isEqualTo(1);
                    assertThat(dto.getModulo()).isEqualTo("DOCUMENTOS");
                });

        verify(repository, times(1)).findByIdWithUsuario(1);
    }

    @Test
    @DisplayName("Should find registros list by usuario id")
    void shouldFindRegistrosByUsuarioIdList() {
        List<RegistroAuditoria> registros = new ArrayList<>();
        registros.add(testRegistro);

        when(repository.findByFkIdUsuarioIdUsuario(1)).thenReturn(registros);

        List<RegistroAuditoria> result = service.findByUsuarioId(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testRegistro);

        verify(repository, times(1)).findByFkIdUsuarioIdUsuario(1);
    }

    @Test
    @DisplayName("Should find registros list by usuario id as DTOs")
    void shouldFindRegistrosByUsuarioIdAsDto() {
        List<RegistroAuditoria> registros = new ArrayList<>();
        registros.add(testRegistro);

        when(repository.findByUsuarioIdWithUsuario(1)).thenReturn(registros);

        List<DtoRegistroAuditoria> result = service.findByUsuarioIdAsDto(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModulo().equals("DOCUMENTOS"));

        verify(repository, times(1)).findByUsuarioIdWithUsuario(1);
    }

    @Test
    @DisplayName("Should save registro auditoria")
    void shouldSaveRegistro() {
        when(repository.save(testRegistro)).thenReturn(testRegistro);

        RegistroAuditoria result = service.save(testRegistro);

        assertThat(result).isNotNull()
                .isEqualTo(testRegistro)
                .extracting(RegistroAuditoria::getModulo)
                .isEqualTo("DOCUMENTOS");

        verify(repository, times(1)).save(testRegistro);
    }

    @Test
    @DisplayName("Should delete registro by id")
    void shouldDeleteRegistroById() {
        service.deleteById(1);

        verify(repository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should map registro to DTO correctly")
    void shouldMapRegistroToDto() {
        DtoRegistroAuditoria dto = RegistroAuditoriaService.toDto(testRegistro);

        assertThat(dto).isNotNull()
                .extracting(DtoRegistroAuditoria::getIdRegistroAuditoria,
                        DtoRegistroAuditoria::getModulo,
                        DtoRegistroAuditoria::getDetalleOperacion)
                .containsExactly(1, "DOCUMENTOS", "CREATE");
        assertThat(dto.getUsuarios()).isNotNull()
                .extracting(u -> u.getNombre())
                .isEqualTo("Juan");
    }

    @Test
    @DisplayName("Should handle null usuario in DTO mapping")
    void shouldHandleNullUsuarioInDtoMapping() {
        RegistroAuditoria registroSinUsuario = new RegistroAuditoria();
        registroSinUsuario.setIdRegistroAuditoria(1);
        registroSinUsuario.setModulo("DOCUMENTOS");
        registroSinUsuario.setDetalleOperacion("DELETE");
        registroSinUsuario.setFecha(new Date());
        registroSinUsuario.setFkIdUsuario(null);

        DtoRegistroAuditoria dto = RegistroAuditoriaService.toDto(registroSinUsuario);

        assertThat(dto).isNotNull()
                .extracting(DtoRegistroAuditoria::getModulo)
                .isEqualTo("DOCUMENTOS");
        assertThat(dto.getUsuarios()).isNull();
    }
}
