package com.licensis.notaire.unit.concept;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
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
import com.licensis.notaire.adapter.out.persistence.concept.CachingConceptCatalog;
import com.licensis.notaire.application.port.in.concept.ManageConceptsUseCase;
import com.licensis.notaire.application.port.in.concept.QueryConceptsUseCase;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort;
import com.licensis.notaire.application.usecase.concept.ConceptQueryUseCaseService;
import com.licensis.notaire.application.usecase.concept.ConceptWriteService;
import com.licensis.notaire.dto.DtoConcept;

/**
 * The concept list is cached in the outbound catalog adapter and a write drops it (#597, CU74).
 */
class ConceptCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryConceptsUseCase.class);
            var manage = context.getBean(ManageConceptsUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(new DtoConcept("Stamp", BigDecimal.ONE, 0));
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
            var manager = new CaffeineCacheManager(CachingConceptCatalog.CACHE);
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog counting() {
            return new CountingCatalog();
        }

        @Bean
        @Primary
        ConceptOpsPort catalog(CountingCatalog counting) {
            return new CachingConceptCatalog(counting);
        }

        @Bean
        QueryConceptsUseCase query(ConceptOpsPort catalog) {
            return new ConceptQueryUseCaseService(catalog);
        }

        @Bean
        ManageConceptsUseCase manage(ConceptOpsPort catalog) {
            return new ConceptWriteService(catalog);
        }
    }

    static final class CountingCatalog implements ConceptOpsPort {
        int reads;
        private final List<DtoConcept> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(concept(nextId++, "Fee"));
        }

        @Override
        public List<DtoConcept> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public List<DtoConcept> searchByName(String name) {
            return rows.stream().filter(row -> row.getName().contains(name)).toList();
        }

        @Override
        public Optional<DtoConcept> findById(Integer id) {
            return rows.stream().filter(row -> id.equals(row.getIdConcept())).findFirst();
        }

        @Override
        public InUseResult isInUse(Integer id) {
            return findById(id).isPresent() ? new InUseResult.Ok(false) : new InUseResult.NotFound();
        }

        @Override
        public SaveResult create(DtoConcept dto) {
            dto.setIdConcept(nextId++);
            rows.add(dto);
            return new SaveResult.Created(dto);
        }

        @Override
        public SaveResult update(Integer id, DtoConcept dto) {
            if (findById(id).isEmpty()) {
                return new SaveResult.NotFound();
            }
            dto.setIdConcept(id);
            rows.replaceAll(row -> id.equals(row.getIdConcept()) ? dto : row);
            return new SaveResult.Updated();
        }

        @Override
        public DeleteResult delete(Integer id) {
            return rows.removeIf(row -> id.equals(row.getIdConcept()))
                    ? new DeleteResult.Deleted()
                    : new DeleteResult.NotFound();
        }

        private static DtoConcept concept(int id, String name) {
            var dto = new DtoConcept(name, BigDecimal.TEN, 0);
            dto.setIdConcept(id);
            return dto;
        }
    }
}
