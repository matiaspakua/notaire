> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1021, Use Case CU76 – Quality Assurance and Testing Infrastructure (documentation debt of epic #973). The ERD artifacts reference the pre-#973 Spanish names and no longer match the schema.

## Goals / Non-Goals

**Goals:** ERD sources, renders and CSV match the migrated schema; a guard keeps them honest.
**Non-Goals:** Rewriting the data dictionary (drift is reported in the issue); changing the schema.

## Decisions

1. Generate from the migrated database rather than editing by hand: the schema is the source of truth (P6) and 36 tables are too many to retype.
2. The guard is database-free: retired names come from the `RENAME TO` statements of the migrations, and the three artifacts must list the same entities. Rejected: a guard that needs PostgreSQL, because CI jobs that run the wrappers have none.

## Riesgos / Trade-offs

- The rendered SVG depends on the PlantUML and Graphviz versions; it is regenerated, not diffed line by line.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No artifact uses a retired Spanish table name | static | `scripts/test_erd_current_schema.py` |
| Sources, SVG and CSV list the same entities | static | `scripts/test_erd_current_schema.py` |
| Renamed tables appear under their new name | static | `scripts/test_erd_current_schema.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none
- Full suite command: `bash scripts/preflight.sh`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit

## Rollback Strategy

- Revert the PR.
