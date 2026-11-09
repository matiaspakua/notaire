> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#2170 validation; next-slice-wilson-multistage-honesty.md

## Goals / Non-Goals

**Goals:** Honest multi-stage Wilson coverage signal without inventing Isolator or L1 promotion.
**Non-Goals:** Isolator, ForgePort, portal, L1 auto-promotion, feeding pre-push into Notaire ledger (follow-on).

## Decisions

Expand autonomy allowlist to signal+pre-push so fixtures can prove two-stage honesty; count trials per stage for the ≥2 check.

## Riesgos / Trade-offs

Notaire production ledger still signal-only → detail becomes L1-not-evaluable (honest).

## Testing Strategy

AutonomyWilsonTest single-stage vs two-stage; report HTML still surfaces level 0.

## Regression Strategy

framework/ai-sdlc/tests + workspace aisdlc adoption if needed

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
