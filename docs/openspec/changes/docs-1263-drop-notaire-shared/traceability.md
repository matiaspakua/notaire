# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1263 | open (label `in-progress` may need Owner — agent `gh` often 403) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Related | #1255 / ADR-025 (retirement) | referenced |
| Specification | `docs/openspec/changes/docs-1263-drop-notaire-shared/` | Gate 1 draft |
| Branch | `cursor/docs-1263-drop-notaire-shared-debd` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | n/a docs |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Constitution does not list notaire-shared as a live module | `workspace/tests/test_notaire_shared_retired.py` | pending |
| External services use the API (module not listed as live) | same (`DOCS_AS_NON_LIVE` includes CONSTITUTION.md) | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash workspace/sdlc/validate-sdlc-plan.sh docs-1263-drop-notaire-shared` |
| 2 | Failing tests written, test cases designed | pending | red guard after CONSTITUTION added to list |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Constitution amendment follows §12 (Owner merges).
