# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #655 | open → in progress |
| Use Case | CU15 – Procesar pago | exists |
| Specification | `docs/openspec/changes/fix-655-payment-request-validation/` | Gate 1 draft |
| Branch | `fix/655_payment_request_validation` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create without budget or amount | `PaymentControllerTest` | passing |
| Create with non-positive amount | `PaymentControllerTest` | passing |
| Update with non-positive amount | `PaymentControllerTest, Bruno payments/08a` | passing |
| Partial update without amount | `PaymentControllerTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-655-payment-request-validation` |
| 2 | Failing tests written, test cases designed | yes | 6 new `PaymentControllerTest` cases observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | payment unit/integration tests and Bruno payments (15 requests, 26 tests) green; backend full suite run before push; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`scripts/run_pipeline.sh` needs Docker, unavailable on the agent box. The OpenAPI breaking-diff check (not a required check) flags 4 ERR changes that only document already-enforced rules; Owner to accept.
