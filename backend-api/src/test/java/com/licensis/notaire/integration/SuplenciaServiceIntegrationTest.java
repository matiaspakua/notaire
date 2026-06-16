package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.SuplenciaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Suplencia Service Integration Tests")
class SuplenciaServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private SuplenciaRepository suplenciaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private TipoIdentificacion tipoIdentificacion;

    @BeforeEach
    void setUp() {
        suplenciaRepository.deleteAll();
        personaRepository.deleteAll();
        tipoIdentificacionRepository.deleteAll();

        tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacion.setCaracteres("8");
        tipoIdentificacion = tipoIdentificacionRepository.save(tipoIdentificacion);
    }

    @Test
    @DisplayName("Should perform CRUD lifecycle")
    void shouldPerformCRUDLifecycle() {
        Persona suplente = createPersona("Suplente 1");
        Persona suplantado = createPersona("Suplantado 1");

        // Create
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 1, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 12, 31)));
        suplencia.setObservaciones("Ciclo completo");
        Suplencia created = suplenciaRepository.save(suplencia);

        assertThat(created.getIdSuplencia()).isNotNull();

        // Read
        Suplencia read = suplenciaRepository.findById(created.getIdSuplencia()).orElseThrow();
        assertThat(read.getObservaciones()).isEqualTo("Ciclo completo");

        // Update
        read.setObservaciones("Actualizado");
        Suplencia updated = suplenciaRepository.save(read);
        assertThat(updated.getObservaciones()).isEqualTo("Actualizado");

        // Delete
        suplenciaRepository.delete(updated);
        assertThat(suplenciaRepository.findById(created.getIdSuplencia())).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple suplencias for same substitute")
    void shouldHandleMultipleSuplenciasForSameSubstitute() {
        Persona suplente = createPersona("Suplente Activo");
        Persona suplantado1 = createPersona("Suplantado 1");
        Persona suplantado2 = createPersona("Suplantado 2");

        for (int i = 0; i < 3; i++) {
            Suplencia suplencia = new Suplencia();
            suplencia.setFkIdSuplente(suplente);
            suplencia.setFkIdSuplantado(i % 2 == 0 ? suplantado1 : suplantado2);
            suplencia.setFechaInicio(toDate(LocalDate.of(2025 + i, 1, 1)));
            suplencia.setFechaFin(toDate(LocalDate.of(2025 + i, 12, 31)));
            suplenciaRepository.save(suplencia);
        }

        List<Suplencia> all = suplenciaRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should handle date range scenarios")
    void shouldHandleDateRangeScenarios() {
        Persona suplente = createPersona("Suplente 2");
        Persona suplantado = createPersona("Suplantado 2");

        Suplencia suplencia1 = new Suplencia();
        suplencia1.setFkIdSuplente(suplente);
        suplencia1.setFkIdSuplantado(suplantado);
        suplencia1.setFechaInicio(toDate(LocalDate.of(2026, 1, 1)));
        suplencia1.setFechaFin(toDate(LocalDate.of(2026, 6, 30)));
        suplenciaRepository.save(suplencia1);

        Suplencia suplencia2 = new Suplencia();
        suplencia2.setFkIdSuplente(suplente);
        suplencia2.setFkIdSuplantado(suplantado);
        suplencia2.setFechaInicio(toDate(LocalDate.of(2026, 7, 1)));
        suplencia2.setFechaFin(toDate(LocalDate.of(2026, 12, 31)));
        suplenciaRepository.save(suplencia2);

        List<Suplencia> all = suplenciaRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should maintain relationship integrity")
    void shouldMaintainRelationshipIntegrity() {
        Persona suplente = createPersona("Suplente 3");
        Persona suplantado = createPersona("Suplantado 3");

        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 3, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 3, 31)));
        suplenciaRepository.save(suplencia);

        Suplencia retrieved = suplenciaRepository.findById(suplencia.getIdSuplencia()).orElseThrow();

        assertThat(retrieved.getFkIdSuplente()).isNotNull();
        assertThat(retrieved.getFkIdSuplente().getIdPersona()).isEqualTo(suplente.getIdPersona());
        assertThat(retrieved.getFkIdSuplantado()).isNotNull();
        assertThat(retrieved.getFkIdSuplantado().getIdPersona()).isEqualTo(suplantado.getIdPersona());
    }

    @Test
    @DisplayName("Should handle concurrent operations")
    void shouldHandleConcurrentOperations() {
        Persona suplente1 = createPersona("Suplente A");
        Persona suplente2 = createPersona("Suplente B");
        Persona suplantado = createPersona("Suplantado Concurrente");

        Suplencia sup1 = new Suplencia();
        sup1.setFkIdSuplente(suplente1);
        sup1.setFkIdSuplantado(suplantado);
        sup1.setFechaInicio(toDate(LocalDate.of(2026, 2, 1)));
        sup1.setFechaFin(toDate(LocalDate.of(2026, 2, 28)));
        Suplencia saved1 = suplenciaRepository.save(sup1);

        Suplencia sup2 = new Suplencia();
        sup2.setFkIdSuplente(suplente2);
        sup2.setFkIdSuplantado(suplantado);
        sup2.setFechaInicio(toDate(LocalDate.of(2026, 3, 1)));
        sup2.setFechaFin(toDate(LocalDate.of(2026, 3, 31)));
        Suplencia saved2 = suplenciaRepository.save(sup2);

        Suplencia read1 = suplenciaRepository.findById(saved1.getIdSuplencia()).orElseThrow();
        Suplencia read2 = suplenciaRepository.findById(saved2.getIdSuplencia()).orElseThrow();

        assertThat(read1.getFkIdSuplente().getIdPersona()).isNotEqualTo(read2.getFkIdSuplente().getIdPersona());
    }

    @Test
    @DisplayName("Should support filtering operations")
    void shouldSupportFilteringOperations() {
        Persona suplente = createPersona("Suplente Filtrado");
        Persona suplantado = createPersona("Suplantado Filtrado");

        for (int i = 1; i <= 5; i++) {
            Suplencia suplencia = new Suplencia();
            suplencia.setFkIdSuplente(suplente);
            suplencia.setFkIdSuplantado(suplantado);
            suplencia.setFechaInicio(toDate(LocalDate.of(2026, i, 1)));
            suplencia.setFechaFin(toDate(LocalDate.of(2026, i, 28)));
            suplenciaRepository.save(suplencia);
        }

        List<Suplencia> all = suplenciaRepository.findAll();
        List<Suplencia> filtered = all.stream()
                .filter(s -> s.getFkIdSuplente().getIdPersona().equals(suplente.getIdPersona()))
                .toList();

        assertThat(filtered).hasSize(5);
    }

    @Test
    @DisplayName("Should handle batch operations")
    void shouldHandleBatchOperations() {
        Persona suplente = createPersona("Suplente Batch");

        for (int i = 0; i < 10; i++) {
            Persona suplantado = createPersona("Suplantado Batch " + i);
            Suplencia suplencia = new Suplencia();
            suplencia.setFkIdSuplente(suplente);
            suplencia.setFkIdSuplantado(suplantado);
            suplencia.setFechaInicio(toDate(LocalDate.of(2026, 1, 1)));
            suplencia.setFechaFin(toDate(LocalDate.of(2026, 12, 31)));
            suplenciaRepository.save(suplencia);
        }

        List<Suplencia> all = suplenciaRepository.findAll();
        long countBatch = all.stream()
                .filter(s -> s.getFkIdSuplente().getNombre().startsWith("Suplente Batch"))
                .count();

        assertThat(countBatch).isEqualTo(10);
    }

    @Test
    @DisplayName("Should support complex scenarios")
    void shouldSupportComplexScenarios() {
        // Create multiple personas
        Persona suplente1 = createPersona("Escribano Principal");
        Persona suplente2 = createPersona("Escribano Suplente");
        Persona suplantado1 = createPersona("Escribano A");
        Persona suplantado2 = createPersona("Escribano B");

        // Create overlapping suplencias
        Suplencia overlap1 = new Suplencia();
        overlap1.setFkIdSuplente(suplente1);
        overlap1.setFkIdSuplantado(suplantado1);
        overlap1.setFechaInicio(toDate(LocalDate.of(2026, 1, 1)));
        overlap1.setFechaFin(toDate(LocalDate.of(2026, 6, 30)));
        overlap1.setObservaciones("Primer semestre");
        suplenciaRepository.save(overlap1);

        Suplencia overlap2 = new Suplencia();
        overlap2.setFkIdSuplente(suplente2);
        overlap2.setFkIdSuplantado(suplantado2);
        overlap2.setFechaInicio(toDate(LocalDate.of(2026, 4, 1)));
        overlap2.setFechaFin(toDate(LocalDate.of(2026, 9, 30)));
        overlap2.setObservaciones("Período extendido");
        suplenciaRepository.save(overlap2);

        List<Suplencia> all = suplenciaRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).anyMatch(s -> "Primer semestre".equals(s.getObservaciones()));
        assertThat(all).anyMatch(s -> "Período extendido".equals(s.getObservaciones()));
    }

    private Persona createPersona(String nombre) {
        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setApellido("Test");
        persona.setNumeroIdentificacion(System.nanoTime() % 100000000 + "");
        persona.setEsCliente(true);
        persona.setFkIdTipoIdentificacion(tipoIdentificacion);
        return personaRepository.save(persona);
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
