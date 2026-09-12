package com.licensis.notaire.service.unit;

import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.service.ManagementQueryService;
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
@DisplayName("GestionQueryService Unit Tests")
class ManagementQueryServiceTest {

    @Mock
    private DeedManagementRepository repository;

    @InjectMocks
    private ManagementQueryService service;

    private DeedManagement testManagement;
    private ManagementStatus testStatus;

    @BeforeEach
    void setUp() {
        testStatus = new ManagementStatus();
        testStatus.setIdManagementStatus(1);
        testStatus.setName("INICIADO");

        testManagement = new DeedManagement();
        testManagement.setIdManagement(1);
        testManagement.setNumber(100);
        testManagement.setEncabezado("Venta Inmueble");
        testManagement.setDateStart(new Date());
        testManagement.setFkIdManagementStatus(testStatus);
    }

    @Test
    @DisplayName("Should find all gestiones paginated")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DeedManagement> page = new PageImpl<>(List.of(testManagement), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<DtoManagementSummary> result = service.findAll(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.encabezado().equals("Venta Inmueble"));
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no gestiones")
    void shouldReturnEmptyPageWhenNoGestiones() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DeedManagement> page = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<DtoManagementSummary> result = service.findAll(pageable);

        assertThat(result).isNotNull()
                .hasSize(0)
                .isEmpty();

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find gestion by id")
    void shouldFindManagementById() {
        when(repository.findById(1)).thenReturn(Optional.of(testManagement));

        Optional<DtoManagementSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.idManagement()).isEqualTo(1);
                    assertThat(dto.number()).isEqualTo(100);
                    assertThat(dto.encabezado()).isEqualTo("Venta Inmueble");
                    assertThat(dto.statusActual()).isEqualTo("INICIADO");
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty when gestion id not found")
    void shouldReturnEmptyWhenManagementIdNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        Optional<DtoManagementSummary> result = service.findById(999);

        assertThat(result).isEmpty();

        verify(repository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find gestion by number")
    void shouldFindManagementByNumber() {
        when(repository.findByNumber(100)).thenReturn(Optional.of(testManagement));

        Optional<DtoManagementSummary> result = service.findByNumber(100);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.number()).isEqualTo(100);
                    assertThat(dto.encabezado()).isEqualTo("Venta Inmueble");
                });

        verify(repository, times(1)).findByNumber(100);
    }

    @Test
    @DisplayName("Should return empty when gestion number not found")
    void shouldReturnEmptyWhenManagementNumberNotFound() {
        when(repository.findByNumber(999)).thenReturn(Optional.empty());

        Optional<DtoManagementSummary> result = service.findByNumber(999);

        assertThat(result).isEmpty();

        verify(repository, times(1)).findByNumber(999);
    }

    @Test
    @DisplayName("Should handle null status in summary mapping")
    void shouldHandleNullStatusInMapping() {
        DeedManagement managementSinStatus = new DeedManagement();
        managementSinStatus.setIdManagement(1);
        managementSinStatus.setNumber(100);
        managementSinStatus.setEncabezado("Test");
        managementSinStatus.setDateStart(new Date());
        managementSinStatus.setFkIdManagementStatus(null);

        when(repository.findById(1)).thenReturn(Optional.of(managementSinStatus));

        Optional<DtoManagementSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.statusActual()).isNull();
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should count procedures in summary")
    void shouldCountProceduresInSummary() {
        testManagement.setProcedureList(new ArrayList<>());
        testManagement.getProcedureList().add(null);
        testManagement.getProcedureList().add(null);

        when(repository.findById(1)).thenReturn(Optional.of(testManagement));

        Optional<DtoManagementSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.procedureCount()).isEqualTo(2);
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should handle null procedures list")
    void shouldHandleNullProceduresList() {
        testManagement.setProcedureList(null);

        when(repository.findById(1)).thenReturn(Optional.of(testManagement));

        Optional<DtoManagementSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.procedureCount()).isEqualTo(0);
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should map date inicio correctly")
    void shouldMapDateStart() {
        Date testDate = new Date();
        testManagement.setDateStart(testDate);

        when(repository.findById(1)).thenReturn(Optional.of(testManagement));

        Optional<DtoManagementSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.dateStart()).isEqualTo(testDate);
                });

        verify(repository, times(1)).findById(1);
    }
}
