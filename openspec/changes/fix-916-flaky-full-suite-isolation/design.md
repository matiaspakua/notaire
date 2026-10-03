> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #916: full-suite flakiness on main. Historical CI Actions run
`33502758566` showed payment ITs expecting 201 but getting 400 after
cross-class payments against seed `idBudget=1`. `ManagementArchiveIntegrationTest`
already creates its own budget. #848 overpayment guard is CLOSED product
behavior — keep it. Prefer fixtures over blanket `@DirtiesContext(AFTER_EACH)`.

## Goals / Non-Goals

**Goals:**

- Isolate payment mutation ITs via dedicated presupuesto fixtures.
- Prove isolation with a red-then-green test that drains seed budget `1` then
  pays against a new budget.
- Stabilize `SimpleControllersTest` by splitting happy/error paths.
- Five consecutive green local `mvn test -pl backend-api` runs.
- Keep #848 overpayment tests green.

**Non-Goals:**

- Changing overpayment HTTP mapping or business rules.
- Adding class-wide `@DirtiesContext(AFTER_EACH)` as the primary fix.
- Rewriting every GET-against-seed-id-1 smoke assertion.
- Frontend/Playwright changes.
- Touching `local-ai/`.

## Decisions

- **Fixture helpers over DirtiesContext**: add a small shared test helper
  (person + presupuesto create via MockMvc/JSON), reuse in
  `BusinessWorkflowIntegrationTest` and `RemainingControllersIntegrationTest`,
  matching `BudgetResumenControllerTest` / `ManagementArchiveIntegrationTest`.
- **Isolation proof IT**: new
  `PaymentFixtureIsolationIntegrationTest` exhausts seed budget `1` (or
  pays until rejected), then creates a dedicated budget and asserts payment
  201 — proves the suite no longer depends on shared seed saldo.
- **SimpleControllers split**: for each nested `all` / `allPaths`, move
  `thenThrow(new RuntimeException("x"))` (and follow-on assertions) into a
  sibling `@Test` with the same fresh field mocks JUnit already provides;
  attach `GlobalExceptionHandler` where status codes rely on advice.
- **Leave existing RemainingControllers `@DirtiesContext`**: do not expand it;
  do not remove in this change unless it blocks the fixture approach (it does not).
- **No `@Disabled`**.

## Riesgos / Trade-offs

- [Risk] Seed budget `1` amount / charge model changes → Mitigation: isolation
  proof uses dedicated budget for the success assertion; draining seed is only
  to demonstrate independence.
- [Risk] Splitting SimpleControllers increases method count → Mitigation:
  keep DisplayNames clear; no production code churn.
- [Risk] 5× full suite is slow on cloud VM → Mitigation: run after green once;
  capture log evidence under `/opt/cursor/artifacts/` and status file.

## Testing Strategy

TDD: write isolation proof and observe failure (or observe existing create
payment failure under drained seed), then fixture-isolate; split SimpleControllers
after a focused failing reproduction if needed.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Payment create uses dedicated presupuesto after seed budget drained | integration | `PaymentFixtureIsolationIntegrationTest#shouldAcceptPaymentAgainstDedicatedBudgetAfterSeedBudgetExhausted` |
| BusinessWorkflow payment create is fixture-isolated | integration | `BusinessWorkflowIntegrationTest$PaymentsWorkflow#createPaymentReturns200` |
| RemainingControllers payment create is fixture-isolated | integration | `RemainingControllersIntegrationTest$PaymentTests#shouldCreatePayment` |
| SimpleControllers happy path separate from RuntimeException stubs | unit | `SimpleControllersTest` nested happy/error methods |
| Overpayment still rejected | unit / integration | existing `ProcessPaymentServiceTest`, `PaymentUseCaseTest$OverpaymentValidationTests`, `PaymentControllerTest` |

- New unit tests: SimpleControllers split (same coverage, clearer methods)
- New integration tests: isolation proof + fixture helpers usage
- Coverage impact: should hold JaCoCo ratchet (test-only change)

## Regression Strategy

- Existing tests affected: `BusinessWorkflowIntegrationTest`,
  `RemainingControllersIntegrationTest`, `SimpleControllersTest`
- Must stay green: #848 overpayment suite (`PaymentUseCaseTest`,
  `ProcessPaymentServiceTest`, `PaymentUseCaseIntegrationTest`,
  `PaymentControllerTest`)
- Full suite command: `mvn test -pl backend-api` (×5 for AC); then
  `bash scripts/preflight.sh --fix` before push when feasible
- HTTP/Bruno: not required for test-infra-only change; skip if stack down
- Legacy `jpa` / Swing: out of scope

## Playwright Strategy

- n/a — no UI surface. Backend test isolation only; record in tasks §7.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none (tests only)
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `mvn test -pl backend-api` green; health
  endpoint unchanged

## Rollback Strategy

- Revert safe: yes — test-only revert via PR revert
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none in production; CI flake may return

## Migration Plan

1. Gate 1 artifacts + validate.
2. Add isolation proof (observe red under polluted seed or prove requirement).
3. Fixture-isolate BusinessWorkflow + RemainingControllers payment creates.
4. Split SimpleControllers mega methods.
5. Run #848 tests + 5× full suite.
6. Update TESTING-PATTERNS / TEST-PLAN / CU76 note.
7. Commit, push, draft PR.
