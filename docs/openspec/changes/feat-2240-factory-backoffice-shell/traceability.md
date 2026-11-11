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
| Issue | #2240 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — factory control | exists |
| Specification | `docs/openspec/changes/feat-2240-factory-backoffice-shell/` | drafted |
| Branch | `feat/2240_factory-backoffice-shell` | created |
| Tasks | `tasks.md` | 0/N complete (Gate 1 filled) |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| shell has right nav and kit brand | `tests/test_backoffice_html.py` | pending |
| list and detail for agents and skills | `tests/test_backoffice_html.py` | pending |
| create and edit allocate Phase A versions | `tests/test_backoffice_serve.py` | pending |
| allowed transitions only | `tests/test_backoffice_html.py` | pending |
| approve cannot skip guards | `tests/test_backoffice_html.py` / serve | pending |
| history listed on detail | `tests/test_backoffice_html.py` | pending |
| secret refs redacted in HTML | `tests/test_backoffice_html.py` | pending |
| UI respects illegal transition | `tests/test_backoffice_serve.py` | pending |
| contract check green with backoffice note | `tests/test_contract.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | — |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | pending | — |
| `docs/openspec/specs/factory-backoffice-ui/spec.md` | pending (on archive) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Gate 1.6 WIP label SKIPPED — `addLabelsToLabelable` / issues:write 403 (FLEET-IDENTITY).
