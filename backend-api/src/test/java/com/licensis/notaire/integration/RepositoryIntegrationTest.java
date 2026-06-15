package com.licensis.notaire.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test-h2")
public abstract class RepositoryIntegrationTest {
    // Base class for repository integration tests
}
