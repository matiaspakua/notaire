package com.licensis.notaire.unit.folio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase.SaveFolioTypeCommand;
import com.licensis.notaire.application.usecase.folio.FolioTypeManagementService;
import com.licensis.notaire.application.usecase.folio.FolioTypeQueryService;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeInUseException;
import com.licensis.notaire.domain.folio.FolioTypeInvalidException;
import com.licensis.notaire.domain.folio.FolioTypeNotFoundException;
import com.licensis.notaire.domain.folio.FolioTypeStaleVersionException;
import com.licensis.notaire.testing.InMemoryFolioTypeCatalog;

/** The rules of the folio type catalog run on plain objects: no Spring, no database, no mocks (issue #1703). */
@DisplayName("Folio type use cases (issue #1703)")
class FolioTypeUseCasesTest {

    private InMemoryFolioTypeCatalog catalog;
    private FolioTypeManagementService manage;
    private FolioTypeQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryFolioTypeCatalog();
        manage = new FolioTypeManagementService(catalog, catalog);
        query = new FolioTypeQueryService(catalog, catalog);
    }

    private FolioType seed(String name) {
        return catalog.save(FolioType.create(name, null, true, false));
    }

    private SaveFolioTypeCommand command(String name, int version) {
        return new SaveFolioTypeCommand(name, "n", true, false, version);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("stores the type and returns it with an id")
        void stores() {
            FolioType created = manage.create(command("Protocol", 0));

            assertThat(created.id()).isNotNull();
            assertThat(query.getById(created.id()).name()).isEqualTo("Protocol");
        }

        @Test
        @DisplayName("without a name stores nothing")
        void refusesMissingName() {
            assertThatThrownBy(() -> manage.create(command(null, 0))).isInstanceOf(FolioTypeInvalidException.class);
            assertThat(query.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("replaces the fields of an unused type with the stored version")
        void replaces() {
            FolioType type = seed("Old");

            manage.update(type.id(), command("New", type.version()));

            assertThat(query.getById(type.id()).name()).isEqualTo("New");
        }

        @Test
        @DisplayName("a version that is not the stored one is refused and nothing changes")
        void refusesStaleVersion() {
            FolioType type = seed("Keep");

            assertThatThrownBy(() -> manage.update(type.id(), command("Changed", type.version() + 5)))
                    .isInstanceOf(FolioTypeStaleVersionException.class);
            assertThat(query.getById(type.id()).name()).isEqualTo("Keep");
        }

        @Test
        @DisplayName("a type used by folios is locked, and says so")
        void lockedWhenInUse() {
            FolioType type = seed("Locked");
            catalog.withUsedByFolios(type.id());

            assertThatThrownBy(() -> manage.update(type.id(), command("Other", type.version())))
                    .isInstanceOf(FolioTypeInUseException.class)
                    .hasMessage("This folio type is in use and cannot be modified. Create a new one.");
        }

        @Test
        @DisplayName("an unknown id is not found, before the lock is checked")
        void unknown() {
            assertThatThrownBy(() -> manage.update(99, command("X", 0)))
                    .isInstanceOf(FolioTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("removes an unused type")
        void removes() {
            FolioType type = seed("Gone");

            manage.delete(type.id());

            assertThat(query.findAll()).isEmpty();
        }

        @Test
        @DisplayName("keeps a type used by folios and says why")
        void guard() {
            FolioType type = seed("Guarded");
            catalog.withUsedByFolios(type.id());

            assertThatThrownBy(() -> manage.delete(type.id()))
                    .isInstanceOf(FolioTypeInUseException.class)
                    .hasMessage("Cannot delete: the folio type is in use by folios.");
            assertThat(query.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("an unknown id is not found")
        void unknown() {
            assertThatThrownBy(() -> manage.delete(99)).isInstanceOf(FolioTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("query")
    class Query {
        @Test
        @DisplayName("searches by a part of the name")
        void searches() {
            seed("Notarial protocol");
            seed("Auxiliary");

            assertThat(query.searchByName("protocol")).extracting(FolioType::name).containsExactly("Notarial protocol");
        }

        @Test
        @DisplayName("in-use reflects the folios and an unknown id is not found")
        void inUse() {
            FolioType used = seed("Used");
            FolioType free = seed("Free");
            catalog.withUsedByFolios(used.id());

            assertThat(query.isInUse(used.id())).isTrue();
            assertThat(query.isInUse(free.id())).isFalse();
            assertThatThrownBy(() -> query.isInUse(99)).isInstanceOf(FolioTypeNotFoundException.class);
        }
    }
}
