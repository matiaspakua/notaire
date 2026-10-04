# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1021 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (documentation debt of epic #973) | exists |
| Related | #973 (rename epic) | referenced |
| Specification | `openspec/changes/docs-1021-regenerate-erd/` | Gate 1 draft |
| Branch | `docs/1021_regenerate_erd` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No artifact uses a retired Spanish table name | `scripts/test_erd_current_schema.py` | pending |
| Sources, SVG and CSV list the same entities | `scripts/test_erd_current_schema.py` | pending |
| Renamed tables appear under their new name | `scripts/test_erd_current_schema.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/205-data-model/ERD/*` | pending | — |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
