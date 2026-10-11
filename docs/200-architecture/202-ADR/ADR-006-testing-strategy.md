# ADR-006: Testing Strategy

**Status:** Accepted (updated 2026-10-11, #1511)  
**Date:** 2026-04-13  
**Deciders:** Matías Miguez  
**Related:** ADR-001, ADR-005, ADR-012, ADR-026, CU76

## Context

The project needs a clear testing strategy that covers the backend (Spring Boot),
the frontend (Next.js), black-box QA under `testing/`, and CI gates. Early text
in this ADR described a small Testing Trophy and tools (Page Object Model,
Gatling, MockMvc-as-contract) that no longer match practice. This update keeps
the same decision — multi-level automated testing with fail-fast feedback —
and records the current architecture.

## Decision

We adopt a **fail-fast test pyramid** (trophy-shaped middle for API/integration),
documented in the Master Test Plan:

- **MTP:** [`docs/300-development/303-testing/TEST-PLAN.md`](../../300-development/303-testing/TEST-PLAN.md)
- **Process architecture:** [`PROCESS-ARCHITECTURE.md`](../../300-development/303-testing/PROCESS-ARCHITECTURE.md)
- **Living inventory:** [`INVENTORY.md`](../../300-development/303-testing/INVENTORY.md)

```text
        Security / Performance (weekly / pipeline)
      Playwright E2E — TS-nnnn (GherkinSteps + API helpers)
    Bruno API + OpenAPI contract (oasdiff)
  Integration — Spring H2 + Testcontainers PG + DB V&V
Unit (JUnit 5) + Component (Vitest)
```

### Backend

| Type | Tools | Scope | Gate |
|------|-------|-------|------|
| Unit | JUnit 5 + Mockito + AssertJ | Services, validators | `mvn test` |
| Integration | `@SpringBootTest` + H2 / Testcontainers | Controllers, repositories | `ci.yml` |
| API | Bruno collection | HTTP contract | `playwright-e2e.yml` |
| OpenAPI | committed YAML + oasdiff | Breaking changes | `openapi-contract.yml` |
| Coverage | JaCoCo | Line/branch | **80% / 65%** enforced |
| Performance | k6 (`infra/performance/k6`) | Load baseline | weekly |
| Security | Semgrep/Trivy/CodeQL + ZAP | SAST/SCA/DAST | gates + weekly DAST |

### Frontend

| Type | Tools | Scope |
|------|-------|-------|
| Unit / component | Vitest + Testing Library | Components, hooks, utils |
| E2E | Playwright in `testing/e2e` | User journeys (`TS-nnnn-*.spec.ts`) |
| Accessibility | axe / dedicated QA specs | WCAG-oriented checks |

Vitest floors are **raise-only** in `vitest.config.ts` (currently ~14% lines);
the long-term 80% target is tracked separately (#1246).

### E2E conventions

- Nomenclature: `TS-nnnn-<workflow-name>.spec.ts` (mandatory; inventory guard).
- Fixtures: **GherkinSteps** (not a classical Page Object Model).
- Traceability: CU comments + [`CU-API-MATRIX.csv`](../../300-development/303-testing/CU-API-MATRIX.csv).
- Skip policy: open `#issue` for product gaps only (#1066).

### CI Pipeline (current)

```yaml
# Product PRs (path-scoped aggregators, #1257):
1. Backend: unit + integration (H2 + PG) + JaCoCo 80/65
2. Checkstyle / Spotless / SpotBugs / Trivy (as configured)
3. Frontend: tsc, ESLint, Vitest, Next build
4. Bruno + Playwright (when UI/API paths change)
5. Process: OpenSpec plan, CU matrix, testing inventory, TDD evidence
# Weekly / dispatch: DAST ZAP, k6
```

Local mirror: `bash workspace/sdlc/preflight.sh` (`--full` for stack-backed suites).

## Consequences

- Tagged/path-scoped jobs keep feedback fast without dropping required check names.
- Living inventory prevents MTP/README count rot (#1511).
- Bruno + OpenAPI replace “MockMvc as the only API contract” narrative.
- Large E2E inventory is accepted; criticality bands in the MTP guide ROI.
- Agents and humans share one MTP + process architecture under CU76.

## References

- Master Test Plan — `docs/300-development/303-testing/TEST-PLAN.md`
- Specialization guidelines (QA Automation / MTP / pyramid / traceability)
- [Testing Trophy (Kent C. Dodds)](https://kentcdodds.com/blog/the-testing-trophy-and-testing-classifications)
- [Playwright](https://playwright.dev/docs/intro) · [Testcontainers](https://java.testcontainers.org/)
