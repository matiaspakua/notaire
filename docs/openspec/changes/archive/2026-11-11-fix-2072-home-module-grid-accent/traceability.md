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
| Issue | #2072 | open (Gate 1.6 SKIPPED — labels 403) |
| Use Case | RNF-05 – Aspecto visual | exists |
| Specification | `docs/openspec/changes/fix-2072-home-module-grid-accent/` | drafted |
| Branch | `fix/2072_home-module-grid-accent` | created |
| Tasks | `tasks.md` | Gate 1–8 complete |
| Commits | `ebfd405b` | committed |
| Pull Request | [#2234](https://github.com/matiaspakua/notaire/pull/2234) | open |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Module icon wells have no scale-110 | `frontend/src/tests/unit/dashboard-home-module-grid.test.tsx` | passing |
| At most one SF Blue module well | `frontend/src/tests/unit/dashboard-home-module-grid.test.tsx` | passing |
| Non-accent wells use mist | `frontend/src/tests/unit/dashboard-home-module-grid.test.tsx` | passing |
| Module links remain keyboard-reachable | `frontend/src/tests/unit/dashboard-home-module-grid.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | yes | `ebfd405b` |
| `CHANGELOG.md` | yes | `ebfd405b` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | OpenSpec change + validate scripts |
| 2 | Failing tests written, test cases designed | yes | `tdd-red.txt` → `tdd-green.txt` |
| 3 | Suite green, coverage held, docs updated | yes | `preflight-full.txt` PREFLIGHT PASSED |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Gate 1.6 honest SKIP: integration cannot add `status:in-progress` (addLabelsToLabelable 403).
