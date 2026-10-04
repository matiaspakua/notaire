> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1222, Use Case CU76 – Quality Assurance and Testing Infrastructure. #1021 regenerated the ERD from the schema and found the dictionary stale; its descriptions were keyed by retired column names.

## Goals / Non-Goals

**Goals:** Dictionary equals schema; descriptions preserved; drift detected statically in CI without a database.
**Non-Goals:** Rewriting descriptions beyond mapping renamed columns and describing the new ones; the legacy `identificaciones` notes.

## Decisions

1. The guard compares the dictionary with the committed ERD CSV and PlantUML (themselves generated from the schema and guarded by test_erd_current_schema) so CI needs no database. Rejected: a CI PostgreSQL job just for docs.
2. Undocumented columns get a `TODO` marker that the guard rejects, so a migration forces a human description. Rejected: silent empty descriptions.

## Riesgos / Trade-offs

- Description mapping from retired names is done once by position and type and reviewed by hand; a wrong mapping would be a documentation error, not a runtime one.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Every schema table has a dictionary section and vice versa | static | `scripts/test_data_dictionary_sync.py` |
| Dictionary columns, PK, FK and nullability match the schema | static | `scripts/test_data_dictionary_sync.py` |
| The referential matrix equals the schema foreign keys | static | `scripts/test_data_dictionary_sync.py` |
| No description is left as TODO | static | `scripts/test_data_dictionary_sync.py` |

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
