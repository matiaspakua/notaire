package com.licensis.notaire.unit.identification;

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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.licensis.notaire.application.port.in.identification.ManageIdentificationTypesUseCase;
import com.licensis.notaire.application.port.in.identification.ManageIdentificationTypesUseCase.SaveIdentificationTypeCommand;
import com.licensis.notaire.application.port.in.identification.QueryIdentificationTypesUseCase;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeUsagePort;
import com.licensis.notaire.application.usecase.identification.IdentificationTypeManagementService;
import com.licensis.notaire.application.usecase.identification.IdentificationTypeQueryService;
import com.licensis.notaire.domain.identification.IdentificationType;

/**
 * Catalog list reads are cached and a write drops the list (#597, CU74).
 */
class IdentificationTypeCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryIdentificationTypesUseCase.class);
            var manage = context.getBean(ManageIdentificationTypesUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(new SaveIdentificationTypeCommand("Passport", "9"));
            assertEquals(2, query.findAll().size());
            // The write's own name check reads the port, then the evicted list is loaded again.
            assertEquals(3, catalog.reads, "a write drops the list so the next read sees the new row");
        }
    }

    @Configuration
    @EnableCaching
    static class CacheHarness {
        @Bean
        CacheManager cacheManager() {
            var manager = new CaffeineCacheManager("identification-types");
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog catalog() {
            return new CountingCatalog();
        }

        @Bean
        IdentificationTypeQueryService query(CountingCatalog catalog) {
            return new IdentificationTypeQueryService(catalog);
        }

        @Bean
        IdentificationTypeManagementService manage(CountingCatalog catalog) {
            IdentificationTypeUsagePort unused = id -> false;
            return new IdentificationTypeManagementService(catalog, unused);
        }
    }

    static final class CountingCatalog implements IdentificationTypeCatalogPort {
        int reads;
        private final List<IdentificationType> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(IdentificationType.restore(nextId++, "DNI", "8", 0));
        }

        @Override
        public List<IdentificationType> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public Optional<IdentificationType> findById(int id) {
            return rows.stream().filter(row -> row.id() == id).findFirst();
        }

        @Override
        public boolean existsById(int id) {
            return findById(id).isPresent();
        }

        @Override
        public IdentificationType save(IdentificationType type) {
            if (type.id() == null) {
                var saved = IdentificationType.restore(nextId++, type.name(), type.characters(), 0);
                rows.add(saved);
                return saved;
            }
            rows.replaceAll(row -> row.id().equals(type.id()) ? type : row);
            return type;
        }

        @Override
        public void deleteById(int id) {
            rows.removeIf(row -> row.id() == id);
        }
    }
}
