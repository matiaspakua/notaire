> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693. Pattern: FE protocol #1833.

## Goals / Non-Goals

**Goals:** Shrink four testimony eslint suppressions; keep REST behaviour identical.
**Non-Goals:** Do not migrate unrelated report hooks.

## Decisions

Separate testimony and testimony-movement cores; preserve TestimonyCreate/Update wire shapes including version; duck-type 404/409 on UI.

## Riesgos / Trade-offs

Optimistic lock version must round-trip on PUT; create requires deed.idDeed+number.

## Testing Strategy

Vitest use-cases + gateways + architecture-boundaries.

## Regression Strategy

Generate/register/verify/update/delete; enter-registration/withdraw/reenter; copy PDF.

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
