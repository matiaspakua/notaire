package com.licensis.notaire.unit.identification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.identification.ManageIdentificationTypesUseCase.SaveIdentificationTypeCommand;
import com.licensis.notaire.application.usecase.identification.IdentificationTypeManagementService;
import com.licensis.notaire.application.usecase.identification.IdentificationTypeQueryService;
import com.licensis.notaire.domain.identification.IdentificationType;
import com.licensis.notaire.domain.identification.IdentificationTypeInUseException;
import com.licensis.notaire.domain.identification.IdentificationTypeNameTakenException;
import com.licensis.notaire.domain.identification.IdentificationTypeNotFoundException;
import com.licensis.notaire.testing.InMemoryIdentificationTypeCatalog;

/** The rules of the catalog run on plain objects: no Spring context, no database, no mocks (issue #1700). */
@DisplayName("Identification type use cases (issue #1700)")
class IdentificationTypeUseCasesTest {

    private InMemoryIdentificationTypeCatalog catalog;
    private IdentificationTypeManagementService manage;
    private IdentificationTypeQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryIdentificationTypeCatalog();
        manage = new IdentificationTypeManagementService(catalog, catalog);
        query = new IdentificationTypeQueryService(catalog);
    }

    private IdentificationType seed(String name) {
        return catalog.save(IdentificationType.create(name, "8"));
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("saves a trimmed type")
        void saves() {
            IdentificationType created = manage.create(new SaveIdentificationTypeCommand("  DNI ", "8"));

            assertThat(created.id()).isNotNull();
            assertThat(query.getById(created.id()).name()).isEqualTo("DNI");
        }

        @Test
        @DisplayName("refuses a name another type has, ignoring case and spaces, and saves nothing")
        void refusesDuplicate() {
            seed("DNI");

            assertThatThrownBy(() -> manage.create(new SaveIdentificationTypeCommand(" dni ", "8")))
                    .isInstanceOf(IdentificationTypeNameTakenException.class)
                    .hasMessage("An identification type with this name already exists");
            assertThat(query.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("renames and changes the characters")
        void renames() {
            IdentificationType dni = seed("DNI");

            manage.update(dni.id(), new SaveIdentificationTypeCommand("National id", "9"));

            assertThat(query.getById(dni.id()).name()).isEqualTo("National id");
            assertThat(query.getById(dni.id()).characters()).isEqualTo("9");
        }

        @Test
        @DisplayName("keeping its own name is fine")
        void keepsOwnName() {
            IdentificationType dni = seed("DNI");

            manage.update(dni.id(), new SaveIdentificationTypeCommand("dni", "10"));

            assertThat(query.getById(dni.id()).characters()).isEqualTo("10");
        }

        @Test
        @DisplayName("refuses another type's name")
        void refusesAnotherName() {
            IdentificationType dni = seed("DNI");
            seed("CUIT");

            assertThatThrownBy(() -> manage.update(dni.id(), new SaveIdentificationTypeCommand("cuit", "8")))
                    .isInstanceOf(IdentificationTypeNameTakenException.class);
            assertThat(query.getById(dni.id()).name()).isEqualTo("DNI");
        }

        @Test
        @DisplayName("an unknown id is not found, before the name is checked")
        void unknown() {
            seed("DNI");

            assertThatThrownBy(() -> manage.update(99, new SaveIdentificationTypeCommand("DNI", "8")))
                    .isInstanceOf(IdentificationTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("removes an unused type")
        void removes() {
            IdentificationType dni = seed("DNI");

            manage.delete(dni.id());

            assertThat(query.findAll()).isEmpty();
        }

        @Test
        @DisplayName("keeps a type that people use and says why")
        void keepsUsed() {
            IdentificationType dni = seed("DNI");
            catalog.withUsedByPeople(dni.id());

            assertThatThrownBy(() -> manage.delete(dni.id()))
                    .isInstanceOf(IdentificationTypeInUseException.class)
                    .hasMessage("Cannot delete: the identification type is in use by people");
            assertThat(query.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("an unknown id is not found, before the usage is checked")
        void unknown() {
            assertThatThrownBy(() -> manage.delete(99)).isInstanceOf(IdentificationTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("query")
    class Query {
        @Test
        @DisplayName("an unknown id is not found")
        void unknown() {
            assertThatThrownBy(() -> query.getById(99)).isInstanceOf(IdentificationTypeNotFoundException.class);
        }
    }
}
