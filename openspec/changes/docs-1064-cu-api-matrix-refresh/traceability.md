# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens — never pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1064 | open |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/docs-1064-cu-api-matrix-refresh/` | drafted |
| Branch | `cursor/docs-1064-cu-api-matrix-refresh-69d3` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — docs/tooling, no release artifact | pending |
| Smoke test | `python3 scripts/validate-cu-api-matrix.py` + unittest | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Stale Spanish controller name is rejected | `scripts/tests/test_validate_cu_api_matrix.py` | pending |
| Current English controller names are accepted | same + repo CSV | pending |
| Missing required resource base fails validation | same | pending |
| All required resource bases present passes that check | same + repo CSV | pending |
| Status word in Bruno_Test is rejected | same | pending |
| Path and sentinel values are accepted | same | pending |
| MISSING without #953 is rejected | same | pending |
| Preflight list includes the matrix validator | same / preflight `--list` | pending |
| Unit tests prove stale fails and refreshed passes | unittest discover | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | pending | — |
| `docs/300-development/303-testing/TEST-PLAN.md` | pending | — |
| `docs/300-development/303-testing/README.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1064; this change folder |
| 2 | Failing tests written, test cases designed | pending | unittest red on stale fixture |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **Branch naming**: Cloud Agent task requires
  `cursor/docs-1064-cu-api-matrix-refresh-69d3`; Constitution form recorded via
  Issue #1064 / CU76.
- **Bruno fills**: deliberately out of scope; gaps remain `#953`.
- **Playwright**: n/a — no UI surface.
- **local-ai/**: never used (Cloud VM only).
