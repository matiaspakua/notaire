> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1241, Use Case CU72. `update` calls `toEntity(request)`, which builds a fresh `SubmittedDocument` with defaults for everything the request lacks, and saves it under the stored id.

## Goals / Non-Goals

**Goals:** a partial update that preserves stored fields.
**Non-Goals:** moving the mapping out of the controller or returning records (#577 slices).

## Decisions

1. `toEntity` becomes `apply(entity, request)`: it writes a field only when the request carries it. `create` calls it on a new entity pre-set with the old defaults, `update` on the stored one. Rejected: a separate update mapper (duplicates the due-date rule).
2. The due fields are recomputed only when the type or the date is in the request, so a name or flag edit never touches them.

## Riesgos / Trade-offs

- A caller can no longer clear a field by omitting it; no caller in the repository does, and clearing a text field remains possible by sending an empty string.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Delivered flag update keeps the rest | integration | `SubmittedDocumentPartialUpdateIntegrationTest` |
| Name update keeps the flags | integration | `SubmittedDocumentPartialUpdateIntegrationTest` |
| Unknown document | integration | `SubmittedDocumentPartialUpdateIntegrationTest` |
| Date change recomputes the due date | integration | `SubmittedDocumentPartialUpdateIntegrationTest` |

- Existing tests: `SubmittedDocumentControllerTest` must stay green unchanged
- Coverage impact: neutral to positive

## Regression Strategy

- Full suite command: `mvn verify -pl backend-api; bash scripts/run_pipeline.sh`
- Bruno `submitted-documents` and the Documentos Playwright spec must stay green

## Playwright Strategy

No UI change; the existing Documentos specs (TS-0099) run as regression evidence.

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): CD green; the update test passes in CI

## Rollback Strategy

- Revert the PR.
