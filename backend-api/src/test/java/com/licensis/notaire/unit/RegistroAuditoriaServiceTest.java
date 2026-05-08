package com.licensis.notaire.unit;

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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        testUsuario = new Usuario();
        testUsuario.setIdUsuario(1);
        testUsuario.setNombre("testuser");

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
        @DisplayName("Should return all audit records")
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
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should delegate to repository deleteById")
        void shouldDeleteRegistro() {
            doNothing().when(repository).deleteById(1);

            service.deleteById(1);

            verify(repository).deleteById(1);
        }
    }
}
