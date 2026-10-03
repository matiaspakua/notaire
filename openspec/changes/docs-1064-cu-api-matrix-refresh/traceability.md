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
| Specification | `openspec/changes/docs-1064-cu-api-matrix-refresh/` | complete |
| Branch | `cursor/docs-1064-cu-api-matrix-refresh-69d3` | created |
| Tasks | `tasks.md` | in progress |
| Commits | `e993d30d`, `08fea8c9`, `78905c3c` | committed |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — docs/tooling, no release artifact | pending |
| Smoke test | `python3 scripts/validate-cu-api-matrix.py` + unittest | pending (post-push) |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Stale Spanish controller name is rejected | `scripts/tests/test_validate_cu_api_matrix.py` | passed |
| Current English controller names are accepted | same + repo CSV | passed |
| Missing required resource base fails validation | same | passed |
| All required resource bases present passes that check | same + repo CSV | passed |
| Status word in Bruno_Test is rejected | same | passed |
| Path and sentinel values are accepted | same | passed |
| MISSING without #953 is rejected | same | passed |
| Preflight list includes the matrix validator | same / preflight `--list` | passed |
| Unit tests prove stale fails and refreshed passes | unittest discover | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | `78905c3c` |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | `78905c3c` |
| `docs/300-development/303-testing/README.md` | yes | `78905c3c` |
| `CHANGELOG.md` | yes | `78905c3c` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1064; this change folder |
| 2 | Failing tests written, test cases designed | yes | unittest + validator on stale CSV before refresh |
| 3 | Suite green, coverage held, docs updated | yes | unittest discover + validate-cu-api-matrix.py |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **Branch naming**: Cloud Agent task requires
  `cursor/docs-1064-cu-api-matrix-refresh-69d3`; Constitution form recorded via
  Issue #1064 / CU76.
- **Bruno fills**: deliberately out of scope; gaps remain `#953`.
- **Playwright**: n/a — no UI surface.
- **local-ai/**: never used (Cloud VM only).
