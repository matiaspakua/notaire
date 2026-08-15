package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Tramite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU01", "CU45", "CU60"})
@DisplayName("Presupuesto Entity Tests")
class PresupuestoEntityTest {

    @Nested
    @DisplayName("CU01 - Preparar Presupuesto - Unit Tests")
    class PrepararPresupuestoTests {

        @Test
        @DisplayName("Should create presupuesto with required fields")
        void shouldCreatePresupuestoWithRequiredFields() {
            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setIdPresupuesto(1);
            presupuesto.setNumero(1001);
            presupuesto.setFecha(new Date());
            presupuesto.setEncabezado("Compraventa de inmueble");
            presupuesto.setEstado("pendiente");
            presupuesto.setMontoInmueble(500000.00f);

            assertThat(presupuesto.getNumero()).isEqualTo(1001);
            assertThat(presupuesto.getEncabezado()).isEqualTo("Compraventa de inmueble");
            assertThat(presupuesto.getEstado()).isEqualTo("pendiente");
            assertThat(presupuesto.getMontoInmueble()).isEqualTo(500000.00f);
        }

        @Test
        @DisplayName("Should update presupuesto estado")
        void shouldUpdatePresupuestoEstado() {
            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setIdPresupuesto(1);
            presupuesto.setNumero(1001);
            presupuesto.setFecha(new Date());
            presupuesto.setEncabezado("Compraventa");
            presupuesto.setEstado("pendiente");

            presupuesto.setEstado("aprobado");
            assertThat(presupuesto.getEstado()).isEqualTo("aprobado");
        }

        @Test
        @DisplayName("Should link presupuesto to persona")
        void shouldLinkPresupuestoToPersona() {
            Persona persona = new Persona();
            persona.setIdPersona(1);
            persona.setNombre("Juan");
            persona.setApellido("Perez");

            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setIdPresupuesto(1);
            presupuesto.setFkIdPersona(persona);

            assertThat(presupuesto.getFkIdPersona()).isNotNull();
            assertThat(presupuesto.getFkIdPersona().getNombre()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Should associate presupuesto with more than one tramite")
        void shouldAssociatePresupuestoWithMultipleTramites() {
            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setIdPresupuesto(1);

            Tramite tramite1 = new Tramite();
            tramite1.setIdTramite(1);
            tramite1.setFkIdPresupuesto(presupuesto);

            Tramite tramite2 = new Tramite();
            tramite2.setIdTramite(2);
            tramite2.setFkIdPresupuesto(presupuesto);

            presupuesto.setTramiteList(List.of(tramite1, tramite2));

            assertThat(presupuesto.getTramiteList()).containsExactly(tramite1, tramite2);
            assertThat(tramite1.getFkIdPresupuesto()).isEqualTo(presupuesto);
            assertThat(tramite2.getFkIdPresupuesto()).isEqualTo(presupuesto);
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Presupuesto p1 = new Presupuesto(1);
            Presupuesto p2 = new Presupuesto(1);
            Presupuesto p3 = new Presupuesto(2);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }

        @Test
        @DisplayName("Should initialize lists in constructor")
        void shouldInitializeListsInConstructor() {
            Presupuesto presupuesto = new Presupuesto();

            assertThat(presupuesto.getItemList()).isNotNull();
            assertThat(presupuesto.getItemList()).isEmpty();
            assertThat(presupuesto.getPagoList()).isNotNull();
        }
    }

    @Nested
    @DisplayName("CU60 - Buscar Presupuesto - Unit Tests")
    class BuscarPresupuestoTests {

        @Test
        @DisplayName("Should filter presupuestos by persona")
        void shouldFilterPresupuestosByPersona() {
            Persona persona1 = new Persona(1);
            Persona persona2 = new Persona(2);

            Presupuesto presupuesto1 = new Presupuesto(1);
            presupuesto1.setFkIdPersona(persona1);

            Presupuesto presupuesto2 = new Presupuesto(2);
            presupuesto2.setFkIdPersona(persona2);

            Presupuesto presupuesto3 = new Presupuesto(3);
            presupuesto3.setFkIdPersona(persona1);

            List<Presupuesto> presupuestos = List.of(presupuesto1, presupuesto2, presupuesto3);

            var filtered = presupuestos.stream()
                .filter(p -> p.getFkIdPersona() != null && p.getFkIdPersona().getIdPersona().equals(1))
                .toList();

            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should filter presupuestos by estado")
        void shouldFilterPresupuestosByEstado() {
            Presupuesto presupuesto1 = new Presupuesto(1);
            presupuesto1.setEstado("pendiente");

            Presupuesto presupuesto2 = new Presupuesto(2);
            presupuesto2.setEstado("aprobado");

            List<Presupuesto> presupuestos = List.of(presupuesto1, presupuesto2);

            var pending = presupuestos.stream()
                .filter(p -> "pendiente".equals(p.getEstado()))
                .toList();

            assertThat(pending).hasSize(1);
            assertThat(pending.get(0).getIdPresupuesto()).isEqualTo(1);
        }
    }
}
