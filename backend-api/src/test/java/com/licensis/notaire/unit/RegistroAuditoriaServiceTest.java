package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import com.licensis.notaire.service.RegistroAuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegistroAuditoriaService unit tests - CU23")
@ExtendWith(MockitoExtension.class)
class RegistroAuditoriaServiceTest {

    @Mock
    private RegistroAuditoriaRepository repository;

    @InjectMocks
    private RegistroAuditoriaService service;

    private RegistroAuditoria testRegistro;
    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        Persona persona = new Persona();
        persona.setIdPersona(10);
        persona.setNombre("Test");
        persona.setApellido("User");

        testUsuario = new Usuario();
        testUsuario.setIdUsuario(1);
        testUsuario.setNombre("testuser");
        testUsuario.setTipo("Escribano");
        testUsuario.setEstado(true);
        testUsuario.setFkIdPersona(persona);

        testRegistro = new RegistroAuditoria();
        testRegistro.setIdRegistroAuditoria(1);
        testRegistro.setFkIdUsuario(testUsuario);
        testRegistro.setFecha(new Date());
        testRegistro.setDetalleOperacion("LOGIN");
        testRegistro.setModulo("USUARIOS");
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all audit records as entities")
        void shouldReturnAllRegistros() {
            when(repository.findAll()).thenReturn(List.of(testRegistro));

            List<RegistroAuditoria> result = service.findAll();

            assertThat(result).hasSize(1).containsExactly(testRegistro);
            verify(repository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no records exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            assertThat(service.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return paged audit records")
        void shouldReturnPagedRegistros() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);
            when(repository.findAll(pageable)).thenReturn(page);

            Page<RegistroAuditoria> result = service.findAll(pageable);

            assertThat(result.getContent()).containsExactly(testRegistro);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return all audit records as DTOs")
        void shouldReturnAllAsDto() {
            when(repository.findAllWithUsuario()).thenReturn(List.of(testRegistro));

            List<DtoRegistroAuditoria> result = service.findAllAsDto();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIdRegistroAuditoria()).isEqualTo(1);
            assertThat(result.get(0).getUsuarios().getNombre()).isEqualTo("testuser");
            assertThat(result.get(0).getUsuarios().getPersonas().getNombre()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should return paged audit records as DTOs")
        void shouldReturnPagedAsDto() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);
            when(repository.findAll(pageable)).thenReturn(page);

            Page<DtoRegistroAuditoria> result = service.findAllAsDto(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getModulo()).isEqualTo("USUARIOS");
        }
    }

    @Nested
    @DisplayName("findByModulo")
    class FindByModulo {

        @Test
        @DisplayName("Should return audit records filtered by modulo")
        void shouldReturnRecordsForModulo() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);
            when(repository.findByModulo("USUARIOS", pageable)).thenReturn(page);

            Page<RegistroAuditoria> result = service.findByModulo("USUARIOS", pageable);

