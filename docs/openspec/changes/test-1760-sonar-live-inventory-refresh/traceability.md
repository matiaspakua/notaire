# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1760 | in progress |
| Use Case | CU78 / CU76 | exists |
| Specification | `docs/openspec/changes/test-1760-sonar-live-inventory-refresh/` | ready |
| Branch | `cursor/1760-sonar-live-inventory-refresh-69d3` | created |
| Tasks | `tasks.md` | mostly complete; PR pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Every vulnerability row is classified | `test_live_refresh_vuln_heuristics` + live inventory markdown | verified |
| Live refresh records provenance | `test_render_markdown_includes_live_refresh_note` | verified |
| Known live vulnerability clusters get stable heuristics | `test_live_refresh_vuln_heuristics` | verified |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/SONAR-LEGACY-VULN-INVENTORY.md` | yes (live) | pending |
| `CHANGELOG.md` | no (CHANGELOG-free) | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1760 + this change |
| 2 | Failing tests first | yes | unittest red then green |
| 3 | Suite green, docs | yes | 8 unittest OK; inventory live |
| 4 | CI green / heavy-CI | pending | pending |
| 5 | Merged; #1760 closed | pending | pending |

## Exceptions

- CHANGELOG Unreleased omitted intentionally (CHANGELOG-free while hex/peer traffic).
- Do **not** reopen #1740.
- Child #1762 tracks the remaining `fix` (`java:S4790`); umbrella #1535 stays open.
