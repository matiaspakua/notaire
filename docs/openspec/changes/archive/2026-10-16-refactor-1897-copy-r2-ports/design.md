> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after history #1892 merged (R2=17 on main @ 17d041872).

## Goals / Non-Goals

**Goals:** Clear ~4 copy R2 edges without behaviour change.
**Non-Goals:** Substitution/concept R2; FE changes; API contract changes.

## Decisions

Move CopyService beside OpsAdapter; inject CopyRepository directly; delete unused entity RepositoryPort.

## Riesgos / Trade-offs

Package move breaks imports on CopyOpsAdapter.

## Testing Strategy

CopyControllerTest + HexagonArchitectureTest shrink.

## Regression Strategy

Existing copy Bruno/API and controller tests.

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
