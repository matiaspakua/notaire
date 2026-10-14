package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Static JPA access is retired (issue #574)")
class BudgetTemplateProviderArchitectureTest {

    private static final String PROVIDER = "com.licensis.notaire.config.JpaControllerProvider";
    private static final String CONTROLLER = "com.licensis.notaire.adapter.in.web.budget.BudgetTemplateController";

    @Test
    @DisplayName("JpaControllerProvider no longer exists")
    void providerIsGone() {
        assertThatThrownBy(() -> Class.forName(PROVIDER)).isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("the controller depends on a service, not on the legacy JPA controller")
    void controllerDependsOnTheService() throws ClassNotFoundException {
        Class<?> controller = Class.forName(CONTROLLER);

        assertThat(controller.getDeclaredConstructors()[0].getParameterTypes())
                .extracting(Class::getSimpleName)
                .contains("BudgetTemplateCatalogService");
    }
}
