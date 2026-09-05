package com.licensis.notaire.integration;

import com.licensis.notaire.api.HistorialController;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the historial delete silently no-op-ing (CU13 audit trail).
 *
 * <p>The bug only reproduces when the row being deleted was created and committed in a
 * prior, separate transaction — exactly what happens across two real HTTP requests. A
 * single {@code @Transactional} test method that creates and deletes the row in one
 * persistence context does not exercise the failure: it must be split into two
 * transactions via {@link TestTransaction} to mirror production.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
class HistorialDeleteIntegrationTest {

    @Autowired
    private HistorialController historialController;
    @Autowired
    private HistorialRepository historialRepository;
    @Autowired
    private GestionDeEscrituraRepository gestionRepository;
    @Autowired
    private EstadoDeGestionRepository estadoRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    @Test
    @Transactional
    @DisplayName("Should delete historial row created in a prior, already-committed transaction")
    void shouldDeleteHistorialCreatedInPriorTransaction() {
        Integer id = createAndCommitHistorial();

        ResponseEntity<Void> response = historialController.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(historialRepository.existsById(id)).isFalse();
    }

    private Integer createAndCommitHistorial() {
        TipoIdentificacion tipo = tipoIdentificacionRepository.findById(1).orElseThrow();

        Persona persona = new Persona();
        persona.setNombre("X");
        persona.setApellido("Y");
        persona.setNumeroIdentificacion("1");
        persona.setFkIdTipoIdentificacion(tipo);
        persona = personaRepository.save(persona);

        EstadoDeGestion estado = estadoRepository.findById(1).orElseThrow();

        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setFechaInicio(new Date());
        gestion.setNumero(1);
        gestion.setEncabezado("test");
        gestion.setFkIdPersonaEscribano(persona);
        gestion.setFkIdEstadoDeGestion(estado);
        gestion = gestionRepository.save(gestion);

        Historial historial = new Historial();
        historial.setFecha(new Date());
        historial.setObservaciones("obs");
        historial.setFkIdGestion(gestion);
        historial.setFkIdEstadoGestion(estado);
        historial = historialRepository.save(historial);

        Integer id = historial.getIdHistorial();

        // Commit and start a fresh transaction so the row above is genuinely persisted
        // and reloaded from scratch by the code under test, matching a real HTTP request
        // that arrives after the row was created by an earlier, unrelated request.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        return id;
    }
}
