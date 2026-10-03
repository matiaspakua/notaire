# Fix flaky full-suite tests: SimpleControllers + Pago isolation

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #916 |
| Use Case | CU15 – Procesar pago / CU16 – Archivar Gestión (test infrastructure risk); CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-916-flaky-full-suite-isolation-69d3` |
| Gate 1 status | passed (local validate) |

## Objetivo

Full-suite `mvn test -pl backend-api` intermittently fails because (1) H2 payment
ITs hardcode seed `idBudget=1` and accumulate payments across shared Spring
contexts until the #848 overpayment guard returns 400/409 instead of 201, and
(2) `SimpleControllersTest` mega `all`/`allPaths` methods mix happy-path stubs
with `RuntimeException("x")` error stubs, which is non-deterministic under
full-suite ordering. Stabilize both without weakening product overpayment behavior.

## What Changes

- Fixture-isolate payment create paths in `BusinessWorkflowIntegrationTest` and
  `RemainingControllersIntegrationTest` (follow `ManagementArchiveIntegrationTest`
  / `BudgetResumenControllerTest` pattern: create own person + presupuesto).
- Add a failing-then-green isolation proof that payments against a dedicated
  presupuesto succeed even after seed budget `1` has no remaining saldo.
- Split/stabilize `SimpleControllersTest` mega `all`/`allPaths` methods into
  separate happy-path and error-path tests with fresh mocks / consistent
  `@ControllerAdvice`.
- Prefer isolated presupuesto fixtures over adding blanket
  `@DirtiesContext(AFTER_EACH)`.
- Keep #848 overpayment unit/integration tests green (product behavior unchanged).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Payment monto MUST NOT exceed saldo pendiente | CU15 / #848 / `pago-limite-saldo-pendiente` | Unchanged (product); tests must not fight it |
| Backend full-suite ITs that mutate payments MUST arrange their own presupuesto fixture | CU76 / Issue #916 | New (test infra rule) |
| Controller unit mega-tests MUST NOT mix happy + throw stubs in one method that can leak | CU76 / Issue #916 | New (test infra rule) |
| No `@Disabled` without documented justification | CONSTITUTION §7 | Made explicit for this change |

## Capabilities

### New Capabilities

- `backend-test-isolation`: Rules for H2 payment IT fixture isolation and
  SimpleControllers unit-test stability under full-suite runs.

### Modified Capabilities

- None at permanent product-spec level (`pago-limite-saldo-pendiente` stays as-is).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Test classes / helpers only |
| `frontend` | no | — |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none (test fixtures only)
- Endpoints: no production contract change
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING: no

### Architecture review

Follows existing IT fixture helpers (`ManagementArchiveIntegrationTest`,
`BudgetResumenControllerTest`). No ADR. Does not reintroduce blanket
`@DirtiesContext` as the primary isolation strategy.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | Document presupuesto/payment IT fixture isolation (own budget per mutating class; avoid hardcoding seed id 1) |
| `docs/300-development/303-testing/TEST-PLAN.md` | Note full-suite isolation / anti-flake for payment ITs + SimpleControllers split |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Brief note that suite isolation for payment ITs is a CU76 reliability concern |
| `CHANGELOG.md` | n/a — not user-visible product behavior |

## Out of Scope

- Changing #848 overpayment product rules or HTTP status mapping
- Broad `@DirtiesContext(AFTER_EACH)` on all H2 ITs
- Rewriting all integration tests that only GET seed presupuesto `1`
- Frontend / Playwright UI changes
- `local-ai/`
