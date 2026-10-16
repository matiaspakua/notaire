> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after substitution #1898 merged (R2=9 on main @ 055de7d25).

## Goals / Non-Goals

**Goals:** Clear ~2 concept R2 edges without behaviour change.
**Non-Goals:** Folio/item/user/property R2; FE changes; API contract changes.

## Decisions

Move ConceptService beside OpsAdapter; inject ConceptRepository directly; delete unused entity RepositoryPort; update unit test mocks.

## Riesgos / Trade-offs

ConceptServiceTest mocks ConceptRepositoryPort — switch to ConceptRepository.

## Testing Strategy

ConceptServiceTest + HexagonArchitectureTest shrink.

## Regression Strategy

Existing concept Bruno/API and controller tests.

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
