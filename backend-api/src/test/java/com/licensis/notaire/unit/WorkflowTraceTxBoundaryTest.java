package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.adapter.in.web.management.ManagementController;
import com.licensis.notaire.testing.RequirementCoverage;

/**
 * #1745 / CU77: GET workflow-trace must not own a controller transaction that
 * swallows IllegalArgumentException and then fails commit with
 * UnexpectedRollbackException.
 */
@RequirementCoverage({"CU77", "CU83"})
@DisplayName("CU77/CU83 — workflow-trace transaction boundary")
class WorkflowTraceTxBoundaryTest {

    @Test
    @DisplayName("getWorkflowTrace has no @Transactional annotation")
    void getWorkflowTraceHasNoTransactionalAnnotation() throws Exception {
        Method method = ManagementController.class.getDeclaredMethod("getWorkflowTrace", Integer.class);
        assertThat(method.getAnnotation(Transactional.class))
                .as("controller must not own a TX that catches IllegalArgumentException then commits")
                .isNull();
    }
}
