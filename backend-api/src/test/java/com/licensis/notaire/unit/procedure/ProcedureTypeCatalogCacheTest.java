package com.licensis.notaire.unit.procedure;

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
import com.licensis.notaire.adapter.out.persistence.procedure.CachingProcedureTypeCatalog;
import com.licensis.notaire.application.port.in.procedure.ManageProcedureTypesUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProcedureTypesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort;
import com.licensis.notaire.application.usecase.procedure.ProcedureTypeQueryUseCaseService;
import com.licensis.notaire.application.usecase.procedure.ProcedureTypeWriteService;
import com.licensis.notaire.dto.DtoProcedureType;

/**
 * The procedure-type list is cached in the outbound catalog adapter and a write drops it (#597, CU74).
 */
class ProcedureTypeCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryProcedureTypesUseCase.class);
            var manage = context.getBean(ManageProcedureTypesUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(type(null, "Sale"));
            assertEquals(2, query.findAll().size());
            assertEquals(2, catalog.reads, "a write drops the list so the next read sees the new row");

            manage.assignWorkflow(2, 9);
            assertEquals(9, query.findAll().get(1).getWorkflowDefinitionId());
            assertEquals(3, catalog.reads, "assigning a workflow drops the list");

            manage.delete(1);
            assertEquals(1, query.findAll().size());
            assertEquals(4, catalog.reads, "a delete drops the list too");
        }
    }

    @Configuration
    @EnableCaching
    static class CacheHarness {
        @Bean
        CacheManager cacheManager() {
            var manager = new CaffeineCacheManager(CachingProcedureTypeCatalog.CACHE);
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog counting() {
            return new CountingCatalog();
        }

        @Bean
        @Primary
        ProcedureTypeOpsPort catalog(CountingCatalog counting) {
            return new CachingProcedureTypeCatalog(counting);
        }

        @Bean
        QueryProcedureTypesUseCase query(ProcedureTypeOpsPort catalog) {
            return new ProcedureTypeQueryUseCaseService(catalog);
        }

        @Bean
        ManageProcedureTypesUseCase manage(ProcedureTypeOpsPort catalog) {
            return new ProcedureTypeWriteService(catalog);
        }
    }

    static final class CountingCatalog implements ProcedureTypeOpsPort {
        int reads;
        private final List<DtoProcedureType> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(type(nextId++, "Deed"));
        }

        @Override
        public List<DtoProcedureType> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public List<DtoProcedureType> searchByName(String name) {
            return rows.stream().filter(row -> row.getName().contains(name)).toList();
        }

        @Override
        public Optional<DtoProcedureType> findById(Integer id) {
            return rows.stream().filter(row -> id.equals(row.getIdProcedureType())).findFirst();
        }

        @Override
        public Optional<Boolean> isInUse(Integer id) {
            return findById(id).isPresent() ? Optional.of(false) : Optional.empty();
        }

        @Override
        public SaveResult create(DtoProcedureType dto) {
            dto.setIdProcedureType(nextId++);
            rows.add(dto);
            return new SaveResult.Created(dto);
        }

        @Override
        public SaveResult update(Integer id, DtoProcedureType dto) {
            if (findById(id).isEmpty()) {
                return new SaveResult.NotFound();
            }
            dto.setIdProcedureType(id);
            rows.replaceAll(row -> id.equals(row.getIdProcedureType()) ? dto : row);
            return new SaveResult.Updated();
        }

        @Override
        public DeleteResult delete(Integer id) {
            return rows.removeIf(row -> id.equals(row.getIdProcedureType()))
                    ? new DeleteResult.Deleted()
                    : new DeleteResult.NotFound();
        }

        @Override
        public WorkflowAssignResult assignWorkflow(Integer id, Integer workflowDefinitionId) {
            var found = findById(id);
            if (found.isEmpty()) {
                return new WorkflowAssignResult.NotFound();
            }
            found.get().setWorkflowDefinitionId(workflowDefinitionId);
            return new WorkflowAssignResult.Ok(found.get());
        }
    }

    private static DtoProcedureType type(Integer id, String name) {
        var dto = new DtoProcedureType(name, false, false, false);
        dto.setIdProcedureType(id);
        return dto;
    }
}
