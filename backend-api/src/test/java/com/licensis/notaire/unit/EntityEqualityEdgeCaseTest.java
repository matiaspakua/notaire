package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU01", "CU02", "CU03", "CU04", "CU17", "CU45", "CU58", "CU83"})
@DisplayName("Entity equality/hashCode edge cases")
class EntityEqualityEdgeCaseTest {

    @Nested
    @DisplayName("Presupuesto")
    class PresupuestoEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Presupuesto p = new Presupuesto();
            p.setIdPresupuesto(null);
            assertThat(p).isNotEqualTo(new Presupuesto(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Presupuesto p1 = new Presupuesto();
            p1.setIdPresupuesto(null);
            Presupuesto p2 = new Presupuesto();
            p2.setIdPresupuesto(null);
            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Presupuesto(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new Presupuesto(1)).isNotEqualTo("not a presupuesto");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            Presupuesto p = new Presupuesto();
            p.setIdPresupuesto(null);
            assertThat(p.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("hashCode is non-zero for non-null id")
        void hashCodeNonZeroForNonNullId() {
            assertThat(new Presupuesto(42).hashCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("toString includes id")
        void toStringIncludesId() {
            assertThat(new Presupuesto(7).toString()).contains("7");
        }
    }

    @Nested
    @DisplayName("Persona")
    class PersonaEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Persona()).isNotEqualTo(new Persona(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Persona()).isEqualTo(new Persona());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Persona(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new Persona(1)).isNotEqualTo("not a persona");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Persona().hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString includes id")
        void toStringIncludesId() {
            assertThat(new Persona(5).toString()).contains("5");
        }
    }

    @Nested
    @DisplayName("Tramite")
    class TramiteEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Tramite t = new Tramite();
            t.setIdTramite(null);
            assertThat(t).isNotEqualTo(new Tramite(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Tramite t1 = new Tramite();
            t1.setIdTramite(null);
            Tramite t2 = new Tramite();
            t2.setIdTramite(null);
            assertThat(t1).isEqualTo(t2);
        }
    }

    @Nested
    @DisplayName("Suplencia")
    class SuplenciaEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Suplencia()).isNotEqualTo(new Suplencia(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Suplencia()).isEqualTo(new Suplencia());
        }
    }

    @Nested
    @DisplayName("Copia")
    class CopiaEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Copia()).isNotEqualTo(new Copia(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Copia()).isEqualTo(new Copia());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Copia(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Copia().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Folio")
    class FolioEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Folio()).isNotEqualTo(new Folio(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Folio()).isEqualTo(new Folio());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Folio(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Folio().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Inmueble")
    class InmuebleEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Inmueble i = new Inmueble();
            i.setIdInmueble(null);
            assertThat(i).isNotEqualTo(new Inmueble(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Inmueble i1 = new Inmueble();
            i1.setIdInmueble(null);
            Inmueble i2 = new Inmueble();
            i2.setIdInmueble(null);
            assertThat(i1).isEqualTo(i2);
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Inmueble(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            Inmueble i = new Inmueble();
            i.setIdInmueble(null);
            assertThat(i.hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Item")
    class ItemEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Item item = new Item();
            item.setIdItem(null);
            assertThat(item).isNotEqualTo(new Item(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Item i1 = new Item();
            i1.setIdItem(null);
            Item i2 = new Item();
            i2.setIdItem(null);
            assertThat(i1).isEqualTo(i2);
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Item(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            Item i = new Item();
            i.setIdItem(null);
            assertThat(i.hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("TipoDeFolio")
    class TipoDeFolioEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new TipoDeFolio()).isNotEqualTo(new TipoDeFolio(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new TipoDeFolio()).isEqualTo(new TipoDeFolio());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new TipoDeFolio(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new TipoDeFolio().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("TipoIdentificacion")
    class TipoIdentificacionEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new TipoIdentificacion()).isNotEqualTo(new TipoIdentificacion(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new TipoIdentificacion()).isEqualTo(new TipoIdentificacion());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new TipoIdentificacion(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new TipoIdentificacion().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("RegistroAuditoria")
    class RegistroAuditoriaEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new RegistroAuditoria()).isNotEqualTo(new RegistroAuditoria(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new RegistroAuditoria()).isEqualTo(new RegistroAuditoria());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new RegistroAuditoria(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new RegistroAuditoria().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("WorkflowNode")
    class WorkflowNodeEqualityTests {

        @Test
        @DisplayName("null id is not equal to node with id")
        void nullIdNotEqualToNonNull() {
            assertThat(new WorkflowNode()).isNotEqualTo(new WorkflowNode(1));
        }

        @Test
        @DisplayName("two null-id nodes are not equal (WorkflowNode requires non-null id)")
        void twoNullIdNodesAreNotEqual() {
            assertThat(new WorkflowNode()).isNotEqualTo(new WorkflowNode());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new WorkflowNode(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new WorkflowNode(1)).isNotEqualTo("not a node");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new WorkflowNode().hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString is not blank for node with id")
        void toStringNotBlank() {
            assertThat(new WorkflowNode(3).toString()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("WorkflowTransition")
    class WorkflowTransitionEqualityTests {

        @Test
        @DisplayName("null id is not equal to transition with id")
        void nullIdNotEqualToNonNull() {
            assertThat(new WorkflowTransition()).isNotEqualTo(new WorkflowTransition(1));
        }

        @Test
        @DisplayName("two null-id transitions are not equal (WorkflowTransition requires non-null id)")
        void twoNullIdTransitionsAreNotEqual() {
            assertThat(new WorkflowTransition()).isNotEqualTo(new WorkflowTransition());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new WorkflowTransition(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new WorkflowTransition(1)).isNotEqualTo("not a transition");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new WorkflowTransition().hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString is not blank for transition with id")
        void toStringNotBlank() {
            assertThat(new WorkflowTransition(8).toString()).isNotBlank();
        }
    }
}
