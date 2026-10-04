# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1210 (follows #1192) | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists (add #1192 to the ID table) |
| Related | #1192 / PR #1212 (the move), #1190 (umbrella) | referenced |
| Specification | `openspec/changes/docs-1210-constitution-playwright-path/` | Gate 1 draft |
| Branch | `docs/1210_constitution_playwright_path` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | `74cf3dc` red guards, `a6b964b` pure rename, `43b0e9e` constants + packaging, `e254bee` frontend removal, `b6b5c9d` gates, `5aa6670` docs | done |
| Pull Request | — | passed |
| CI run | — | passed |
| Merge commit | — | passed |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No stale Playwright path in the Constitution | `scripts/test_testing_standalone.py` (`E2ELegacyReferenceTest`) | pending |
| The E2E command points at testing/e2e | same | pending |
| Agent rule files stay consistent | `bash scripts/check-agent-rules.sh` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | done | this PR |
| `CHANGELOG.md` | done | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh docs-1210-constitution-playwright-path` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.

