package com.licensis.notaire.unit.document;

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
import com.licensis.notaire.adapter.out.persistence.document.CachingDocumentTypeCatalog;
import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase;
import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase.SaveDocumentTypeCommand;
import com.licensis.notaire.application.port.in.document.QueryDocumentTypesUseCase;
import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.document.DocumentTypeUsagePort;
import com.licensis.notaire.application.usecase.document.DocumentTypeManagementService;
import com.licensis.notaire.application.usecase.document.DocumentTypeQueryService;
import com.licensis.notaire.domain.document.DocumentType;

/**
 * The document-type list is cached in the outbound catalog adapter and a write drops it (#597, CU74).
 */
class DocumentTypeCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryDocumentTypesUseCase.class);
            var manage = context.getBean(ManageDocumentTypesUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(new SaveDocumentTypeCommand("Deed", false, null, "client", true, false, 0));
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
            var manager = new CaffeineCacheManager(CachingDocumentTypeCatalog.CACHE);
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog counting() {
            return new CountingCatalog();
        }

        @Bean
        @Primary
        DocumentTypeCatalogPort catalog(CountingCatalog counting) {
            return new CachingDocumentTypeCatalog(counting);
        }

        @Bean
        QueryDocumentTypesUseCase query(DocumentTypeCatalogPort catalog) {
            DocumentTypeUsagePort unused = id -> false;
            return new DocumentTypeQueryService(catalog, unused);
        }

        @Bean
        ManageDocumentTypesUseCase manage(DocumentTypeCatalogPort catalog) {
            DocumentTypeUsagePort unused = id -> false;
            return new DocumentTypeManagementService(catalog, unused);
        }
    }

    static final class CountingCatalog implements DocumentTypeCatalogPort {
        int reads;
        private final List<DocumentType> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(DocumentType.restore(nextId++, "ID card", false, null, "client", true, false, 0));
        }

        @Override
        public List<DocumentType> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public List<DocumentType> findByNameContaining(String text) {
            return rows.stream().filter(row -> row.name().contains(text)).toList();
        }

        @Override
        public Optional<DocumentType> findById(int id) {
            return rows.stream().filter(row -> row.id() == id).findFirst();
        }

        @Override
        public DocumentType save(DocumentType type) {
            if (type.id() == null) {
                var saved = DocumentType.restore(
                        nextId++, type.name(), type.expires(), type.dueDays(), type.deliveredBy(), type.enabled(),
                        type.returned(), 0);
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
