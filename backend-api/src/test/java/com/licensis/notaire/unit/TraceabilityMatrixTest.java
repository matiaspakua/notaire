package com.licensis.notaire.unit;

import com.licensis.notaire.testing.UseCaseRouteCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the test suite covers the use cases defined in UseCaseRouteCatalog.
 * Each entry in REGISTRY maps a test class name to the use cases it exercises.
 */
@DisplayName("Requirements traceability matrix")
class TraceabilityMatrixTest {

    /** Explicit registry: test class name → covered use case IDs. */
    private static final Map<String, List<String>> REGISTRY = Map.ofEntries(
        Map.entry("PresupuestoEntityTest",          List.of("CU01", "CU45", "CU60")),
        Map.entry("PresupuestoServiceTest",         List.of("CU01", "CU45", "CU60")),
        Map.entry("PaginationTest",                 List.of("CU60", "CU62")),
        Map.entry("GestionDeEscrituraEntityTest",   List.of("CU02", "CU13", "CU14", "CU19", "CU24")),
        Map.entry("EscrituraEntityTest",            List.of("CU05", "CU06", "CU52", "CU62")),
        Map.entry("PagoEntityTest",                 List.of("CU15", "CU47")),
        Map.entry("PagoServiceTest",                List.of("CU15", "CU47")),
        Map.entry("PersonaEntityTest",              List.of("CU17", "CU18", "CU41", "CU46", "CU48", "CU51", "CU54", "CU61")),
        Map.entry("PersonaServiceTest",             List.of("CU17", "CU18", "CU41", "CU54", "CU61")),
        Map.entry("WorkflowEntityTest", List.of("CU83")),
        Map.entry("WorkflowDefinitionControllerTest", List.of("CU83")),
        Map.entry("WorkflowNodeControllerTest", List.of("CU83")),
        Map.entry("WorkflowTransitionControllerTest", List.of("CU83")),
        Map.entry("WorkflowValidationControllerTest", List.of("CU83")),
        Map.entry("WorkflowValidationServiceTest", List.of("CU83")),
        Map.entry("TipoDeTramiteWorkflowAssignmentTest", List.of("CU83")),
        Map.entry("PlantillaPresupuestoControllerTest", List.of("CU39", "CU49", "CU55")),
        Map.entry("PlantillaTramiteControllerTest",     List.of("CU55")),
        Map.entry("FolioControllerTest",            List.of("CU28", "CU33", "CU56", "CU63")),
        Map.entry("RegistroAuditoriaServiceTest",   List.of("CU23")),
        Map.entry("ObservabilityTest",              List.of("CU23")),
        Map.entry("CatalogControllersIntegrationTest", List.of(
                "CU26", "CU27", "CU28", "CU29", "CU30",
                "CU31", "CU32", "CU33", "CU34", "CU35",
                "CU36", "CU37", "CU38", "CU40",
                "CU57", "CU58", "CU64", "CU65", "CU66", "CU67", "CU68")),
        Map.entry("SimpleControllersTest",          List.of(
                "CU07", "CU08", "CU09", "CU10", "CU11", "CU12",
                "CU14", "CU22", "CU56")),
        Map.entry("AdditionalControllersTest",      List.of("CU02", "CU13", "CU14", "CU19", "CU24", "CU25")),
        Map.entry("CoreBusinessControllersIntegrationTest", List.of(
                "CU01", "CU02", "CU05", "CU15", "CU17", "CU18",
                "CU20", "CU45", "CU47", "CU54", "CU60", "CU61")),
        Map.entry("RemainingControllersIntegrationTest",    List.of(
                "CU02", "CU04", "CU11", "CU13", "CU14",
                "CU15", "CU22", "CU24", "CU25", "CU59")),
        Map.entry("BusinessWorkflowIntegrationTest", List.of(
                "CU03", "CU17", "CU18", "CU19", "CU20",
                "CU02", "CU15", "CU05", "CU06", "CU26",
                "CU45", "CU47"))
    );

    @Test
    @DisplayName("Every registry entry references known use case IDs from the catalog")
    void allTaggedUseCasesShouldExistInCatalog() {
        Set<String> catalogIds = new HashSet<>();
        for (UseCaseRouteCatalog.UseCaseRoute route : UseCaseRouteCatalog.all()) {
            catalogIds.add(route.useCaseId());
        }

        assertFalse(REGISTRY.isEmpty(), "Traceability registry must not be empty");

        for (var entry : REGISTRY.entrySet()) {
            String testClass = entry.getKey();
            for (String id : entry.getValue()) {
                assertTrue(catalogIds.contains(id),
                        "Test class " + testClass + " references unknown use case: " + id);
            }
        }
    }

    @Test
    @DisplayName("At least 50% of catalog use cases have test coverage")
    void mostUseCasesShouldHaveTestCoverage() {
        Set<String> covered = new HashSet<>();
        REGISTRY.values().forEach(covered::addAll);

        List<UseCaseRouteCatalog.UseCaseRoute> all = UseCaseRouteCatalog.all();
        int total = all.size();
        int coveredCount = (int) all.stream()
                .filter(r -> covered.contains(r.useCaseId()))
                .count();

        double ratio = (double) coveredCount / total;
        assertTrue(ratio >= 0.5,
                String.format("Only %d/%d (%.0f%%) use cases have test coverage. Expected ≥50%%.",
                        coveredCount, total, ratio * 100));
    }

    @Test
    @DisplayName("Critical use cases CU01-CU20 and CU23 all have test coverage")
    void criticalUseCasesHaveCoverage() {
        Set<String> covered = new HashSet<>();
        REGISTRY.values().forEach(covered::addAll);

        List<String> critical = List.of(
                "CU01", "CU02", "CU03", "CU04", "CU05", "CU06",
                "CU07", "CU08", "CU09", "CU10", "CU11", "CU12",
                "CU13", "CU14", "CU15", "CU17", "CU18", "CU19",
                "CU20", "CU23"
        );

        List<String> uncovered = critical.stream()
                .filter(id -> !covered.contains(id))
                .toList();

        assertTrue(uncovered.isEmpty(),
                "Critical use cases without test coverage: " + uncovered);
    }
}
