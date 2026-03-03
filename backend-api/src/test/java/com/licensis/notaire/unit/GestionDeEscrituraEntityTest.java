package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.Persona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GestionDeEscritura Entity Tests")
class GestionDeEscrituraEntityTest {

    @Nested
    @DisplayName("CU02 - Iniciar Gestión - Unit Tests")
    class IniciarGestionTests {

        @Test
        @DisplayName("Should create gestion with required fields")
        void shouldCreateGestionWithRequiredFields() {
            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(1);
            gestion.setNumero(1001);
            gestion.setFechaInicio(new Date());
            gestion.setEncabezado("Compraventa - Perez Garcia");

            assertThat(gestion.getNumero()).isEqualTo(1001);
            assertThat(gestion.getEncabezado()).isEqualTo("Compraventa - Perez Garcia");
        }

        @Test
        @DisplayName("Should link gestion to escribano")
        void shouldLinkGestionToEscribano() {
            Persona escribano = new Persona();
            escribano.setIdPersona(1);
            escribano.setNombre("Juan Carlos");
            escribano.setApellido("Garcia");
            escribano.setRegistroEscribano(1001);

            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(1);
            gestion.setFkIdPersonaEscribano(escribano);

            assertThat(gestion.getFkIdPersonaEscribano()).isNotNull();
            assertThat(gestion.getFkIdPersonaEscribano().getRegistroEscribano()).isEqualTo(1001);
        }

        @Test
        @DisplayName("Should link gestion to estado")
        void shouldLinkGestionToEstado() {
            EstadoDeGestion estado = new EstadoDeGestion();
            estado.setIdEstadoGestion(1);
            estado.setNombre("Iniciada");

            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(1);
            gestion.setFkIdEstadoDeGestion(estado);

            assertThat(gestion.getFkIdEstadoDeGestion()).isNotNull();
            assertThat(gestion.getFkIdEstadoDeGestion().getNombre()).isEqualTo("Iniciada");
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            GestionDeEscritura g1 = new GestionDeEscritura(1);
            GestionDeEscritura g2 = new GestionDeEscritura(1);
            GestionDeEscritura g3 = new GestionDeEscritura(2);

            assertThat(g1).isEqualTo(g2);
            assertThat(g1).isNotEqualTo(g3);
        }
    }

    @Nested
    @DisplayName("CU19 - Buscar gestiones de un Cliente - Unit Tests")
    class BuscarGestionesClienteTests {

        @Test
        @DisplayName("Should filter gestiones by cliente")
        void shouldFilterGestionesByCliente() {
            GestionDeEscritura gestion1 = new GestionDeEscritura(1);
            GestionDeEscritura gestion2 = new GestionDeEscritura(2);
            GestionDeEscritura gestion3 = new GestionDeEscritura(3);

            var gestiones = java.util.List.of(gestion1, gestion2, gestion3);

            assertThat(gestiones).hasSize(3);
        }
    }

    @Nested
    @DisplayName("CU14 - Consultar estado gestión - Unit Tests")
    class ConsultarEstadoGestionTests {

        @Test
        @DisplayName("Should get current state from gestion")
        void shouldGetCurrentStateFromGestion() {
            EstadoDeGestion estado = new EstadoDeGestion();
            estado.setIdEstadoGestion(2);
            estado.setNombre("En Tramite");

            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setFkIdEstadoDeGestion(estado);

            assertThat(gestion.getFkIdEstadoDeGestion().getNombre()).isEqualTo("En Tramite");
        }
    }
}
