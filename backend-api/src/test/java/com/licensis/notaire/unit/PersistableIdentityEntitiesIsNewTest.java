package com.licensis.notaire.unit;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.Role;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.User;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Persistable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit coverage for {@link Persistable#isNew()} on the 25 entities with a surrogate
 * {@code @GeneratedValue(IDENTITY)} integer key (design.md — Decision 1). No Spring
 * context or database is needed: {@code isNew()} is derived purely from the id field.
 */
class PersistableIdentityEntitiesIsNewTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    @DisplayName("isNew() is true for a freshly constructed entity, false once the id is set")
    void isNewReflectsIdentityNotVersion(String name, Supplier<Persistable<Integer>> factory,
                                          Function<Persistable<Integer>, Persistable<Integer>> withIdSet) {
        Persistable<Integer> freshlyConstructed = factory.get();
        assertThat(freshlyConstructed.isNew()).isTrue();

        Persistable<Integer> withId = withIdSet.apply(factory.get());
        assertThat(withId.isNew()).isFalse();
    }

    static Stream<Arguments> entities() {
        return Stream.of(
                identityCase("Concepto", Concept::new, (Concept e) -> e.setIdConcept(1)),
                identityCase("Copia", Copy::new, (Copy e) -> e.setIdCopy(1)),
                identityCase("Cuaderno", Notebook::new, (Notebook e) -> e.setIdNotebook(1)),
                identityCase("DocumentoPresentado", SubmittedDocument::new,
                        (SubmittedDocument e) -> e.setIdSubmittedDocument(1)),
                identityCase("Escritura", Deed::new, (Deed e) -> e.setIdDeed(1)),
                identityCase("EstadoDeGestion", ManagementStatus::new, (ManagementStatus e) -> e.setIdManagementStatus(1)),
                identityCase("Folio", Folio::new, (Folio e) -> e.setIdFolio(1)),
                identityCase("GestionDeEscritura", DeedManagement::new,
                        (DeedManagement e) -> e.setIdManagement(1)),
                identityCase("Inmueble", Property::new, (Property e) -> e.setIdProperty(1)),
                identityCase("MinutaInscripcion", RegistrationDraft::new,
                        (RegistrationDraft e) -> e.setIdRegistrationDraft(1)),
                identityCase("MovimientoTestimonio", TestimonyMovement::new,
                        (TestimonyMovement e) -> e.setIdTestimonyMovement(1)),
                identityCase("Persona", Person::new, (Person e) -> e.setPersonId(1)),
                identityCase("Presupuesto", Budget::new, (Budget e) -> e.setIdBudget(1)),
                identityCase("RegistroAuditoria", AuditRecord::new,
                        (AuditRecord e) -> e.setIdAuditRecord(1)),
                identityCase("Rol", Role::new, (Role e) -> e.setIdRole(1)),
                identityCase("Suplencia", Substitution::new, (Substitution e) -> e.setIdSubstitution(1)),
                identityCase("Testimonio", Testimony::new, (Testimony e) -> e.setIdTestimony(1)),
                identityCase("TipoDeDocumento", DocumentType::new, (DocumentType e) -> e.setIdDocumentType(1)),
                identityCase("TipoDeFolio", FolioType::new, (FolioType e) -> e.setIdFolioType(1)),
                identityCase("TipoDeTramite", ProcedureType::new, (ProcedureType e) -> e.setIdProcedureType(1)),
                identityCase("TipoIdentificacion", IdentificationType::new,
                        (IdentificationType e) -> e.setIdIdentificationType(1)),
                identityCase("Usuario", User::new, (User e) -> e.setIdUser(1)),
                identityCase("WorkflowDefinition", WorkflowDefinition::new, (WorkflowDefinition e) -> e.setId(1)),
                identityCase("WorkflowNode", WorkflowNode::new, (WorkflowNode e) -> e.setId(1)),
                identityCase("WorkflowTransition", WorkflowTransition::new, (WorkflowTransition e) -> e.setId(1))
        );
    }

    @SuppressWarnings("unchecked")
    private static <T extends Persistable<Integer>> Arguments identityCase(
            String name, Supplier<T> factory, java.util.function.Consumer<T> idSetter) {
        Function<Persistable<Integer>, Persistable<Integer>> withIdSet = entity -> {
            idSetter.accept((T) entity);
            return entity;
        };
        return Arguments.of(name, (Supplier<Persistable<Integer>>) factory, withIdSet);
    }
}
