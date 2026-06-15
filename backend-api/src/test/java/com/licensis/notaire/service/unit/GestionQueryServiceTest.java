package com.licensis.notaire.service.unit;

import com.licensis.notaire.dto.DtoGestionSummary;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.service.GestionQueryService;
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
class GestionQueryServiceTest {

    @Mock
    private GestionDeEscrituraRepository repository;

    @InjectMocks
    private GestionQueryService service;

    private GestionDeEscritura testGestion;
    private EstadoDeGestion testEstado;

    @BeforeEach
    void setUp() {
        testEstado = new EstadoDeGestion();
        testEstado.setIdEstadoGestion(1);
        testEstado.setNombre("INICIADO");

        testGestion = new GestionDeEscritura();
        testGestion.setIdGestion(1);
        testGestion.setNumero(100);
        testGestion.setEncabezado("Venta Inmueble");
        testGestion.setFechaInicio(new Date());
        testGestion.setFkIdEstadoDeGestion(testEstado);
    }

    @Test
    @DisplayName("Should find all gestiones paginated")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<GestionDeEscritura> page = new PageImpl<>(List.of(testGestion), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<DtoGestionSummary> result = service.findAll(pageable);

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
        Page<GestionDeEscritura> page = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<DtoGestionSummary> result = service.findAll(pageable);

        assertThat(result).isNotNull()
                .hasSize(0)
                .isEmpty();

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find gestion by id")
    void shouldFindGestionById() {
        when(repository.findById(1)).thenReturn(Optional.of(testGestion));

        Optional<DtoGestionSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.idGestion()).isEqualTo(1);
                    assertThat(dto.numero()).isEqualTo(100);
                    assertThat(dto.encabezado()).isEqualTo("Venta Inmueble");
                    assertThat(dto.estadoActual()).isEqualTo("INICIADO");
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty when gestion id not found")
    void shouldReturnEmptyWhenGestionIdNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        Optional<DtoGestionSummary> result = service.findById(999);

        assertThat(result).isEmpty();

        verify(repository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find gestion by numero")
    void shouldFindGestionByNumero() {
        when(repository.findByNumero(100)).thenReturn(Optional.of(testGestion));

        Optional<DtoGestionSummary> result = service.findByNumero(100);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.numero()).isEqualTo(100);
                    assertThat(dto.encabezado()).isEqualTo("Venta Inmueble");
                });

        verify(repository, times(1)).findByNumero(100);
    }

    @Test
    @DisplayName("Should return empty when gestion numero not found")
    void shouldReturnEmptyWhenGestionNumeroNotFound() {
        when(repository.findByNumero(999)).thenReturn(Optional.empty());

        Optional<DtoGestionSummary> result = service.findByNumero(999);

        assertThat(result).isEmpty();

        verify(repository, times(1)).findByNumero(999);
    }

    @Test
    @DisplayName("Should handle null estado in summary mapping")
    void shouldHandleNullEstadoInMapping() {
        GestionDeEscritura gestionSinEstado = new GestionDeEscritura();
        gestionSinEstado.setIdGestion(1);
        gestionSinEstado.setNumero(100);
        gestionSinEstado.setEncabezado("Test");
        gestionSinEstado.setFechaInicio(new Date());
        gestionSinEstado.setFkIdEstadoDeGestion(null);

        when(repository.findById(1)).thenReturn(Optional.of(gestionSinEstado));

        Optional<DtoGestionSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.estadoActual()).isNull();
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should count tramites in summary")
    void shouldCountTramitesInSummary() {
        testGestion.setTramiteList(new ArrayList<>());
        testGestion.getTramiteList().add(null);
        testGestion.getTramiteList().add(null);

        when(repository.findById(1)).thenReturn(Optional.of(testGestion));

        Optional<DtoGestionSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.tramiteCount()).isEqualTo(2);
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should handle null tramites list")
    void shouldHandleNullTramitesList() {
        testGestion.setTramiteList(null);

        when(repository.findById(1)).thenReturn(Optional.of(testGestion));

        Optional<DtoGestionSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.tramiteCount()).isEqualTo(0);
                });

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should map fecha inicio correctly")
    void shouldMapFechaInicio() {
        Date testDate = new Date();
        testGestion.setFechaInicio(testDate);

        when(repository.findById(1)).thenReturn(Optional.of(testGestion));

        Optional<DtoGestionSummary> result = service.findById(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.fechaInicio()).isEqualTo(testDate);
                });

        verify(repository, times(1)).findById(1);
    }
}
