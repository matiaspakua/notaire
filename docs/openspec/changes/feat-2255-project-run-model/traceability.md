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
| Issue | #2255 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2255-project-run-model/` | passed |
| Branch | `cursor/2255-project-run-model-b926` | created |
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
| project create defaults dry_run | test_projects | pending |
| start run requires approved workflow | test_runs | pending |
| start run appends RunRequest evidence | test_runs | pending |
| status stays queued without harness events | test_runs | pending |
| harness report moves run to running then finished | test_harness_report | pending |
| budget overrun marks blocked; skip is never pass | test_harness_report | pending |
| evidence verify covers new run events | test_runs | pending |
| HTTP GET/POST projects and runs | test_serve_runs | pending |
| cancel returns 501 with capability reason | test_serve_runs | pending |
| OpenAPI Project Run RunRequest schemas present | test_contract | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | pending |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | pending | pending |
| `framework/ai-sdlc/docs/HARNESS-REPORTING.md` | pending | pending |
| `CHANGELOG.md` | n/a fence (#2271) | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2255, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
