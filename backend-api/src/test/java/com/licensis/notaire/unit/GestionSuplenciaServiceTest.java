package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.repository.SuplenciaRepository;
import com.licensis.notaire.service.GestionSuplenciaService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU22", "CU59"})
@DisplayName("GestionSuplenciaService unit tests")
@ExtendWith(MockitoExtension.class)
class GestionSuplenciaServiceTest {

    @Mock
    private SuplenciaRepository suplenciaRepository;

    private GestionSuplenciaService gestionSuplenciaService;

    private Persona escribanoSolicitado;
    private Persona suplente;
    private Date fechaGestion;

    @BeforeEach
    void setUp() {
        gestionSuplenciaService = new GestionSuplenciaService(suplenciaRepository);

        escribanoSolicitado = new Persona();
        escribanoSolicitado.setIdPersona(10);
        escribanoSolicitado.setNombre("Escribano");
        escribanoSolicitado.setApellido("Solicitado");

        suplente = new Persona();
        suplente.setIdPersona(20);
        suplente.setNombre("Escribano");
        suplente.setApellido("Suplente");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2026, Calendar.JANUARY, 15, 0, 0, 0);
        fechaGestion = calendar.getTime();
    }

    @Test
    @DisplayName("Should assign requested escribano when no active suplencia exists")
    void shouldAssignRequestedEscribanoWhenNoActiveSuplencia() {
        when(suplenciaRepository
                .findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        eq(escribanoSolicitado.getIdPersona()), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        GestionSuplenciaService.EscribanoAsignado resultado =
                gestionSuplenciaService.resolverEscribano(escribanoSolicitado, fechaGestion);

        assertThat(resultado.escribano()).isEqualTo(escribanoSolicitado);
        assertThat(resultado.suplenciaAplicada()).isNull();
    }

    @Test
    @DisplayName("Should assign suplente when escribano has an active suplencia")
    void shouldAssignSuplenteWhenEscribanoHasActiveSuplencia() {
        Suplencia suplenciaActiva = new Suplencia(1, fechaGestion, fechaGestion);
        suplenciaActiva.setFkIdSuplantado(escribanoSolicitado);
        suplenciaActiva.setFkIdSuplente(suplente);
        when(suplenciaRepository
                .findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        eq(escribanoSolicitado.getIdPersona()), any(Date.class), any(Date.class)))
                .thenReturn(List.of(suplenciaActiva));

        GestionSuplenciaService.EscribanoAsignado resultado =
                gestionSuplenciaService.resolverEscribano(escribanoSolicitado, fechaGestion);

        assertThat(resultado.escribano()).isEqualTo(suplente);
        assertThat(resultado.suplenciaAplicada()).isEqualTo(suplenciaActiva);
        verify(suplenciaRepository)
                .findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        eq(escribanoSolicitado.getIdPersona()), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("Should record the redirection identifying requested and assigned escribanos")
    void shouldRecordRedirectionInObservaciones() {
        String observacion = gestionSuplenciaService.observacionRedireccion(escribanoSolicitado, suplente);

        assertThat(observacion)
                .contains(escribanoSolicitado.getNombre())
                .contains(escribanoSolicitado.getApellido())
                .contains(suplente.getNombre())
                .contains(suplente.getApellido());
    }
}
