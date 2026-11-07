> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Owner wants >90% coverage; gates are raise-only floors below that.

## Goals / Non-Goals

**Goals:** ['Document target vs floor', 'Ratchet JaCoCo 85/70', 'Fleet self-improvement lessons']
**Non-Goals:** ['90% Vitest/Bruno/Playwright floors in this PR']

## Decisions

['Target is policy; floors are enforceable ratchets']

## Riesgos / Trade-offs

['85/70 still leaves ~5pp to 90% line target']

## Testing Strategy

['mvn jacoco:check passes at new floors']

## Regression Strategy

['Existing verify suite']

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Next release.

## Rollback Strategy

Revert PR (floor raise-only: do not lower after merge without ADR).

## Migration Plan

Child tips under #2046.

## Open Questions

None.
