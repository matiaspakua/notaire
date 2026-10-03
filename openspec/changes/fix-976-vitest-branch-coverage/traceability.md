# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #976 | open (implement in progress) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | audit-2026-09; Frontend CI Vitest job; JaCoCo ratchet analogy | referenced |
| Specification | `openspec/changes/fix-976-vitest-branch-coverage/` | Gate 1 applied |
| Branch | `cursor/fix-976-vitest-branch-coverage-69d3` | from `origin/main` @ `68dc2cac` |
| Tasks | `tasks.md` | implement underway |
| Commits | `10d1d67d` (impl), `bfd10489` (traceability SHA note) | done |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1171 | draft open |
| CI run | pending Frontend CI / heavy gate | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Implement-time measurement (`origin/main` @ `68dc2cac`)

```
Statements : 15.09%
Branches   : 10.52%
Functions  : 11.97%
Lines      : 15.55%
```

Chosen floors (≈1pp headroom): statements 14 / branches 9 / functions 10 / lines 14.

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Root cause documented | `.claude/rules/code-quality.md`, FRONTEND-TESTING-GUIDE, CHANGELOG | done |
| Thresholds raised under measured coverage | `vitest.config.ts` + `npx vitest run --coverage` exit 0 | done |
| Frontend CI Vitest job green | CI on PR | pending |
| Raise-only policy documented | code-quality + FRONTEND-TESTING-GUIDE + TEST-COVERAGE-STRATEGY | done |
| Floors not silently lowered | `vitest-coverage-thresholds.test.ts` | done (TDD red→green) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `.claude/rules/code-quality.md` | yes | pending |
| `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` | yes | pending |
| `docs/300-development/303-testing/test-coverage/TEST-COVERAGE-STRATEGY.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this folder |
| 2 | Failing tests written, test cases designed | yes | guard test failed then passed |
| 3 | Suite green, coverage held, docs updated | yes (local) | vitest + lint + typecheck |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Project board IN PROGRESS move blocked by gh permissions; coordinator may update.
