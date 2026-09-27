# Design: Remove Dead Default Credentials

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The `backend-api/src/main/resources/application.properties` file contains
dead default security credential keys (`spring.security.user.name=admin`,
`spring.security.user.password=admin`, `spring.security.user.roles`) on lines
92-94 with a misleading comment about Prometheus scraping. These keys are no
longer used by the running application — Spring Security now authenticates
via JWT tokens, and Actuator endpoints use the separate `actuator.security.*`
keys.

Additionally, `backend-api/src/main/resources/config.properties` is a legacy
Swing-era configuration file with database credentials that is no longer
referenced by any code in the modernized stack.

## Goals / Non-Goals

**Goals:**
- Remove dead `spring.security.user.*` keys from `application.properties` to
  eliminate confusion and avoid overriding the runtime security configuration.
- Remove the misleading Prometheus scraping comment.
- Delete the unused `config.properties` file.

**Non-Goals:**
- Adding new credential validation logic — the existing `ProductionCredentialsGuard`
  already enforces datasource credential security at startup.
- Changing application behavior — this is configuration cleanup only.

## Decisions

- **Remove configuration artifacts directly.** No abstraction or migration step
  is needed; dead credentials are not actively used and removing them has no
  runtime impact.
- **Do not add new configuration keys.** The application reads datasource
  credentials from `.env` (git-ignored) and validates them at startup.

## Riesgos / Trade-offs

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Removing `spring.security.user.*` breaks Actuator auth | Low | Medium | Actuator uses separate `actuator.security.*` keys; these are also secured by `ProductionCredentialsGuard` |
| Deleting `config.properties` breaks local dev | Low | Low | Credentials come from `.env`; `config.properties` is unused in modern Docker-based workflow |
| CI build fails due to missing resource | Low | Low | Spring handles missing resources gracefully; verify with `mvn clean install` |

## Testing Strategy

### Unit Tests
Two new unit tests will be added and made to fail before implementation:

1. `ApplicationPropertiesHygieneTest.shouldNotDefineDeadSecurityUserKeys`
   - **Under test**: `ApplicationPropertiesHygiene` utility class
   - **Test method**: `shouldNotDefineDeadSecurityUserKeys`
   - **Input**: A `Properties` object containing application.properties content
   - **Assertion**: No key starts with `spring.security.user.` prefix
   - **Expected result**: Test FAILS initially (dead keys present), PASSES after fix

2. `ApplicationResourceTest.shouldNotIncludeConfigProperties`
   - **Under test**: Resource loading mechanism
   - **Test method**: `shouldNotIncludeConfigProperties`
   - **Input**: Resource name `/config.properties`
   - **Assertion**: Resource is null/absent
   - **Expected result**: Test FAILS initially (file exists), PASSES after deletion

### Integration Tests
**n/a** — The dead keys being removed do not affect integration test assertions.
Existing tests for datasource connection, authentication, and other features
will continue to use credentials from environment variables.

## Playwright Strategy

n/a — no UI surface. This change touches only backend configuration files (`application.properties`,
`config.properties`), not any frontend components or browser-visible behavior.

## Regression Strategy

### Affected Areas
- **CI/CD**: The `config.properties` file may be referenced in build scripts.
  After deletion, verify `mvn clean install` still completes without resource
  lookup errors.
- **Local development**: Developers should check `.env` for datasource
  credentials, not rely on defaults in configuration files.
- **Actuator security**: The dead keys (`spring.security.user.*`) are a
  legacy artifact. Actuator endpoints now use `actuator.security.*` keys.

### Validation Commands
```bash
# Verify backend still builds
mvn clean install -pl backend-api -am

# Run all backend tests
mvn test -pl backend-api

# Specifically verify ApplicationPropertiesHygieneTest
mvn test -pl backend-api -Dtest=ApplicationPropertiesHygieneTest

# Verify ApplicationResourceTest
mvn test -pl backend-api -Dtest=ApplicationResourceTest

# Run frontend E2E (ensure no regression)
cd frontend && npx playwright test
```

## Deployment Strategy

- **Flyway migration required**: no
- **Deployment order / coupling**: none — configuration cleanup only
- **Configuration or `.env` keys to add**: none
- **Feature flag**: no
- **Smoke test after deploy (Gate 5)**: `mvn verify -pl backend-api` and
  `mvn verify` — all quality gates green; E2E suite unaffected

## Rollback Strategy

- **Revert safe**: yes — a plain `git revert` restores the previous config files;
  no other state depends on the removal.
- **Database rollback**: none needed.
- **Data written under the new behavior**: none — this only affects configuration.
- **Blast radius**: none beyond restoration of the dead keys.

## Migration Plan

Not applicable — single-file, single-line change with no staged rollout.
