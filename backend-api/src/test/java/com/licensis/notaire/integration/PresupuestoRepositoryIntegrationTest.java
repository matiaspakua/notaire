package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Presupuesto Repository Integration Tests")
class PresupuestoRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private Persona persona;

    @BeforeEach
    void setUp() {
        TipoIdentificacion tipoId = new TipoIdentificacion();
        tipoId.setNombre("DNI");
        tipoId.setCaracteres("8");
        tipoId = tipoIdentificacionRepository.save(tipoId);

        persona = new Persona();
        persona.setNombre("Cliente");
        persona.setApellido("Test");
        persona.setNumeroIdentificacion("99999999");
        persona.setEsCliente(true);
        persona.setFkIdTipoIdentificacion(tipoId);
        persona = personaRepository.save(persona);
    }

    @Test
    @DisplayName("Should create presupuesto")
    void shouldCreatePresupuesto() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setNumero(1001);
        presupuesto.setFecha(new Date());
        presupuesto.setEncabezado("Presupuesto Test");
        presupuesto.setEstado("BORRADOR");
        presupuesto.setMontoInmueble(100000.0f);
        presupuesto.setFkIdPersona(persona);

        Presupuesto saved = presupuestoRepository.save(presupuesto);

        assertThat(saved.getIdPresupuesto()).isNotNull();
        assertThat(saved.getNumero()).isEqualTo(1001);
        assertThat(saved.getEstado()).isEqualTo("BORRADOR");
    }

    @Test
    @DisplayName("Should retrieve presupuesto by ID")
    void shouldRetrievePresupuestoById() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setNumero(2001);
        presupuesto.setFecha(new Date());
        presupuesto.setEncabezado("Presupuesto Recuperación");
        presupuesto.setEstado("PENDIENTE");
        presupuesto.setFkIdPersona(persona);
        Presupuesto saved = presupuestoRepository.save(presupuesto);

        Optional<Presupuesto> found = presupuestoRepository.findById(saved.getIdPresupuesto());

        assertThat(found).isPresent();
        assertThat(found.get().getNumero()).isEqualTo(2001);
        assertThat(found.get().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Should update presupuesto state")
    void shouldUpdatePresupuestoState() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setNumero(3001);
        presupuesto.setFecha(new Date());
        presupuesto.setEncabezado("Presupuesto Actualizable");
        presupuesto.setEstado("BORRADOR");
        presupuesto.setFkIdPersona(persona);
        Presupuesto saved = presupuestoRepository.save(presupuesto);

        saved.setEstado("APROBADO");
        saved.setObservaciones("Aprobado por el cliente");
        Presupuesto updated = presupuestoRepository.save(saved);

        assertThat(updated.getEstado()).isEqualTo("APROBADO");
        assertThat(updated.getObservaciones()).isEqualTo("Aprobado por el cliente");
    }

    @Test
    @DisplayName("Should support multiple presupuestos")
    void shouldSupportMultiplePresupuestos() {
        String encabezadoUnico = "Pres" + System.nanoTime();
        String[] estados = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "CANCELADO"};
        for (int i = 0; i < estados.length; i++) {
            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setNumero(4000 + i);
            presupuesto.setFecha(new Date());
            presupuesto.setEncabezado(encabezadoUnico + " " + estados[i]);
            presupuesto.setEstado(estados[i]);
            presupuesto.setFkIdPersona(persona);
            presupuestoRepository.save(presupuesto);
        }

        List<Presupuesto> creados = presupuestoRepository.findAll().stream()
                .filter(p -> p.getEncabezado() != null && p.getEncabezado().startsWith(encabezadoUnico))
                .toList();
        assertThat(creados).hasSize(5);

        List<Presupuesto> aprobados = creados.stream()
                .filter(p -> "APROBADO".equals(p.getEstado()))
                .toList();
        assertThat(aprobados).hasSize(1);
    }
}