            assertThat(result.getContent()).containsExactly(testRegistro);
        }

        @Test
        @DisplayName("Should return audit DTOs filtered by modulo")
        void shouldReturnDtosForModulo() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);
            when(repository.findByModulo("USUARIOS", pageable)).thenReturn(page);

            Page<DtoRegistroAuditoria> result = service.findByModuloAsDto("USUARIOS", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getModulo()).isEqualTo("USUARIOS");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return registro when found by ID")
        void shouldReturnRegistroWhenFound() {
            when(repository.findById(1)).thenReturn(Optional.of(testRegistro));

            Optional<RegistroAuditoria> result = service.findById(1);

            assertThat(result).isPresent().contains(testRegistro);
        }

        @Test
        @DisplayName("Should return empty when registro not found")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findById(999)).thenReturn(Optional.empty());

            assertThat(service.findById(999)).isEmpty();
        }

        @Test
        @DisplayName("Should return DTO when found by ID")
        void shouldReturnDtoWhenFound() {
            when(repository.findByIdWithUsuario(1)).thenReturn(Optional.of(testRegistro));

            Optional<DtoRegistroAuditoria> result = service.findByIdAsDto(1);

            assertThat(result).isPresent();
            assertThat(result.get().getModulo()).isEqualTo("USUARIOS");
        }

        @Test
        @DisplayName("Should return empty DTO when not found")
        void shouldReturnEmptyDtoWhenNotFound() {
            when(repository.findByIdWithUsuario(999)).thenReturn(Optional.empty());

            assertThat(service.findByIdAsDto(999)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUsuarioId - CU23")
    class FindByUsuarioId {

        @Test
        @DisplayName("Should return audit records for given user ID")
        void shouldReturnRecordsForUserId() {
            when(repository.findByFkIdUsuarioIdUsuario(1)).thenReturn(List.of(testRegistro));

            List<RegistroAuditoria> result = service.findByUsuarioId(1);

            assertThat(result).hasSize(1).containsExactly(testRegistro);
            verify(repository).findByFkIdUsuarioIdUsuario(1);
        }

        @Test
        @DisplayName("Should return empty list when user has no audit records")
        void shouldReturnEmptyListForUserWithNoRecords() {
            when(repository.findByFkIdUsuarioIdUsuario(999)).thenReturn(Collections.emptyList());

            assertThat(service.findByUsuarioId(999)).isEmpty();
        }

        @Test
        @DisplayName("Should return paged audit records for user")
        void shouldReturnPagedRecordsForUser() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);
            when(repository.findByFkIdUsuarioIdUsuario(1, pageable)).thenReturn(page);

            Page<RegistroAuditoria> result = service.findByUsuarioId(1, pageable);

            assertThat(result.getContent()).containsExactly(testRegistro);
        }

        @Test
        @DisplayName("Should return paged DTOs for user")
        void shouldReturnPagedDtosForUser() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<RegistroAuditoria> page = new PageImpl<>(List.of(testRegistro), pageable, 1);
            when(repository.findByFkIdUsuarioIdUsuario(1, pageable)).thenReturn(page);

            Page<DtoRegistroAuditoria> result = service.findByUsuarioIdAsDto(1, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUsuarios().getIdUsuario()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return DTOs for user without pagination")
        void shouldReturnDtosForUserUnpaged() {
            when(repository.findByUsuarioIdWithUsuario(1)).thenReturn(List.of(testRegistro));

            List<DtoRegistroAuditoria> result = service.findByUsuarioIdAsDto(1);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return audit record")
        void shouldSaveAndReturnRegistro() {
            when(repository.save(any(RegistroAuditoria.class))).thenReturn(testRegistro);

            RegistroAuditoria result = service.save(testRegistro);

            assertThat(result).isEqualTo(testRegistro);
            assertThat(result.getDetalleOperacion()).isEqualTo("LOGIN");
            verify(repository).save(testRegistro);
        }
    }

    @Nested
    @DisplayName("toDto mapping")
    class ToDto {

        @Test
        @DisplayName("Should map entity to DTO with usuario and persona")
        void shouldMapEntityWithUsuarioAndPersona() {
            DtoRegistroAuditoria dto = RegistroAuditoriaService.toDto(testRegistro);

            assertThat(dto.getIdRegistroAuditoria()).isEqualTo(1);
            assertThat(dto.getUsuarios().getNombre()).isEqualTo("testuser");
            assertThat(dto.getUsuarios().getPersonas().getApellido()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should map entity to DTO when usuario is null")
        void shouldMapEntityWithoutUsuario() {
            testRegistro.setFkIdUsuario(null);
            DtoRegistroAuditoria dto = RegistroAuditoriaService.toDto(testRegistro);

            assertThat(dto.getUsuarios()).isNull();
        }

        @Test
        @DisplayName("Should map entity to DTO when persona is null")
        void shouldMapEntityWithoutPersona() {
            testUsuario.setFkIdPersona(null);
            DtoRegistroAuditoria dto = RegistroAuditoriaService.toDto(testRegistro);

            assertThat(dto.getUsuarios().getNombre()).isEqualTo("testuser");
            assertThat(dto.getUsuarios().getPersonas()).isNull();
        }
    }
}
