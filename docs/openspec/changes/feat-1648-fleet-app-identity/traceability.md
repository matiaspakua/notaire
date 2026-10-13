# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1648 | in-progress |
| Use Case | CU78 — Security, Privacy and Compliance | exists |
| Specification | `docs/openspec/changes/feat-1648-fleet-app-identity/` | archived in this PR |
| Branch | `feat/1648_fleet-app-identity` | created |
| Tasks | `tasks.md` | groups 1–10.1 complete |
| Commits |  | pending |
| Pull Request |  | pending |
| CI run |  | pending |
| Merge commit |  | pending |
| Release / tag |  | pending |
| Smoke test |  | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| JWT is signed by the App key | `FleetTokenTest.test_jwt_is_rs256_signed_by_the_app_key` | passing |
| Without App credentials it falls back loudly | `FleetTokenTest.test_without_app_credentials_it_falls_back_loudly` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> |  |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1648 |
| 2 | Failing tests written, test cases designed | yes | 2 FAIL without script |
| 3 | Suite green, coverage held, docs updated | yes | tests OK |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
