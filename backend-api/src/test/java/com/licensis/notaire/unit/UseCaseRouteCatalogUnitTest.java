package com.licensis.notaire.unit;

import com.licensis.notaire.testing.UseCaseRouteCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UseCaseRouteCatalogUnitTest {

    @Test
    void shouldContainAllUseCasesWithoutDuplicates() {
        List<UseCaseRouteCatalog.UseCaseRoute> routes = UseCaseRouteCatalog.all();
        assertEquals(68, routes.size(), "The catalog must contain all 68 use cases.");

        Set<String> ids = routes.stream()
                .map(UseCaseRouteCatalog.UseCaseRoute::useCaseId)
                .collect(Collectors.toSet());
        assertEquals(68, ids.size(), "Use case IDs must be unique.");
    }

    @Test
    void shouldDefineRouteDataForEveryUseCase() {
        for (UseCaseRouteCatalog.UseCaseRoute route : UseCaseRouteCatalog.all()) {
            assertNotNull(route.useCaseId());
            assertNotNull(route.useCaseName());
            assertNotNull(route.method());
            assertNotNull(route.pathPattern());
            assertFalse(route.pathPattern().isBlank(), "Route path pattern must not be blank.");
        }
    }
}
