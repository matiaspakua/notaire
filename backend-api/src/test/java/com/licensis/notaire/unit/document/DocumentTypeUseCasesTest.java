package com.licensis.notaire.unit.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase.SaveDocumentTypeCommand;
import com.licensis.notaire.application.usecase.document.DocumentTypeManagementService;
import com.licensis.notaire.application.usecase.document.DocumentTypeQueryService;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeInUseException;
import com.licensis.notaire.domain.document.DocumentTypeInvalidException;
import com.licensis.notaire.domain.document.DocumentTypeNotFoundException;
import com.licensis.notaire.domain.document.DocumentTypeStaleVersionException;
import com.licensis.notaire.testing.InMemoryDocumentTypeCatalog;

/** The rules of the document type catalog run on plain objects: no Spring, no database, no mocks (issue #1705). */
@DisplayName("Document type use cases (issue #1705)")
class DocumentTypeUseCasesTest {

    private InMemoryDocumentTypeCatalog catalog;
    private DocumentTypeManagementService manage;
    private DocumentTypeQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryDocumentTypeCatalog();
        manage = new DocumentTypeManagementService(catalog, catalog);
        query = new DocumentTypeQueryService(catalog, catalog);
    }

    private DocumentType seed(String name) {
        return catalog.save(DocumentType.create(name, false, null, "", true, false));
    }

    private SaveDocumentTypeCommand command(String name, int version) {
        return new SaveDocumentTypeCommand(name, false, null, "", true, false, version);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("stores the type and returns it with an id")
        void stores() {
            DocumentType created = manage.create(command("ID card", 0));

            assertThat(created.id()).isNotNull();
            assertThat(query.getById(created.id()).name()).isEqualTo("ID card");
        }

        @Test
        @DisplayName("without a name stores nothing")
        void refusesMissingName() {
            assertThatThrownBy(() -> manage.create(command(null, 0))).isInstanceOf(DocumentTypeInvalidException.class);
            assertThat(query.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("replaces the fields of an unused type with the stored version")
        void replaces() {
            DocumentType type = seed("Old");

            manage.update(type.id(), command("New", type.version()));

            assertThat(query.getById(type.id()).name()).isEqualTo("New");
        }

        @Test
        @DisplayName("a version that is not the stored one is refused and nothing changes")
        void refusesStaleVersion() {
            DocumentType type = seed("Keep");

            assertThatThrownBy(() -> manage.update(type.id(), command("Changed", type.version() + 5)))
                    .isInstanceOf(DocumentTypeStaleVersionException.class);
            assertThat(query.getById(type.id()).name()).isEqualTo("Keep");
        }

        @Test
        @DisplayName("a type in use is locked, and says so")
        void lockedWhenInUse() {
            DocumentType type = seed("Locked");
            catalog.withInUse(type.id());

            assertThatThrownBy(() -> manage.update(type.id(), command("Other", type.version())))
                    .isInstanceOf(DocumentTypeInUseException.class)
                    .hasMessage("This document type is in use and cannot be modified. Create a new one.");
        }

        @Test
        @DisplayName("an unknown id is not found, before the lock is checked")
        void unknown() {
            assertThatThrownBy(() -> manage.update(99, command("X", 0)))
                    .isInstanceOf(DocumentTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("removes an unused type")
        void removes() {
            DocumentType type = seed("Gone");

            manage.delete(type.id());

            assertThat(query.findAll()).isEmpty();
        }

        @Test
        @DisplayName("keeps a type in use and says why")
        void guard() {
            DocumentType type = seed("Guarded");
            catalog.withInUse(type.id());

            assertThatThrownBy(() -> manage.delete(type.id()))
                    .isInstanceOf(DocumentTypeInUseException.class)
                    .hasMessage(
                            "Cannot delete: the document type is used by procedure templates or submitted documents.");
            assertThat(query.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("an unknown id is not found")
        void unknown() {
            assertThatThrownBy(() -> manage.delete(99)).isInstanceOf(DocumentTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("query")
    class Query {
        @Test
        @DisplayName("searches by a part of the name")
        void searches() {
            seed("National ID card");
            seed("Passport");

            assertThat(query.searchByName("ID")).extracting(DocumentType::name).containsExactly("National ID card");
        }

        @Test
        @DisplayName("in-use requires the type to exist")
        void inUseRequiresExist() {
            assertThatThrownBy(() -> query.isInUse(99)).isInstanceOf(DocumentTypeNotFoundException.class);
        }
    }
}
