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
| Issue | #2242 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — factory control | exists |
| Specification | `docs/openspec/changes/feat-2242-factory-runtime-ui/` | drafted |
| Branch | `cursor/2242_factory-runtime-ui-201b` | created |
| Tasks | `tasks.md` | 0/N complete (Gate 1 filled) |
| Commits | pending | pending |
| Pull Request | pending push | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| runtime shell has Projects / Start / Monitor + kit brand | `tests/test_runtime_html.py` | pending |
| create project binds profile_ref | `tests/test_runtime_html.py` / serve | pending |
| bind repo defaults dry_run | `tests/test_runtime_serve.py` | pending |
| approved-only workflow picker | `tests/test_runtime_html.py` | pending |
| start run shows queued not running | `tests/test_runtime_serve.py` | pending |
| unapproved start refused | `tests/test_runtime_serve.py` | pending |
| monitor from evidence events; empty no-adapter | `tests/test_runtime_html.py` | pending |
| cancel honestly disabled | `tests/test_runtime_html.py` | pending |
| no commit/approve affordances | `tests/test_runtime_html.py` | pending |
| no second OpenAPI tree in Phase D | `tests/test_contract.py` / contract note | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | — |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | pending | — |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | pending | — |
| `docs/openspec/specs/factory-runtime-ui/spec.md` | pending (on archive) | — |
| `docs/openspec/specs/factory-contract/spec.md` | pending (on archive) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Gate 1.6 WIP label SKIPPED when `addLabelsToLabelable` / issues:write 403 (FLEET-IDENTITY).
Runtime loopback serve mirrors B/C host pattern (mechanical Deviation vs `/ui/` on
`aisdlc serve` only) — same `projects`/`runs` ports; OpenAPI remains SoR.
CHANGELOG Unreleased omit while fence held (#2271).
