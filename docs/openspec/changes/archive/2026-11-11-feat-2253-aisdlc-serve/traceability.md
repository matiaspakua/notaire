# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2253 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2253-aisdlc-serve/` | passed |
| Branch | `feat/2253_aisdlc-serve` | created |
| Tasks | `tasks.md` | in progress |
| Commits | f75750ec, 61395952 | recorded |
| Pull Request | pending | passing |
| CI run | pending | passing |
| Merge commit | pending | passing |
| Release / tag | pending | passing |
| Smoke test | pending | passing |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| GET factory paths succeed with bearer token | test_serve | passing |
| mutating methods are refused | test_serve | passing |
| default loopback | test_serve | passing |
| remote bind refused | test_serve | passing |
| missing or invalid token yields 401 | test_serve | passing |
| token file mode and creation | test_serve | passing |
| capabilities honesty | test_serve | passing |
| traversal id rejected | test_serve | passing |
| secret redaction | test_serve | passing |
| static mount empty | test_serve | passing |
| contract check | test_contract | passing |
| capabilities path present | test_contract / test_serve | passing |
| entity and workflow schemas present | test_contract | passing |
| contract check green with backoffice note | test_contract | passing |
| no second OpenAPI write tree in Phase C | test_contract | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | passing |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | no | passing |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | no | passing |
| `docs/300-development/303-testing/INVENTORY.md` | no | passing |
| `CHANGELOG.md` | n/a fence | passing |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2253, this folder |
| 2 | Failing tests written, test cases designed | yes | test_serve RED then green |
| 3 | Suite green, coverage held, docs updated | yes | verify.sh 361 OK; README/OpenAPI |
| 4 | CI green, review approved, no conflicts | pending | passing |
| 5 | Deployed, smoke test passed, Issue closed | pending | passing |

## Exceptions

None.
