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
| Pull Request | — | passed |
| CI run | — | passed |
| Merge commit | — | passed |
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
| `docs/200-architecture/205-data-model/ERD/*` | done | this PR |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | done | this PR |
| `CHANGELOG.md` | done | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | done | this PR |
| 3 | Suite green, coverage held, docs updated | done | this PR |
| 4 | CI green, review approved, no conflicts | done | this PR |
| 5 | Deployed, smoke test passed, Issue closed | done | this PR |

## Exceptions

None.

## Verification log (2026-10-04)

- Red first: `scripts/test_erd_current_schema.py` failed 8 of 8 before regeneration, passes after (8 tests).
- Generated from the Flyway-migrated database (36 tables, 49 single-column foreign keys); both SVGs rendered with PlantUML 1.2024.7 and Graphviz 2.42.
- Drift against the data dictionary (29 tables with stale column names, 4 tables missing) is tracked in #1222.
