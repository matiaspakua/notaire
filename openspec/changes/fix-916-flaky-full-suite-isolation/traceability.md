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
| Specification | `openspec/changes/fix-916-flaky-full-suite-isolation/` | Gate 1 passed |
| Branch | `cursor/fix-916-flaky-full-suite-isolation-69d3` | created |
| Tasks | `tasks.md` | implementation complete locally |
| Commits | `9afe1356`, `07457ef1` (+ follow-up status commit if any) | pushed |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1177 | draft |
| CI run | | pending on PR |
| Merge commit | | pending (do not merge from this agent) |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Payment create uses dedicated presupuesto after seed budget drained | `PaymentFixtureIsolationIntegrationTest#shouldAcceptPaymentAgainstDedicatedBudgetAfterSeedBudgetExhausted` | implemented |
| BusinessWorkflow payment create is fixture-isolated | `BusinessWorkflowIntegrationTest$PaymentsWorkflow#createPaymentReturns200` | implemented |
| RemainingControllers payment create is fixture-isolated | `RemainingControllersIntegrationTest$PaymentTests#shouldCreatePayment` | implemented |
| SimpleControllers happy path is separate from RuntimeException stubs | `SimpleControllersTest` nested happy/error tests | implemented |
| Overpayment still rejected when monto exceeds saldo | `ProcessPaymentServiceTest`, `PaymentUseCaseTest$OverpaymentValidationTests`, `PaymentControllerTest` | green |
| Full suite green 5 consecutive times | local `mvn test -pl backend-api` ×5 | green (`/opt/cursor/artifacts/916-five-suite.log`) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | yes | `07457ef1` |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | `07457ef1` |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | `07457ef1` |
| `CHANGELOG.md` | n/a | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | yes | isolation proof red (201 vs 409) then green with fixture |
| 3 | Suite green, coverage held, docs updated | yes (local) | 5× `mvn test -pl backend-api` green; docs updated |
| 4 | CI green, review approved, no conflicts | pending | PR #1177 |
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
