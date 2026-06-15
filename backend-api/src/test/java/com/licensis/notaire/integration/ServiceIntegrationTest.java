package com.licensis.notaire.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
public abstract class ServiceIntegrationTest {
    // Base class for service integration tests with transactional support
}
