# CI starts the backend with Flyway-only ddl-auto

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1979 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/1979_ci-ddl-auto-none` |
| Gate 1 status | passed |

## Objetivo

E2E, DAST and k6 jobs override Hibernate with `ddl-auto=update`, so a missing Flyway
migration can still pass those suites. Align CI with ADR-007: schema comes from Flyway only.

## What Changes

- Set `spring.jpa.hibernate.ddl-auto=none` in `dast-zap.yml`, `performance-test.yml`, and both backend starts in `playwright-e2e.yml`
- Add a Process Checks guard that fails if those workflows set `ddl-auto=update` again

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Flyway is the single schema source of truth in production-like runs | ADR-007, CU76 | Made explicit in CI |

## Capabilities

### New Capabilities

- `ci-flyway-ddl-auto`: CI backend starts must not use Hibernate schema update

### Modified Capabilities

(none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `.github/workflows` | yes | ddl-auto flag |
| `workspace/tests` | yes | guard test |
| docs | yes | OpenSpec + CHANGELOG |

### Surface area

No product API or schema change; CI may fail if a migration is missing (desired).

### Architecture review

Enforces ADR-007 in the suites that previously hid gaps.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note |
| ADR-007 (cite only) | no edit unless prose claims CI uses update |

## Out of Scope

Full Spring profile separation (#583); production Dockerfile changes.
