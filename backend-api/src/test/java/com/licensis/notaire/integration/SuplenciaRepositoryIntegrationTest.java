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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Suplencia Repository Integration Tests")
class SuplenciaRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private SuplenciaRepository suplenciaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private Persona suplente;
    private Persona suplantado;
    private TipoIdentificacion tipoIdentificacion;

    @BeforeEach
    void setUp() {
        suplenciaRepository.deleteAll();
        personaRepository.deleteAll();

        tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacion.setCaracteres("8");
        tipoIdentificacion = tipoIdentificacionRepository.save(tipoIdentificacion);

        suplente = new Persona();
        suplente.setNombre("Juan");
        suplente.setApellido("Suplente");
        suplente.setNumeroIdentificacion("12345678");
        suplente.setEsCliente(true);
        suplente.setFkIdTipoIdentificacion(tipoIdentificacion);
        suplente = personaRepository.save(suplente);

        suplantado = new Persona();
        suplantado.setNombre("Pedro");
        suplantado.setApellido("Suplantado");
        suplantado.setNumeroIdentificacion("87654321");
        suplantado.setEsCliente(true);
        suplantado.setFkIdTipoIdentificacion(tipoIdentificacion);
        suplantado = personaRepository.save(suplantado);
    }

    @Test
    @DisplayName("Should persist suplencia with all fields")
    void shouldPersistSuplenciaWithAllFields() {
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 1, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 6, 30)));
        suplencia.setObservaciones("Suplencia de prueba");

        Suplencia saved = suplenciaRepository.save(suplencia);

        assertThat(saved.getIdSuplencia()).isNotNull();
        assertThat(saved.getFkIdSuplente().getIdPersona()).isEqualTo(suplente.getIdPersona());
        assertThat(saved.getFkIdSuplantado().getIdPersona()).isEqualTo(suplantado.getIdPersona());
        assertThat(saved.getObservaciones()).isEqualTo("Suplencia de prueba");
    }

    @Test
    @DisplayName("Should retrieve suplencia by ID")
    void shouldRetrieveSuplenciaById() {
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 3, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 9, 30)));
        Suplencia saved = suplenciaRepository.save(suplencia);

        Optional<Suplencia> found = suplenciaRepository.findById(saved.getIdSuplencia());

        assertThat(found).isPresent();
        assertThat(found.get().getObservaciones()).isNull();
    }

    @Test
    @DisplayName("Should find all suplencias")
    void shouldFindAllSuplencias() {
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 1, 15)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 12, 15)));
        suplenciaRepository.save(suplencia);

        List<Suplencia> all = suplenciaRepository.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Should update suplencia")
    void shouldUpdateSuplencia() {
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 2, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 2, 28)));
        Suplencia saved = suplenciaRepository.save(suplencia);

        saved.setObservaciones("Actualizado");
        saved.setFechaFin(toDate(LocalDate.of(2026, 3, 31)));
        Suplencia updated = suplenciaRepository.save(saved);

        assertThat(updated.getObservaciones()).isEqualTo("Actualizado");

        Optional<Suplencia> fetched = suplenciaRepository.findById(saved.getIdSuplencia());
        assertThat(fetched.get().getObservaciones()).isEqualTo("Actualizado");
    }

    @Test
    @DisplayName("Should delete suplencia")
    void shouldDeleteSuplencia() {
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 4, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 4, 30)));
        Suplencia saved = suplenciaRepository.save(suplencia);
        Integer id = saved.getIdSuplencia();

        suplenciaRepository.delete(saved);

        Optional<Suplencia> deleted = suplenciaRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should handle null observaciones")
    void shouldHandleNullObservaciones() {
        Suplencia suplencia = new Suplencia();
        suplencia.setFkIdSuplente(suplente);
        suplencia.setFkIdSuplantado(suplantado);
        suplencia.setFechaInicio(toDate(LocalDate.of(2026, 5, 1)));
        suplencia.setFechaFin(toDate(LocalDate.of(2026, 5, 31)));
        suplencia.setObservaciones(null);

        Suplencia saved = suplenciaRepository.save(suplencia);

        assertThat(saved.getObservaciones()).isNull();
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
