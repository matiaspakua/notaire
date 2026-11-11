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
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| GET factory paths succeed with bearer token | test_serve | pending |
| mutating methods are refused | test_serve | pending |
| default loopback | test_serve | pending |
| remote bind refused | test_serve | pending |
| missing or invalid token yields 401 | test_serve | pending |
| token file mode and creation | test_serve | pending |
| capabilities honesty | test_serve | pending |
| traversal id rejected | test_serve | pending |
| secret redaction | test_serve | pending |
| static mount empty | test_serve | pending |
| contract check | test_contract | pending |
| capabilities path present | test_contract / test_serve | pending |
| entity and workflow schemas present | test_contract | pending |
| contract check green with backoffice note | test_contract | pending |
| no second OpenAPI write tree in Phase C | test_contract | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | no | pending |
| `docs/300-development/303-testing/INVENTORY.md` | no | pending |
| `CHANGELOG.md` | n/a fence | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2253, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
