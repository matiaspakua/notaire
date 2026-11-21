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
| Issue | #2387 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2387-coach-junie/` | seeded |
| Branch | `feat/2387_coach_junie` | created |
| Tasks | `tasks.md` | in-progress |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Junie session counted in report | `test_coach.py` | pending |
| Junie prompts matched by project path | `test_coach.py` | pending |
| Junie tool calls counted per stepId | `test_coach.py` | pending |
| Junie model from modelUsage | `test_coach.py` | pending |
| EnvironmentVariablesUpdatedEvent never read | `test_coach.py` | pending |
| Prompt text never in JSON output | `test_coach.py` | pending |
| --days filters Junie sessions | `test_coach.py` | pending |
| Project filter excludes non-matching Junie sessions | `test_coach.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/CHANGELOG.md` | no | pending |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | no | pending |
| `docs/openspec/specs/factory-dashboard/spec.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` passed |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
