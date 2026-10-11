# ADR-006: Testing Strategy

**Status:** Accepted
**Date:** 2026-04-13
**Deciders:** Matías Miguez
**Related:** ADR-001, ADR-005

## Context

The project needs a clear testing strategy that covers every part of the
system: the backend (Spring Boot), the frontend (Next.js), and the full stack
deployed on Kubernetes.

## Decision

We adopt the **Testing Trophy**, adapted to the current stack:

```
        ┌──────────────────────────────┐
        │   E2E Tests (Playwright)     │  ← Full business flows
        │   ~10-20 critical tests      │
        └──────────────────────────────┘
      ┌────────────────────────────────────┐
      │   Integration Tests                │  ← API tests, DB tests
      │   (Spring Boot Test + H2/PG)       │
      │   ~50-100 tests                    │
      └────────────────────────────────────┘
    ┌────────────────────────────────────────────┐
    │   Unit Tests (JUnit 5 + Vitest)            │  ← Business logic
    │   Backend: services, validators            │
    │   Frontend: components, hooks, utils       │
    │   ~200+ tests                              │
    └────────────────────────────────────────────┘
```

### Backend Testing (Java)

| Type | Tools | Scope | Requirement |
|------|-------------|-------|-----------|
| Unit | JUnit 5 + Mockito + AssertJ | Services, validators | 80% coverage |
| Integration | @SpringBootTest + Testcontainers | Controllers, repositories | Every endpoint |
| API Contract | MockMvc + Spring Security Test | Auth, roles, status codes | Happy and sad paths |
| Performance | Gatling / k6 | API under load | Baseline documented |
| Security | OWASP ZAP | Every endpoint | 0 CRITICAL/HIGH |

**Backend conventions:**

- Methods: `shouldXxxWhenYyy()` with `@DisplayName`
- Pattern: Arrange / Act / Assert
- One assert per test when possible
- Tests in `src/test/java/.../unit/` and `integration/`
- Tagging with `@Tag("unit")` and `@Tag("integration")`

### Frontend Testing (TypeScript)

| Type | Tools | Scope |
|------|-------------|-------|
| Unit | Vitest + Testing Library | Components, hooks, utils |
| Visual | Storybook (optional) | Component states |
| E2E | Playwright | Complete user journeys |
| Accessibility | axe-core + Playwright | WCAG 2.1 compliance |

**Frontend conventions:**

- Test files: `*.test.ts` next to the code
- E2E in `testing/e2e/tests/*.spec.ts`
- Page Object Model for E2E

### E2E Tests — Priority flows

| Flow | Use cases | Priority |
|-------|-----|-----------|
| Login → full management → payment | CU01, CU02, CU15, CU16 | Critical |
| Register a client → search | CU17, CU18, CU19, CU61 | High |
| Budget → deed → testimony | CU01, CU05, CU07 | High |
| Admin: register a user + substitution | CU20, CU22 | Medium |
| Generate the monthly report | CU24, CU25 | Medium |

### CI Pipeline

```yaml
# On every pull request:
1. Backend: mvn test (unit + integration)
2. Backend: JaCoCo coverage report (80% gate)
3. Backend: Checkstyle + SpotBugs
4. Backend: Trivy vulnerability scan
5. Frontend: vitest run
6. Frontend: playwright test (headless)
7. Build: Docker images
8. Security: OWASP ZAP (weekly on main)
```

## Consequences

- Tagged tests let us run only unit tests for fast feedback
- Testcontainers removes differences between H2 and PostgreSQL in integration tests
- Playwright E2E in CI checks that the 68 use cases work end to end
- The 80% coverage gate keeps backend quality

## References

- [Testing Trophy (Kent C. Dodds)](https://kentcdodds.com/blog/the-testing-trophy-and-testing-classifications)
- [Spring Boot Testing Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/test-auto-configuration.html)
- [Testcontainers for Java](https://java.testcontainers.org/)
- [Playwright Documentation](https://playwright.dev/docs/intro)
