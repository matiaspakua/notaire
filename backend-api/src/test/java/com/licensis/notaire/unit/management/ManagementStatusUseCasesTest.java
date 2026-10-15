package com.licensis.notaire.unit.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase.SaveManagementStatusCommand;
import com.licensis.notaire.application.usecase.management.ManagementStatusManagementService;
import com.licensis.notaire.application.usecase.management.ManagementStatusQueryService;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusInUseException;
import com.licensis.notaire.domain.management.ManagementStatusInvalidException;
import com.licensis.notaire.domain.management.ManagementStatusNotFoundException;
import com.licensis.notaire.domain.management.ManagementStatusStaleVersionException;
import com.licensis.notaire.testing.InMemoryManagementStatusCatalog;

/** The rules of the management status catalog run on plain objects: no Spring, no database, no mocks (issue #1719). */
@DisplayName("Management status use cases (issue #1719)")
class ManagementStatusUseCasesTest {

    private InMemoryManagementStatusCatalog catalog;
    private ManagementStatusManagementService manage;
    private ManagementStatusQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryManagementStatusCatalog();
        manage = new ManagementStatusManagementService(catalog, catalog);
        query = new ManagementStatusQueryService(catalog, catalog);
    }

    private ManagementStatus seed(String name) {
        return catalog.save(ManagementStatus.create(name, "note"));
    }

    private SaveManagementStatusCommand command(String name, int version) {
        return new SaveManagementStatusCommand(name, "note", version);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("stores the status and returns it with an id")
        void stores() {
            ManagementStatus created = manage.create(command("Started", 0));

            assertThat(created.id()).isNotNull();
            assertThat(query.getById(created.id()).name()).isEqualTo("Started");
        }

        @Test
        @DisplayName("without a name stores nothing")
        void refusesMissingName() {
            assertThatThrownBy(() -> manage.create(command(null, 0)))
                    .isInstanceOf(ManagementStatusInvalidException.class);
            assertThat(query.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("replaces the fields of an unused status with the stored version")
        void replaces() {
            ManagementStatus status = seed("Old");

            manage.update(status.id(), command("New", status.version()));

            assertThat(query.getById(status.id()).name()).isEqualTo("New");
        }

        @Test
        @DisplayName("a version that is not the stored one is refused and nothing changes")
        void refusesStaleVersion() {
            ManagementStatus status = seed("Keep");

            assertThatThrownBy(() -> manage.update(status.id(), command("Changed", status.version() + 5)))
                    .isInstanceOf(ManagementStatusStaleVersionException.class);
            assertThat(query.getById(status.id()).name()).isEqualTo("Keep");
        }

        @Test
        @DisplayName("a status in use is locked, and says so")
        void lockedWhenInUse() {
            ManagementStatus status = seed("Locked");
            catalog.withInUse(status.id());

            assertThatThrownBy(() -> manage.update(status.id(), command("Other", status.version())))
                    .isInstanceOf(ManagementStatusInUseException.class)
                    .hasMessage(ManagementStatusInUseException.LOCKED_MESSAGE);
        }

        @Test
        @DisplayName("an unknown id is not found, before the lock is checked")
        void unknown() {
            assertThatThrownBy(() -> manage.update(99, command("X", 0)))
                    .isInstanceOf(ManagementStatusNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("removes an unused status")
        void removes() {
            ManagementStatus status = seed("Gone");

            manage.delete(status.id());

            assertThat(query.findAll()).isEmpty();
        }

        @Test
        @DisplayName("keeps a status in use and says why")
        void guard() {
            ManagementStatus status = seed("Guarded");
            catalog.withInUse(status.id());

            assertThatThrownBy(() -> manage.delete(status.id()))
                    .isInstanceOf(ManagementStatusInUseException.class)
                    .hasMessage(ManagementStatusInUseException.DELETE_MESSAGE);
            assertThat(query.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("an unknown id is not found")
        void unknown() {
            assertThatThrownBy(() -> manage.delete(99)).isInstanceOf(ManagementStatusNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("query")
    class Query {
        @Test
        @DisplayName("searches by a part of the name")
        void searches() {
            seed("Workflow started");
            seed("Archived");

            assertThat(query.searchByName("started"))
                    .extracting(ManagementStatus::name)
                    .containsExactly("Workflow started");
        }

        @Test
        @DisplayName("in-use requires the status to exist")
        void inUseRequiresExist() {
            assertThatThrownBy(() -> query.isInUse(99)).isInstanceOf(ManagementStatusNotFoundException.class);
        }
    }
}
