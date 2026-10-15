package com.licensis.notaire.unit.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.document.ManageSubmittedDocumentsUseCase.SaveSubmittedDocumentCommand;
import com.licensis.notaire.application.port.out.procedure.ProcedureLookupPort;
import com.licensis.notaire.application.usecase.document.SubmittedDocumentManagementService;
import com.licensis.notaire.application.usecase.document.SubmittedDocumentQueryService;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.SubmittedDocument;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.testing.InMemoryDocumentTypeCatalog;
import com.licensis.notaire.testing.InMemorySubmittedDocumentCatalog;

/** Submitted-document rules on plain objects: no Spring, no database (issue #1726). */
@DisplayName("Submitted document use cases (issue #1726)")
class SubmittedDocumentUseCasesTest {

    private InMemorySubmittedDocumentCatalog catalog;
    private InMemoryDocumentTypeCatalog types;
    private Set<Integer> procedureIds;
    private SubmittedDocumentManagementService manage;
    private SubmittedDocumentQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemorySubmittedDocumentCatalog();
        types = new InMemoryDocumentTypeCatalog();
        procedureIds = new HashSet<>();
        ProcedureLookupPort procedures = procedureIds::contains;
        manage = new SubmittedDocumentManagementService(catalog, types, procedures);
        query = new SubmittedDocumentQueryService(catalog);
    }

    private DocumentType seedType(boolean expires, Integer dueDays, String deliveredBy) {
        return types.save(DocumentType.create("Deed copy", expires, dueDays, deliveredBy, true, false));
    }

    private SaveSubmittedDocumentCommand createCmd(int typeId, int procedureId, LocalDate date, String deliveredBy) {
        return new SaveSubmittedDocumentCommand(typeId, date, false, procedureId, deliveredBy, "Doc", true);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("stores the document and inherits due fields from the type")
        void inheritsDue() {
            DocumentType type = seedType(true, 10, "Registry");
            procedureIds.add(7);
            LocalDate entry = LocalDate.of(2026, 9, 5);

            SubmittedDocument created = manage.create(createCmd(type.id(), 7, entry, null));

            assertThat(created.id()).isNotNull();
            assertThat(created.documentTypeId()).isEqualTo(type.id());
            assertThat(created.procedureId()).isEqualTo(7);
            assertThat(created.expires()).isTrue();
            assertThat(created.dueDays()).isEqualTo(10);
            assertThat(created.deliveredBy()).isEqualTo("Registry");
            assertThat(created.dateDue()).isEqualTo(entry.plusDays(10));
            assertThat(query.findById(created.id())).isPresent();
        }

        @Test
        @DisplayName("explicit deliveredBy overrides the type")
        void explicitDeliveredByWins() {
            DocumentType type = seedType(false, null, "Registry");
            procedureIds.add(1);

            SubmittedDocument created =
                    manage.create(createCmd(type.id(), 1, LocalDate.of(2026, 1, 1), "Client"));

            assertThat(created.deliveredBy()).isEqualTo("Client");
            assertThat(created.dateDue()).isNull();
        }

        @Test
        @DisplayName("unknown type is refused")
        void unknownType() {
            procedureIds.add(1);
            assertThatThrownBy(() -> manage.create(createCmd(999, 1, LocalDate.of(2026, 1, 1), null)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document type");
        }

        @Test
        @DisplayName("unknown procedure is refused")
        void unknownProcedure() {
            DocumentType type = seedType(false, null, "");
            assertThatThrownBy(() -> manage.create(createCmd(type.id(), 404, LocalDate.of(2026, 1, 1), null)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Procedure");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("partial update keeps omitted fields and recomputes due when asked")
        void partialAndRefreshDue() {
            DocumentType type = seedType(true, 5, "Office");
            procedureIds.add(3);
            SubmittedDocument created =
                    manage.create(createCmd(type.id(), 3, LocalDate.of(2026, 3, 1), null));

            manage.update(
                    created.id(),
                    new SaveSubmittedDocumentCommand(
                            null, LocalDate.of(2026, 3, 10), true, null, null, null, true));

            Optional<SubmittedDocument> stored = query.findById(created.id());
            assertThat(stored).isPresent();
            assertThat(stored.get().delivered()).isTrue();
            assertThat(stored.get().name()).isEqualTo("Doc");
            assertThat(stored.get().procedureId()).isEqualTo(3);
            assertThat(stored.get().dateDue()).isEqualTo(LocalDate.of(2026, 3, 15));
        }

        @Test
        @DisplayName("missing document is refused")
        void missing() {
            assertThatThrownBy(() -> manage.update(
                            404, new SaveSubmittedDocumentCommand(null, null, true, null, null, null, false)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("removes an existing document")
        void removes() {
            DocumentType type = seedType(false, null, "");
            procedureIds.add(2);
            SubmittedDocument created =
                    manage.create(createCmd(type.id(), 2, LocalDate.of(2026, 1, 1), null));

            assertThat(manage.delete(created.id())).isTrue();
            assertThat(query.findById(created.id())).isEmpty();
        }

        @Test
        @DisplayName("missing id returns false")
        void missing() {
            assertThat(manage.delete(404)).isFalse();
        }
    }
}
