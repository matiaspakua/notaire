package com.licensis.notaire.unit.management;

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
import com.licensis.notaire.adapter.out.persistence.management.CachingManagementStatusCatalog;
import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase.SaveManagementStatusCommand;
import com.licensis.notaire.application.port.in.management.QueryManagementStatusesUseCase;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusUsagePort;
import com.licensis.notaire.application.usecase.management.ManagementStatusManagementService;
import com.licensis.notaire.application.usecase.management.ManagementStatusQueryService;
import com.licensis.notaire.domain.management.ManagementStatus;

/**
 * The management-status list is cached in the outbound catalog adapter and a write drops it (#597, CU74).
 */
class ManagementStatusCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryManagementStatusesUseCase.class);
            var manage = context.getBean(ManageManagementStatusesUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(new SaveManagementStatusCommand("Archived", "closed", 0));
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
            var manager = new CaffeineCacheManager(CachingManagementStatusCatalog.CACHE);
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog counting() {
            return new CountingCatalog();
        }

        @Bean
        @Primary
        ManagementStatusCatalogPort catalog(CountingCatalog counting) {
            return new CachingManagementStatusCatalog(counting);
        }

        @Bean
        QueryManagementStatusesUseCase query(ManagementStatusCatalogPort catalog) {
            ManagementStatusUsagePort unused = id -> false;
            return new ManagementStatusQueryService(catalog, unused);
        }

        @Bean
        ManageManagementStatusesUseCase manage(ManagementStatusCatalogPort catalog) {
            ManagementStatusUsagePort unused = id -> false;
            return new ManagementStatusManagementService(catalog, unused);
        }
    }

    static final class CountingCatalog implements ManagementStatusCatalogPort {
        int reads;
        private final List<ManagementStatus> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(ManagementStatus.restore(nextId++, "Started", null, 0));
        }

        @Override
        public List<ManagementStatus> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public List<ManagementStatus> findByNameContaining(String text) {
            return rows.stream().filter(row -> row.name().contains(text)).toList();
        }

        @Override
        public Optional<ManagementStatus> findById(int id) {
            return rows.stream().filter(row -> row.id() == id).findFirst();
        }

        @Override
        public ManagementStatus save(ManagementStatus status) {
            if (status.id() == null) {
                var saved = ManagementStatus.restore(nextId++, status.name(), status.notes(), 0);
                rows.add(saved);
                return saved;
            }
            rows.replaceAll(row -> row.id().equals(status.id()) ? status : row);
            return status;
        }

        @Override
        public boolean deleteById(int id) {
            return rows.removeIf(row -> row.id() == id);
        }
    }
}
