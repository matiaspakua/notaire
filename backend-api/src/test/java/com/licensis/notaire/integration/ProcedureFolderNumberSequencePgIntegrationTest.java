package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * CU85 - Administrar Carpetas de Trámite, against the real Flyway-managed Postgres
 * schema. Folder numbers used to be computed as {@code max(number) + 1}, so two
 * concurrent case creations read the same max and the second insert violated
 * {@code uq_carpeta_tramite_numero} (Issue #1038). They now come from a sequence.
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Numeración de carpetas de trámite contra el esquema real (CU85)")
class ProcedureFolderNumberSequencePgIntegrationTest extends BaseIntegrationTest {

    private static final int CONCURRENT_REQUESTS = 20;

    @Autowired
    private ProcedureFolderRepository procedureFolderRepository;

    @Test
    @DisplayName("Should hand out distinct folder numbers to concurrent callers")
    void shouldHandOutDistinctFolderNumbersConcurrently() throws Exception {
        Callable<Integer> draw = procedureFolderRepository::nextFolderNumber;
        List<Integer> numbers;
        try (ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)) {
            List<Future<Integer>> futures = pool.invokeAll(
                    IntStream.range(0, CONCURRENT_REQUESTS).mapToObj(i -> draw).toList());
            numbers = futures.stream().map(ProcedureFolderNumberSequencePgIntegrationTest::get).toList();
        }

        assertThat(numbers).doesNotHaveDuplicates().hasSize(CONCURRENT_REQUESTS);
    }

    @Test
    @DisplayName("Should continue after the highest existing folder number")
    void shouldContinueAfterHighestExistingNumber() {
        int highest = procedureFolderRepository.findAll().stream()
                .mapToInt(ProcedureFolder::getNumber)
                .max()
                .orElse(0);

        assertThat(procedureFolderRepository.nextFolderNumber()).isGreaterThan(highest);
    }

    private static Integer get(Future<Integer> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }
}
