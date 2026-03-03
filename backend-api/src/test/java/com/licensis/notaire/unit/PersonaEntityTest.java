package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Persona Entity Tests")
class PersonaEntityTest {

    @Nested
    @DisplayName("CU17 - Dar Alta Persona - Unit Tests")
    class DarAltaPersonaTests {

        @Test
        @DisplayName("Should create persona with required fields")
        void shouldCreatePersonaWithRequiredFields() {
            TipoIdentificacion tipoId = new TipoIdentificacion();
            tipoId.setIdTipoIdentificacion(1);
            tipoId.setNombre("DNI");

            Persona persona = new Persona();
            persona.setIdPersona(1);
            persona.setNombre("Juan");
            persona.setApellido("Perez");
            persona.setNumeroIdentificacion("12345678");
            persona.setFkIdTipoIdentificacion(tipoId);
            persona.setEsCliente(false);

            assertThat(persona.getNombre()).isEqualTo("Juan");
            assertThat(persona.getApellido()).isEqualTo("Perez");
            assertThat(persona.getNumeroIdentificacion()).isEqualTo("12345678");
            assertThat(persona.getEsCliente()).isFalse();
        }

        @Test
        @DisplayName("Should create cliente with all fields")
        void shouldCreateClienteWithAllFields() {
            TipoIdentificacion tipoId = new TipoIdentificacion();
            tipoId.setIdTipoIdentificacion(1);
            tipoId.setNombre("DNI");

            Persona persona = new Persona();
            persona.setIdPersona(1);
            persona.setNombre("Maria");
            persona.setApellido("Gonzalez");
            persona.setNumeroIdentificacion("87654321");
            persona.setFkIdTipoIdentificacion(tipoId);
            persona.setEsCliente(true);
            persona.setNacionalidad("Argentina");
            persona.setFechaNacimiento(new Date(1990 - 1900, 5, 15));
            persona.setCuit("27-87654321-5");
            persona.setEstadoCivil("soltero");
            persona.setSexo("femenino");
            persona.setOcupacion("empleada");
            persona.setDomicilio("Calle Falsa 123");

            assertThat(persona.getEsCliente()).isTrue();
            assertThat(persona.getNacionalidad()).isEqualTo("Argentina");
            assertThat(persona.getCuit()).isEqualTo("27-87654321-5");
            assertThat(persona.getEstadoCivil()).isEqualTo("soltero");
        }

        @Test
        @DisplayName("Should create escribano with registro")
        void shouldCreateEscribanoWithRegistro() {
            TipoIdentificacion tipoId = new TipoIdentificacion();
            tipoId.setIdTipoIdentificacion(1);
            tipoId.setNombre("DNI");

            Persona escribano = new Persona();
            escribano.setIdPersona(1);
            escribano.setNombre("Juan Carlos");
            escribano.setApellido("Garcia");
            escribano.setNumeroIdentificacion("20123456");
            escribano.setFkIdTipoIdentificacion(tipoId);
            escribano.setRegistroEscribano(1001);
            escribano.setEsCliente(false);

            assertThat(escribano.getRegistroEscribano()).isEqualTo(1001);
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Persona p1 = new Persona(1);
            Persona p2 = new Persona(1);
            Persona p3 = new Persona(2);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }

        @Test
        @DisplayName("Should implement hashCode based on id")
        void shouldImplementHashCodeBasedOnId() {
            Persona p1 = new Persona(1);
            Persona p2 = new Persona(1);

            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }
    }

    @Nested
    @DisplayName("CU61 - Buscar persona o cliente - Unit Tests")
    class BuscarPersonaTests {

        @Test
        @DisplayName("Should filter personas by nombre")
        void shouldFilterPersonasByNombre() {
            List<Persona> personas = createTestPersonas();
            
            String filter = "Juan";
            List<Persona> filtered = personas.stream()
                .filter(p -> p.getNombre() != null && p.getNombre().contains(filter))
                .toList();

            assertThat(filtered).hasSize(1);
            assertThat(filtered.get(0).getApellido()).isEqualTo("Perez");
        }

        @Test
        @DisplayName("Should filter personas by apellido")
        void shouldFilterPersonasByApellido() {
            List<Persona> personas = createTestPersonas();
            
            String filter = "Garcia";
            List<Persona> filtered = personas.stream()
                .filter(p -> p.getApellido() != null && p.getApellido().contains(filter))
                .toList();

            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should filter only clientes")
        void shouldFilterOnlyClientes() {
            List<Persona> personas = createTestPersonas();
            
            List<Persona> clientes = personas.stream()
                .filter(Persona::getEsCliente)
                .toList();

            assertThat(clientes).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by tipo identificacion")
        void shouldFilterByTipoIdentificacion() {
            List<Persona> personas = createTestPersonas();
            
            TipoIdentificacion dni = new TipoIdentificacion();
            dni.setIdTipoIdentificacion(1);
            
            List<Persona> filtered = personas.stream()
                .filter(p -> p.getFkIdTipoIdentificacion() != null 
                    && p.getFkIdTipoIdentificacion().getIdTipoIdentificacion().equals(1))
                .toList();

            assertThat(filtered).hasSize(3);
        }
    }

    private List<Persona> createTestPersonas() {
        TipoIdentificacion dni = new TipoIdentificacion();
        dni.setIdTipoIdentificacion(1);
        dni.setNombre("DNI");

        Persona p1 = new Persona(1, "Juan", "Perez", "12345678", false);
        p1.setFkIdTipoIdentificacion(dni);

        Persona p2 = new Persona(2, "Maria", "Garcia", "87654321", true);
        p2.setFkIdTipoIdentificacion(dni);

        Persona p3 = new Persona(3, "Carlos", "Garcia", "11222333", false);
        p3.setFkIdTipoIdentificacion(dni);

        List<Persona> personas = new ArrayList<>();
        personas.add(p1);
        personas.add(p2);
        personas.add(p3);
        
        return personas;
    }
}
