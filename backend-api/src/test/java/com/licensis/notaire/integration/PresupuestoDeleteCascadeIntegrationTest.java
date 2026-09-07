package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@code Presupuesto}'s
 * {@code @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)} collection of
 * {@link Pago} — the second highest-risk cascade shape identified in design.md "Riesgos"
 * for reverting a delete via Hibernate re-managing the loaded, cascaded children in the
 * same persistence context (#957).
 *
 * <p>Follows the same two-transaction shape as {@link HistorialDeleteIntegrationTest}:
 * the parent and child are created and committed in a prior transaction, matching a real
 * HTTP delete request that arrives after both rows already exist.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
class PresupuestoDeleteCascadeIntegrationTest {

    @Autowired
    private PresupuestoRepository presupuestoRepository;
    @Autowired
    private PagoRepository pagoRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    @Test
    @Transactional
    @DisplayName("Should delete presupuesto and cascade-delete its pago children")
    void shouldDeletePresupuestoWithCascadedChildren() {
        Integer idPresupuesto = createAndCommitPresupuestoWithPago();

        presupuestoRepository.deleteById(idPresupuesto);

        assertThat(presupuestoRepository.existsById(idPresupuesto)).isFalse();
        assertThat(pagoRepository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto)).isEmpty();
    }

    private Integer createAndCommitPresupuestoWithPago() {
        TipoIdentificacion tipo = tipoIdentificacionRepository.findById(1).orElseThrow();

        Persona persona = new Persona();
        persona.setNombre("X");
        persona.setApellido("Y");
        persona.setNumeroIdentificacion("cascade-test-1");
        persona.setFkIdTipoIdentificacion(tipo);
        persona = personaRepository.save(persona);

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setFecha(new Date());
        presupuesto.setNumero(1);
        presupuesto.setEstado("ACTIVO");
        presupuesto.setEncabezado("Cascade Test Presupuesto");
        presupuesto.setFkIdPersona(persona);
        presupuesto = presupuestoRepository.save(presupuesto);

        Pago pago = new Pago();
        pago.setMonto(100f);
        pago.setFecha(new Date());
        pago.setPresupuesto(presupuesto);
        pagoRepository.save(pago);

        Integer idPresupuesto = presupuesto.getIdPresupuesto();

        // Commit and start a fresh transaction so both rows above are genuinely
        // persisted and reloaded from scratch by the code under test, matching a real
        // HTTP delete request that arrives after they were created by an earlier request.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        return idPresupuesto;
    }
}
