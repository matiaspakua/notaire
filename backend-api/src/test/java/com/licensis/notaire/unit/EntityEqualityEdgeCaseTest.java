package com.licensis.notaire.unit;

import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
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
    class BudgetEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Budget p = new Budget();
            p.setIdBudget(null);
            assertThat(p).isNotEqualTo(new Budget(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Budget p1 = new Budget();
            p1.setIdBudget(null);
            Budget p2 = new Budget();
            p2.setIdBudget(null);
            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Budget(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new Budget(1)).isNotEqualTo("not a budget");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            Budget p = new Budget();
            p.setIdBudget(null);
            assertThat(p.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("hashCode is non-zero for non-null id")
        void hashCodeNonZeroForNonNullId() {
            assertThat(new Budget(42).hashCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("toString includes id")
        void toStringIncludesId() {
            assertThat(new Budget(7).toString()).contains("7");
        }
    }

    @Nested
    @DisplayName("Persona")
    class PersonEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Person()).isNotEqualTo(new Person(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Person()).isEqualTo(new Person());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Person(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new Person(1)).isNotEqualTo("not a person");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Person().hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString includes id")
        void toStringIncludesId() {
            assertThat(new Person(5).toString()).contains("5");
        }
    }

    @Nested
    @DisplayName("Tramite")
    class ProcedureEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Procedure t = new Procedure();
            t.setIdProcedure(null);
            assertThat(t).isNotEqualTo(new Procedure(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Procedure t1 = new Procedure();
            t1.setIdProcedure(null);
            Procedure t2 = new Procedure();
            t2.setIdProcedure(null);
            assertThat(t1).isEqualTo(t2);
        }
    }

    @Nested
    @DisplayName("Suplencia")
    class SubstitutionEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Substitution()).isNotEqualTo(new Substitution(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Substitution()).isEqualTo(new Substitution());
        }
    }

    @Nested
    @DisplayName("Copia")
    class CopyEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Copy()).isNotEqualTo(new Copy(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Copy()).isEqualTo(new Copy());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Copy(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Copy().hashCode()).isEqualTo(0);
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
    class PropertyEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            Property i = new Property();
            i.setIdProperty(null);
            assertThat(i).isNotEqualTo(new Property(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            Property i1 = new Property();
            i1.setIdProperty(null);
            Property i2 = new Property();
            i2.setIdProperty(null);
            assertThat(i1).isEqualTo(i2);
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Property(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            Property i = new Property();
            i.setIdProperty(null);
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
    class FolioTypeEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new FolioType()).isNotEqualTo(new FolioType(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new FolioType()).isEqualTo(new FolioType());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new FolioType(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new FolioType().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("TipoIdentificacion")
    class IdentificationTypeEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new IdentificationType()).isNotEqualTo(new IdentificationType(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new IdentificationType()).isEqualTo(new IdentificationType());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new IdentificationType(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new IdentificationType().hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("RegistroAuditoria")
    class AuditRecordEqualityTests {

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new AuditRecord()).isNotEqualTo(new AuditRecord(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new AuditRecord()).isEqualTo(new AuditRecord());
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new AuditRecord(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new AuditRecord().hashCode()).isEqualTo(0);
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
