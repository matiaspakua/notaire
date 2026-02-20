package com.licensis.notaire.integration;

import com.licensis.notaire.testing.UseCaseRouteCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
class UseCaseRouteCoverageIntegrationTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void shouldExposeRoutesForAllUseCases() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (UseCaseRouteCatalog.UseCaseRoute useCaseRoute : UseCaseRouteCatalog.all()) {
            assertTrue(
                    routeExists(handlerMethods, useCaseRoute.method(), useCaseRoute.pathPattern()),
                    () -> "Missing route for " + useCaseRoute.useCaseId() + " (" + useCaseRoute.useCaseName()
                            + "): " + useCaseRoute.method() + " " + useCaseRoute.pathPattern()
            );
        }
    }

    private boolean routeExists(
            Map<RequestMappingInfo, HandlerMethod> handlerMethods,
            RequestMethod expectedMethod,
            String expectedPathPattern
    ) {
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            Set<String> patterns = info.getPatternValues();
            if (!patterns.contains(expectedPathPattern)) {
                continue;
            }

            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            if (methods.isEmpty() || methods.contains(expectedMethod)) {
                return true;
            }
        }
        return false;
    }
}
