> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after copy #1897 merged (R2=13 on main @ bc6f92e8b).

## Goals / Non-Goals

**Goals:** Clear ~4 substitution R2 edges without behaviour change.
**Non-Goals:** Concept R2; FE changes; API contract changes.

## Decisions

Move SubstitutionService beside OpsAdapter; inject SubstitutionRepository directly; delete unused entity RepositoryPort. Update docs path test for the moved service.

## Riesgos / Trade-offs

docs/tests/test_substituted_parameter_names_english.py hardcodes the old path.

## Testing Strategy

SubstitutionServiceIntegrationTest + HexagonArchitectureTest shrink; docs path test.

## Regression Strategy

Existing substitution Bruno/API and integration tests.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
