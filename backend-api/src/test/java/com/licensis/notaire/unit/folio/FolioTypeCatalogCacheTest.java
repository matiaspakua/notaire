package com.licensis.notaire.unit.folio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.licensis.notaire.adapter.out.persistence.folio.CachingFolioTypeCatalog;
import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase;
import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase.SaveFolioTypeCommand;
import com.licensis.notaire.application.port.in.folio.QueryFolioTypesUseCase;
import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeUsagePort;
import com.licensis.notaire.application.usecase.folio.FolioTypeManagementService;
import com.licensis.notaire.application.usecase.folio.FolioTypeQueryService;
import com.licensis.notaire.domain.folio.FolioType;

/**
 * The folio-type list is cached in the outbound catalog adapter and a write drops it (#597, CU74).
 */
class FolioTypeCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryFolioTypesUseCase.class);
            var manage = context.getBean(ManageFolioTypesUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(new SaveFolioTypeCommand("Auxiliary", "notes", true, true, 0));
            assertEquals(2, query.findAll().size());
            assertEquals(2, catalog.reads, "a write drops the list so the next read sees the new row");

            manage.delete(1);
            assertEquals(1, query.findAll().size());
            assertEquals(3, catalog.reads, "a delete drops the list too");
        }
    }

    @Configuration
    @EnableCaching
    static class CacheHarness {
        @Bean
        CacheManager cacheManager() {
            var manager = new CaffeineCacheManager(CachingFolioTypeCatalog.CACHE);
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog counting() {
            return new CountingCatalog();
        }

        @Bean
        @Primary
        FolioTypeCatalogPort catalog(CountingCatalog counting) {
            return new CachingFolioTypeCatalog(counting);
        }

        @Bean
        QueryFolioTypesUseCase query(FolioTypeCatalogPort catalog) {
            FolioTypeUsagePort unused = id -> false;
            return new FolioTypeQueryService(catalog, unused);
        }

        @Bean
        ManageFolioTypesUseCase manage(FolioTypeCatalogPort catalog) {
            FolioTypeUsagePort unused = id -> false;
            return new FolioTypeManagementService(catalog, unused);
        }
    }

    static final class CountingCatalog implements FolioTypeCatalogPort {
        int reads;
        private final List<FolioType> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(FolioType.restore(nextId++, "Protocol", null, true, false, 0));
        }

        @Override
        public List<FolioType> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public List<FolioType> findByNameContaining(String text) {
            return rows.stream().filter(row -> row.name().contains(text)).toList();
        }

        @Override
        public Optional<FolioType> findById(int id) {
            return rows.stream().filter(row -> row.id() == id).findFirst();
        }

        @Override
        public FolioType save(FolioType type) {
            if (type.id() == null) {
                var saved = FolioType.restore(
                        nextId++, type.name(), type.notes(), type.enabled(), type.auxiliary(), 0);
                rows.add(saved);
                return saved;
            }
            rows.replaceAll(row -> row.id().equals(type.id()) ? type : row);
            return type;
        }

        @Override
        public boolean deleteById(int id) {
            return rows.removeIf(row -> row.id() == id);
        }
    }
}
