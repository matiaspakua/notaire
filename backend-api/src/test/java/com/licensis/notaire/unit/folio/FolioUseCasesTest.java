package com.licensis.notaire.unit.folio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.folio.ManageFoliosUseCase.SaveFolioCommand;
import com.licensis.notaire.application.port.out.deed.DeedLookupPort;
import com.licensis.notaire.application.port.out.folio.FolioCatalogPort;
import com.licensis.notaire.application.port.out.person.PersonLookupPort;
import com.licensis.notaire.application.usecase.folio.FolioManagementService;
import com.licensis.notaire.application.usecase.folio.FolioQueryService;
import com.licensis.notaire.domain.folio.Folio;
import com.licensis.notaire.domain.folio.FolioInUseException;
import com.licensis.notaire.domain.folio.FolioInvalidException;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.testing.InMemoryFolioTypeCatalog;

@DisplayName("Folio instance use cases (issue #1727)")
class FolioUseCasesTest {

    private InMemoryFolioCatalog catalog;
    private InMemoryFolioTypeCatalog types;
    private Set<Integer> people;
    private Set<Integer> deeds;
    private FolioManagementService manage;
    private FolioQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryFolioCatalog();
        types = new InMemoryFolioTypeCatalog();
        people = new HashSet<>(Set.of(1));
        deeds = new HashSet<>();
        PersonLookupPort personLookup = people::contains;
        DeedLookupPort deedLookup = deeds::contains;
        manage = new FolioManagementService(catalog, types, personLookup, deedLookup);
        query = new FolioQueryService(catalog);
        types.save(FolioType.create("Protocol", "", true, false));
    }

    private SaveFolioCommand cmd(int number, String status, Integer deedId) {
        return new SaveFolioCommand(number, 2026, status, null, 1, 1, deedId);
    }

    @Test
    @DisplayName("create forces Utilizado when a deed is linked")
    void createWithDeed() {
        deeds.add(10);
        Folio created = manage.create(cmd(1, "New", 10));
        assertThat(created.status()).isEqualTo(Folio.STATUS_USED);
        assertThat(created.deedId()).isEqualTo(10);
    }

    @Test
    @DisplayName("Utilizado folio cannot be deleted")
    void cannotDeleteUsed() {
        Folio created = manage.create(cmd(2, Folio.STATUS_USED, null));
        assertThatThrownBy(() -> manage.delete(created.id())).isInstanceOf(FolioInUseException.class);
    }

    @Test
    @DisplayName("missing type or notary is refused")
    void missingRefs() {
        assertThatThrownBy(() -> manage.create(new SaveFolioCommand(1, 2026, "New", null, null, 1, null)))
                .isInstanceOf(FolioInvalidException.class);
    }

    @Test
    @DisplayName("in-use query follows Utilizado status")
    void inUse() {
        Folio created = manage.create(cmd(3, "New", null));
        assertThat(query.isInUse(created.id())).contains(false);
        Folio used = manage.create(cmd(4, Folio.STATUS_USED, null));
        assertThat(query.isInUse(used.id())).contains(true);
    }

    @Test
    @DisplayName("same-deed re-save is allowed")
    void sameDeedResave() {
        deeds.add(5);
        Folio created = manage.create(cmd(5, "New", 5));
        Folio updated = manage.update(created.id(), cmd(5, Folio.STATUS_USED, 5));
        assertThat(updated.status()).isEqualTo(Folio.STATUS_USED);
    }

    @Test
    @DisplayName("different deed while Utilizado is refused")
    void otherDeedRefused() {
        deeds.add(6);
        deeds.add(7);
        Folio created = manage.create(cmd(6, "New", 6));
        assertThatThrownBy(() -> manage.update(created.id(), cmd(6, "New", 7)))
                .isInstanceOf(FolioInUseException.class);
    }

    static final class InMemoryFolioCatalog implements FolioCatalogPort {
        private final AtomicInteger seq = new AtomicInteger(1);
        private final Map<Integer, Folio> rows = new HashMap<>();

        @Override
        public List<Folio> findAll() {
            return List.copyOf(rows.values());
        }

        @Override
        public List<Folio> findByStatus(String status) {
            return rows.values().stream().filter(f -> status.equals(f.status())).toList();
        }

        @Override
        public Optional<Folio> findById(int id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public List<Folio> findAvailableAuxiliaryFolios() {
            return rows.values().stream().filter(f -> f.folioTypeAuxiliary() && f.deedId() == null).toList();
        }

        @Override
        public Folio save(Folio folio) {
            if (folio.id() == null) {
                Folio stored = folio.withIdentity(seq.getAndIncrement(), 0);
                rows.put(stored.id(), stored);
                return stored;
            }
            rows.put(folio.id(), folio);
            return folio;
        }

        @Override
        public boolean deleteById(int id) {
            return rows.remove(id) != null;
        }
    }
}
