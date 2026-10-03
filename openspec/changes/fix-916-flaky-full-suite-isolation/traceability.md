# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #916 | open (`in-progress` label blocked: `gh` write denied for this agent) |
| Use Case | CU15 – Procesar pago / CU16 – Archivar Gestión / CU76 – QA Infrastructure | exists on issue |
| Related | #848 overpayment guard (CLOSED — keep product behavior); CI run 33502758566 | referenced |
| Specification | `openspec/changes/fix-916-flaky-full-suite-isolation/` | Gate 1 in progress |
| Branch | `cursor/fix-916-flaky-full-suite-isolation-69d3` | created |
| Tasks | `tasks.md` | pending |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Payment create uses dedicated presupuesto after seed budget drained | `PaymentFixtureIsolationIntegrationTest#shouldAcceptPaymentAgainstDedicatedBudgetAfterSeedBudgetExhausted` | pending |
| BusinessWorkflow create payment does not hardcode seed id 1 | `BusinessWorkflowIntegrationTest$PaymentsWorkflow#createPaymentReturns200` | pending |
| RemainingControllers create payment does not hardcode seed id 1 | `RemainingControllersIntegrationTest$PaymentTests#shouldCreatePayment` | pending |
| SimpleControllers happy path is separate from RuntimeException stubs | `SimpleControllersTest` nested happy/error tests | pending |
| #848 overpayment still rejected | `ProcessPaymentServiceTest` / `PaymentUseCaseTest$OverpaymentValidationTests` | pending |
| Full suite green 5 consecutive times | local `mvn test -pl backend-api` ×5 | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | pending | |
| `docs/300-development/303-testing/TEST-PLAN.md` | pending | |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | pending | |
| `CHANGELOG.md` | n/a | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `openspec validate --strict` + `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | isolation proof red-then-green |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

- Label `in-progress` / issue comment could not be applied (`gh` GraphQL: Resource
  not accessible by integration); recorded here, not treated as a Constitution §12
  process exception.
- Cloud branch naming uses `cursor/fix-916-flaky-full-suite-isolation-69d3` as
  required by the agent platform (Constitution form would be
  `fix/916_flaky_full_suite_isolation`).
- OpenSpec store path `/cursor/stores/self/internal/openspec-916/` was not mounted
  on this VM; artifacts were scaffolded in-repo from issue #916 + coordinator
  decisions.
