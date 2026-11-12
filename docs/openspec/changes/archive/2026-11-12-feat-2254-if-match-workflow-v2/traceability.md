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
| Issue | #2254 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2254-if-match-workflow-v2/` | passed |
| Branch | `cursor/2254-if-match-workflow-v2-55fb` | created |
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
| entity carries schema_version | test_schema_migrate | pending |
| migrate upgrades workflow v1 to v2 | test_schema_migrate | pending |
| migrate is idempotent and records evidence | test_schema_migrate | pending |
| workflow body v2 shape | test_workflow_v2 | pending |
| request_validation pins digests | test_workflow_v2 | pending |
| approve fails on pin digest drift | test_workflow_v2 | pending |
| new workflow version returns to draft | test_workflow_v2 | pending |
| If-Match mismatch yields conflict | test_if_match_writes | pending |
| two writers cannot lose an update | test_if_match_writes | pending |
| created_by is server-derived | test_if_match_writes | pending |
| PUT with If-Match succeeds under --write | test_serve_writes | pending |
| PUT without --write remains 405 | test_serve_writes | pending |
| missing CSRF or insufficient scope is refused | test_serve_writes | pending |
| If-Match and schema fields appear in OpenAPI | test_contract | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | pending |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | pending | pending |
| `docs/300-development/303-testing/INVENTORY.md` | pending | pending |
| `CHANGELOG.md` | n/a fence (#2271) | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2254, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
