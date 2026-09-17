package com.licensis.notaire.unit;

import com.licensis.notaire.audit.AuditAspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization test that pins the <em>actual AspectJ pointcut expression</em>
 * declared by {@link AuditAspect}, rather than the advice body.
 *
 * <p>{@code AuditAspectTest} invokes the advice method directly with a mocked
 * {@code JoinPoint}, so it passes regardless of whether the pointcut still matches a
 * given controller. That leaves a blind spot: relocating a controller out of
 * {@code com.licensis.notaire.api} silently stops auditing it, with no failing test.
 *
 * <p>This test closes that blind spot for the Payment slice (CU15 / CU47), which is
 * restructured into a hexagonal inbound web adapter. It deliberately resolves the
 * controller by <em>either</em> its legacy or its adapter package, so the invariant
 * under test is "the payment controller is audited", independently of where it lives.
 */
@DisplayName("AuditAspect pointcut coverage")
class AuditPointcutCoverageTest {

    private static final List<String> PAYMENT_CONTROLLER_CANDIDATES = List.of(
            "com.licensis.notaire.adapter.in.web.payment.PaymentController",
            "com.licensis.notaire.api.PaymentController");

    private static AspectJExpressionPointcut auditPointcut() throws NoSuchMethodException {
        Method pointcutMethod = AuditAspect.class.getMethod("controllerMethods");
        Pointcut declared = pointcutMethod.getAnnotation(Pointcut.class);
        assertThat(declared)
                .as("AuditAspect.controllerMethods() must declare the @Pointcut expression")
                .isNotNull();

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(declared.value());
        return pointcut;
    }

    private static Class<?> paymentController() {
        Optional<Class<?>> found = PAYMENT_CONTROLLER_CANDIDATES.stream()
                .map(AuditPointcutCoverageTest::loadOrNull)
                .filter(java.util.Objects::nonNull)
                .findFirst();

        assertThat(found)
                .as("PaymentController must exist in one of %s", PAYMENT_CONTROLLER_CANDIDATES)
                .isPresent();
        return found.get();
    }

    private static Class<?> loadOrNull(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean isCandidateMethod(Method method) {
        return !method.isSynthetic() && Modifier.isPublic(method.getModifiers());
    }

    private static void assertControllerIsAudited(Class<?> controller) throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = auditPointcut();

        assertThat(controller.getSimpleName())
                .as("audited modules are resolved from the *Controller naming convention")
                .endsWith("Controller");

        List<String> matched = java.util.Arrays.stream(controller.getDeclaredMethods())
                .filter(AuditPointcutCoverageTest::isCandidateMethod)
                .filter(method -> pointcut.matches(method, controller))
                .map(Method::getName)
                .toList();

        assertThat(matched)
                .as("audit pointcut '%s' must match the public methods of %s",
                        pointcut.getExpression(), controller.getName())
                .isNotEmpty();
    }

    @Test
    @DisplayName("Audit pointcut should match the payment controller wherever it is packaged (CU15)")
    void shouldMatchPaymentController() throws Exception {
        assertControllerIsAudited(paymentController());
    }

    @Test
    @DisplayName("Audit pointcut should match the payment write operations that mutate state")
    void shouldMatchPaymentWriteOperations() throws Exception {
        AspectJExpressionPointcut pointcut = auditPointcut();
        Class<?> controller = paymentController();

        List<String> writeMethods = java.util.Arrays.stream(controller.getDeclaredMethods())
                .filter(AuditPointcutCoverageTest::isCandidateMethod)
                .map(Method::getName)
                .filter(name -> name.startsWith("process") || name.equals("update") || name.equals("delete"))
                .toList();

        assertThat(writeMethods)
                .as("payment controller must still expose create/update/delete operations")
                .isNotEmpty();

        for (Method method : controller.getDeclaredMethods()) {
            if (!isCandidateMethod(method) || !writeMethods.contains(method.getName())) {
                continue;
            }
            assertThat(pointcut.matches(method, controller))
                    .as("mutating operation %s.%s must be audited",
                            controller.getSimpleName(), method.getName())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Audit pointcut should still match legacy api-package controllers")
    void shouldMatchLegacyApiPackageControllers() throws Exception {
        assertControllerIsAudited(Class.forName("com.licensis.notaire.adapter.in.web.budget.BudgetController"));
    }

    @Test
    @DisplayName("Audit pointcut should exclude AuditRecordController to avoid recursive auditing")
    void shouldExcludeAuditRecordController() throws Exception {
        AspectJExpressionPointcut pointcut = auditPointcut();
        Class<?> auditRecordController = Class.forName("com.licensis.notaire.adapter.in.web.audit.AuditRecordController");

        for (Method method : auditRecordController.getDeclaredMethods()) {
            if (!isCandidateMethod(method)) {
                continue;
            }
            assertThat(pointcut.matches(method, auditRecordController))
                    .as("AuditRecordController.%s must not be audited", method.getName())
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Payment controller should remain a @RestController inbound adapter")
    void paymentControllerShouldRemainRestController() {
        assertThat(paymentController().getAnnotation(RestController.class))
                .as("PaymentController must remain a @RestController inbound adapter")
                .isNotNull();
    }
}
