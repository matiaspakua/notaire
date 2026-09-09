package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.service.ManagementQueryService;
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
class ManagementQueryServiceTest {

    @Mock
    private DeedManagementRepository repository;

    @InjectMocks
    private ManagementQueryService queryService;

    private DeedManagement testManagement;
    private ManagementStatus testStatus;

    @BeforeEach
    void setUp() {
        testStatus = new ManagementStatus(5);
        testStatus.setName("En Proceso");

        testManagement = new DeedManagement(1, 100, new Date(), "Test Gestion");
        testManagement.setFkIdManagementStatus(testStatus);
    }

    private DeedManagement createManagement(Integer id, int number, String encabezado) {
        DeedManagement g = new DeedManagement(id, number, new Date(), encabezado);
        g.setFkIdManagementStatus(testStatus);
        return g;
    }

    @Nested
    @DisplayName("findAll (paged)")
    class FindAllPaged {

        @Test
        @DisplayName("Should return paged summaries from repository")
        void shouldReturnPagedSummaries() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DeedManagement> entities = List.of(
                    createManagement(1, 100, "Gestion A"),
                    createManagement(2, 200, "Gestion B"));
            Page<DeedManagement> entityPage = new PageImpl<>(entities, pageable, 2);
            when(repository.findAll(pageable)).thenReturn(entityPage);

            Page<DtoManagementSummary> result = queryService.findAll(pageable);

            assertThat(result).hasSize(2);
            assertThat(result.getContent())
                    .extracting(DtoManagementSummary::encabezado)
                    .containsExactly("Gestion A", "Gestion B");
            assertThat(result.getContent())
                    .extracting(DtoManagementSummary::number)
                    .containsExactly(100, 200);
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no gestiones exist")
        void shouldReturnEmptyPageWhenNoneExist() {
            Pageable pageable = PageRequest.of(0, 10);
            when(repository.findAll(pageable)).thenReturn(Page.empty(pageable));

            Page<DtoManagementSummary> result = queryService.findAll(pageable);

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
            when(repository.findById(1)).thenReturn(Optional.of(testManagement));

            Optional<DtoManagementSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().idManagement()).isEqualTo(1);
            assertThat(result.get().number()).isEqualTo(100);
            assertThat(result.get().encabezado()).isEqualTo("Test Gestion");
            assertThat(result.get().statusActual()).isEqualTo("En Proceso");
        }

        @Test
        @DisplayName("Should return empty when gestion not found")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findById(999)).thenReturn(Optional.empty());

            Optional<DtoManagementSummary> result = queryService.findById(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByNumero")
    class FindByNumber {

        @Test
        @DisplayName("Should return summary when gestion found by number")
        void shouldReturnSummaryWhenFound() {
            when(repository.findByNumber(100)).thenReturn(Optional.of(testManagement));

            Optional<DtoManagementSummary> result = queryService.findByNumber(100);

            assertThat(result).isPresent();
            assertThat(result.get().number()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should return empty when no gestion matches number")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findByNumber(999)).thenReturn(Optional.empty());

            Optional<DtoManagementSummary> result = queryService.findByNumber(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("DTO Mapping (toSummary)")
    class DtoMapping {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFields() {
            when(repository.findById(1)).thenReturn(Optional.of(testManagement));

            Optional<DtoManagementSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            DtoManagementSummary summary = result.get();
            assertThat(summary.idManagement()).isEqualTo(1);
            assertThat(summary.number()).isEqualTo(100);
            assertThat(summary.encabezado()).isEqualTo("Test Gestion");
            assertThat(summary.dateStart()).isNotNull();
            assertThat(summary.statusActual()).isEqualTo("En Proceso");
            assertThat(summary.procedureCount()).isZero();
        }

        @Test
        @DisplayName("Should handle null estadoDeGestion gracefully")
        void shouldHandleNullStatus() {
            testManagement.setFkIdManagementStatus(null);
            when(repository.findById(1)).thenReturn(Optional.of(testManagement));

            Optional<DtoManagementSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().statusActual()).isNull();
        }

        @Test
        @DisplayName("Should count procedures correctly")
        void shouldCountProcedures() {
            testManagement.setProcedureList(List.of(new Procedure(), new Procedure()));
            when(repository.findById(1)).thenReturn(Optional.of(testManagement));

            Optional<DtoManagementSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().procedureCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero procedureCount when tramiteList is null")
        void shouldReturnZeroProcedureCountWhenListIsNull() {
            testManagement.setProcedureList(null);
            testManagement.setFkIdManagementStatus(null);
            when(repository.findById(1)).thenReturn(Optional.of(testManagement));

            Optional<DtoManagementSummary> result = queryService.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().procedureCount()).isZero();
            assertThat(result.get().statusActual()).isNull();
        }
    }
}
