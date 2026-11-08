package com.licensis.notaire.unit.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.usecase.catalog.CatalogLockSupport;

/** Shared catalog write-lock control flow: no Spring, no database (issue #1722). */
@DisplayName("CatalogLockSupport (issue #1722)")
class CatalogLockSupportTest {

    private record Row(int id, String name, int version) {
        Row changeTo(String newName) {
            return new Row(id, newName, version);
        }
    }

    private Map<Integer, Row> rows;
    private Set<Integer> inUse;
    private int nextId;

    @BeforeEach
    void setUp() {
        rows = new HashMap<>();
        inUse = new HashSet<>();
        nextId = 1;
    }

    private Row seed(String name) {
        int id = nextId++;
        Row row = new Row(id, name, 0);
        rows.put(id, row);
        return row;
    }

    private RuntimeException notFound(int id) {
        return new IllegalStateException("missing " + id);
    }

    private RuntimeException lockedEdit() {
        return new IllegalStateException("locked-edit");
    }

    private RuntimeException lockedDelete() {
        return new IllegalStateException("locked-delete");
    }

    private RuntimeException stale(int id, int stored, int received) {
        return new IllegalStateException("stale " + id + " " + stored + "/" + received);
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("applies the change when unused and the version matches")
        void happy() {
            Row row = seed("Old");

            CatalogLockSupport.update(
                    row.id(),
                    id -> Optional.ofNullable(rows.get(id)),
                    inUse::contains,
                    Row::version,
                    0,
                            () -> notFound(row.id()),
                            CatalogLockSupportTest.this::lockedEdit,
                            CatalogLockSupportTest.this::stale,
                            existing -> existing.changeTo("New"),
                            saved -> rows.put(saved.id(), saved));

            assertThat(rows.get(row.id()).name()).isEqualTo("New");
        }

        @Test
        @DisplayName("refuses when the row is in use")
        void locked() {
            Row row = seed("Locked");
            inUse.add(row.id());

            assertThatThrownBy(() -> CatalogLockSupport.update(
                            row.id(),
                            id -> Optional.ofNullable(rows.get(id)),
                            inUse::contains,
                            Row::version,
                            0,
                            () -> notFound(row.id()),
                            CatalogLockSupportTest.this::lockedEdit,
                            CatalogLockSupportTest.this::stale,
                            existing -> existing.changeTo("X"),
                            saved -> rows.put(saved.id(), saved)))
                    .hasMessage("locked-edit");
            assertThat(rows.get(row.id()).name()).isEqualTo("Locked");
        }

        @Test
        @DisplayName("refuses a version that is not the stored one")
        void staleVersion() {
            Row row = seed("Keep");

            assertThatThrownBy(() -> CatalogLockSupport.update(
                            row.id(),
                            id -> Optional.ofNullable(rows.get(id)),
                            inUse::contains,
                            Row::version,
                            5,
                            () -> notFound(row.id()),
                            CatalogLockSupportTest.this::lockedEdit,
                            CatalogLockSupportTest.this::stale,
                            existing -> existing.changeTo("X"),
                            saved -> rows.put(saved.id(), saved)))
                    .hasMessage("stale " + row.id() + " 0/5");
            assertThat(rows.get(row.id()).name()).isEqualTo("Keep");
        }

        @Test
        @DisplayName("refuses an unknown id before checking lock")
        void missing() {
            assertThatThrownBy(() -> CatalogLockSupport.update(
                            99,
                            id -> Optional.ofNullable(rows.get(id)),
                            inUse::contains,
                            Row::version,
                            0,
                            () -> notFound(99),
                            CatalogLockSupportTest.this::lockedEdit,
                            CatalogLockSupportTest.this::stale,
                            existing -> existing.changeTo("X"),
                            saved -> rows.put(saved.id(), saved)))
                    .hasMessage("missing 99");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("removes an unused row")
        void happy() {
            Row row = seed("Gone");
            AtomicReference<Boolean> deleted = new AtomicReference<>(false);

            CatalogLockSupport.delete(
                    row.id(),
                    id -> Optional.ofNullable(rows.get(id)),
                    inUse::contains,
                    () -> notFound(row.id()),
                    CatalogLockSupportTest.this::lockedDelete,
                    id -> {
                        deleted.set(rows.remove(id) != null);
                        return deleted.get();
                    });

            assertThat(deleted.get()).isTrue();
            assertThat(rows).isEmpty();
        }

        @Test
        @DisplayName("keeps a row that is in use")
        void locked() {
            Row row = seed("Guarded");
            inUse.add(row.id());

            assertThatThrownBy(() -> CatalogLockSupport.delete(
                            row.id(),
                            id -> Optional.ofNullable(rows.get(id)),
                            inUse::contains,
                            () -> notFound(row.id()),
                            CatalogLockSupportTest.this::lockedDelete,
                            id -> rows.remove(id) != null))
                    .hasMessage("locked-delete");
            assertThat(rows).hasSize(1);
        }

        @Test
        @DisplayName("refuses an unknown id")
        void missing() {
            assertThatThrownBy(() -> CatalogLockSupport.delete(
                            99,
                            id -> Optional.ofNullable(rows.get(id)),
                            inUse::contains,
                            () -> notFound(99),
                            CatalogLockSupportTest.this::lockedDelete,
                            id -> rows.remove(id) != null))
                    .hasMessage("missing 99");
        }

        @Test
        @DisplayName("throws not-found when delete reports the row vanished")
        void vanishedOnDelete() {
            Row row = seed("Race");

            assertThatThrownBy(() -> CatalogLockSupport.delete(
                            row.id(),
                            id -> Optional.ofNullable(rows.get(id)),
                            inUse::contains,
                            () -> notFound(row.id()),
                            CatalogLockSupportTest.this::lockedDelete,
                            id -> false))
                    .hasMessage("missing " + row.id());
        }
    }
}
