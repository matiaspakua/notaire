package com.licensis.notaire.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Issue #655, Owner decision (Oct 9): an optimistic-lock failure that escapes a controller (a
 * stale {@code version}) answers {@code 409 Conflict} with a safe message instead of 500.
 */
class GlobalExceptionHandlerOptimisticLockTest {

    @RestController
    static class StaleController {
        @PutMapping("/spring")
        void spring() {
            throw new ObjectOptimisticLockingFailureException("com.licensis.notaire.business.FolioType", 1);
        }

        @PutMapping("/jpa")
        void jpa() {
            throw new jakarta.persistence.OptimisticLockException("Row was updated or deleted by another transaction");
        }
    }

    private final MockMvc mvc = standaloneSetup(new StaleController())
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    @DisplayName("Spring and JPA optimistic-lock failures answer 409 without leaking entity names")
    void optimisticLockIsConflict() throws Exception {
        for (String path : new String[] {"/spring", "/jpa"}) {
            mvc.perform(put(path))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message")
                            .value("The record was modified by another user; reload it and try again"));
        }
    }
}
