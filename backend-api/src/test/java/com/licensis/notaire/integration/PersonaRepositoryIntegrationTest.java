package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Persona Repository Integration Tests")
class PersonaRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private TipoIdentificacion tipoId;

    @BeforeEach
    void setUp() {
        tipoId = new TipoIdentificacion();
        tipoId.setNombre("DNI");
        tipoId.setCaracteres("8");
        tipoId = tipoIdentificacionRepository.save(tipoId);
    }

    @Test
    @DisplayName("Should create persona with required fields")
    void shouldCreatePersonaWithRequiredFields() {
        Persona persona = new Persona();
        persona.setNombre("Juan");
        persona.setApellido("Pérez");
        persona.setNumeroIdentificacion("12345678");
        persona.setEsCliente(true);
        persona.setFkIdTipoIdentificacion(tipoId);

        Persona saved = personaRepository.save(persona);

        assertThat(saved.getIdPersona()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("Juan");
        assertThat(saved.getApellido()).isEqualTo("Pérez");
        assertThat(saved.getNumeroIdentificacion()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Should retrieve persona by ID")
    void shouldRetrievePersonaById() {
        Persona persona = new Persona();
        persona.setNombre("María");
        persona.setApellido("García");
        persona.setNumeroIdentificacion("87654321");
        persona.setEsCliente(false);
        persona.setFkIdTipoIdentificacion(tipoId);
        Persona saved = personaRepository.save(persona);

        Optional<Persona> found = personaRepository.findById(saved.getIdPersona());

        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("María");
        assertThat(found.get().getFkIdTipoIdentificacion()).isNotNull();
    }

    @Test
    @DisplayName("Should update persona data")
    void shouldUpdatePersonaData() {
        Persona persona = new Persona();
        persona.setNombre("Carlos");
        persona.setApellido("López");
        persona.setNumeroIdentificacion("11111111");
        persona.setEsCliente(true);
        persona.setFkIdTipoIdentificacion(tipoId);
        Persona saved = personaRepository.save(persona);

        saved.setNombre("Carlos Alberto");
        saved.setEsCliente(false);
        Persona updated = personaRepository.save(saved);

        assertThat(updated.getNombre()).isEqualTo("Carlos Alberto");
        assertThat(updated.getEsCliente()).isFalse();
    }

    @Test
    @DisplayName("Should handle optional fields")
    void shouldHandleOptionalFields() {
        Persona persona = new Persona();
        persona.setNombre("Ana");
        persona.setApellido("Martínez");
        persona.setNumeroIdentificacion("22222222");
        persona.setEsCliente(true);
        persona.setFkIdTipoIdentificacion(tipoId);
        persona.setDomicilio("Calle Falsa 123");
        persona.setTelefono("123-4567");
        persona.setEMail("ana@example.com");

        Persona saved = personaRepository.save(persona);

        assertThat(saved.getDomicilio()).isEqualTo("Calle Falsa 123");
        assertThat(saved.getTelefono()).isEqualTo("123-4567");
        assertThat(saved.getEMail()).isEqualTo("ana@example.com");
    }

    @Test
    @DisplayName("Should support multiple personas")
    void shouldSupportMultiplePersonas() {
        String apellidoUnico = "Apellido" + System.nanoTime();
        for (int i = 0; i < 5; i++) {
            Persona persona = new Persona();
            persona.setNombre("Nombre" + i);
            persona.setApellido(apellidoUnico + i);
            persona.setNumeroIdentificacion("ID" + (10000000 + i));
            persona.setEsCliente(i % 2 == 0);
            persona.setFkIdTipoIdentificacion(tipoId);
            personaRepository.save(persona);
        }

        List<Persona> creadas = personaRepository.findAll().stream()
                .filter(p -> p.getApellido() != null && p.getApellido().startsWith(apellidoUnico))
                .toList();
        assertThat(creadas).hasSize(5);

        long clientes = creadas.stream().filter(Persona::getEsCliente).count();
        assertThat(clientes).isEqualTo(3);
    }
}
