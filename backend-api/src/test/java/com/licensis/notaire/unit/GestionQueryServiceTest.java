package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoGestionSummary;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.service.GestionQueryService;
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
import static org.mockito.Mockito.*;

@DisplayName("GestionQueryService unit tests")
@ExtendWith(MockitoExtension.class)
class GestionQueryServiceTest {

    @Mock
    private GestionDeEscrituraRepository repository;

    @InjectMocks
    private GestionQueryService queryService;

    private GestionDeEscritura testGestion;
    private EstadoDeGestion testEstado;

    @BeforeEach
    void setUp() {
        testEstado = new EstadoDeGestion(5);
        testEstado.setNombre("En Proceso");

        testGestion = new GestionDeEscritura(1, 100, new Date(), "Test Gestion");
        testGestion.setFkIdEstadoDeGestion(testEstado);
    }

    private GestionDeEscritura createGestion(Integer id, int numero, String encabezado) {
        GestionDeEscritura g = new GestionDeEscritura(id, numero, new Date(), encabezado);
        g.setFkIdEstadoDeGestion(testEstado);
        return g;
    }

    @Nested
    @DisplayName("findAll (paged)")
    class FindAllPaged {

        @Test
        @DisplayName("Should return paged summaries from repository")
        void shouldReturnPagedSummaries() {
            Pageable pageable = PageRequest.of(0, 10);
            List<GestionDeEscritura> entities = List.of(
                    createGestion(1, 100, "Gestion A"),
                    createGestion(2, 200, "Gestion B"));
            Page<GestionDeEscritura> entityPage = new PageImpl<>(entities, pageable, 2);
            when(repository.findAll(pageable)).thenReturn(entityPage);

            Page<DtoGestionSummary> result = queryService.findAll(pageable);

            assertThat(result).hasSize(2);
            assertThat(result.getContent())
                    .extracting(DtoGestionSummary::encabezado)
                    .containsExactly("Gestion A", "Gestion B");
            assertThat(result.getContent())
                    .extracting(DtoGestionSummary::numero)
                    .containsExactly(100, 200);
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no gestiones exist")
        void shouldReturnEmptyPageWhenNoneExist() {
            Pageable pageable = PageRequest.of(0, 10);
            when(repository.findAll(pageable)).thenReturn(Page.empty(pageable));

            Page<DtoGestionSummary> result = queryService.findAll(pageable);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return summary when gestion found by ID")
        void shouldReturnSummaryWhenFound() {
            when(repository.findById(1)).thenReturn(Optional.of(testGestion));

            Optional<DtoGestionSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().idGestion()).isEqualTo(1);
            assertThat(result.get().numero()).isEqualTo(100);
            assertThat(result.get().encabezado()).isEqualTo("Test Gestion");
            assertThat(result.get().estadoActual()).isEqualTo("En Proceso");
        }

        @Test
        @DisplayName("Should return empty when gestion not found")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findById(999)).thenReturn(Optional.empty());

            Optional<DtoGestionSummary> result = queryService.findById(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByNumero")
    class FindByNumero {

        @Test
        @DisplayName("Should return summary when gestion found by numero")
        void shouldReturnSummaryWhenFound() {
            when(repository.findByNumero(100)).thenReturn(Optional.of(testGestion));

            Optional<DtoGestionSummary> result = queryService.findByNumero(100);

            assertThat(result).isPresent();
            assertThat(result.get().numero()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should return empty when no gestion matches numero")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findByNumero(999)).thenReturn(Optional.empty());

            Optional<DtoGestionSummary> result = queryService.findByNumero(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("DTO Mapping (toSummary)")
    class DtoMapping {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFields() {
            when(repository.findById(1)).thenReturn(Optional.of(testGestion));

            Optional<DtoGestionSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            DtoGestionSummary summary = result.get();
            assertThat(summary.idGestion()).isEqualTo(1);
            assertThat(summary.numero()).isEqualTo(100);
            assertThat(summary.encabezado()).isEqualTo("Test Gestion");
            assertThat(summary.fechaInicio()).isNotNull();
            assertThat(summary.estadoActual()).isEqualTo("En Proceso");
            assertThat(summary.tramiteCount()).isZero();
        }

        @Test
        @DisplayName("Should handle null estadoDeGestion gracefully")
        void shouldHandleNullEstado() {
            testGestion.setFkIdEstadoDeGestion(null);
            when(repository.findById(1)).thenReturn(Optional.of(testGestion));

            Optional<DtoGestionSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().estadoActual()).isNull();
        }

        @Test
        @DisplayName("Should count tramites correctly")
        void shouldCountTramites() {
            testGestion.setTramiteList(List.of(new Tramite(), new Tramite()));
            when(repository.findById(1)).thenReturn(Optional.of(testGestion));

            Optional<DtoGestionSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().tramiteCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero tramiteCount when tramiteList is null")
        void shouldReturnZeroTramiteCountWhenListIsNull() {
            testGestion.setTramiteList(null);
            testGestion.setFkIdEstadoDeGestion(null);
            when(repository.findById(1)).thenReturn(Optional.of(testGestion));

            Optional<DtoGestionSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().tramiteCount()).isZero();
            assertThat(result.get().estadoActual()).isNull();
        }
    }
}
