# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2163 | in-progress |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2163-wilson-l0-evidence/` | passed |
| Branch | `cursor/feat-2163-wilson-l0-evidence-d324` | created |
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
| No signal / small n → no-evaluable L0 | AutonomyWilsonTest | passing |
| Skip excluded from Wilson | AutonomyWilsonTest | passing |
| Wilson floor → L0 eligible not L3 | AutonomyWilsonTest | passing |
| Incident → demoted L1 | AutonomyWilsonTest | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2163, this folder |
| 2 | Failing tests written | pending | pending |
| 3 | Suite green, docs | pending | pending |
| 4 | CI / review | pending | pending |
| 5 | Deploy / close | pending | pending |

## Exceptions

None.
