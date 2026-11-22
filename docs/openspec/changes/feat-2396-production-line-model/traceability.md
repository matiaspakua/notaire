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
| Issue | #2396 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2396-production-line-model/` | passed Gate 1 |
| Branch | `feat/2396_production_line_model` | active |
| Tasks | `tasks.md` | 0/12 groups complete |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Line lists ordered stages with gates | `tests/test_line.py::test_ordered_stages_with_gates` | pending |
| inputs/outputs from profile override kit defaults | `tests/test_line.py::test_profile_io_overrides_defaults` | pending |
| Stage without inputs/outputs gets defaults and io\_source default | `tests/test_line.py::test_default_io_source` | pending |
| Guardrails expose protected paths, hooks, rules, isolator | `tests/test_line.py::test_guardrails_shape` | pending |
| GET /factory/line read scope | `tests/test_serve_line.py::test_get_factory_line` | pending |
| Stage with no gates still listed (via \[\[stages\]\] only) | `tests/test_line.py::test_stage_without_gates_listed` | pending |
| Text and --json output | `tests/test_line.py::test_json_output` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/CHANGELOG.md` | pending | pending |
| `framework/ai-sdlc/README.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this spec + `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
