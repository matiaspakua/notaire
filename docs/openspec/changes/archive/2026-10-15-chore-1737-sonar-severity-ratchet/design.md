> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#1680 FE new-code gate fixed; #1717 cleared some #1535 blockers. Need explicit severity ratchet (#1737).

## Goals / Non-Goals

**Goals:** CHANGELOG-free QG ratchet + report helper; no hex/jpa edits.
**Non-Goals:** Legacy count wipe; Mac stack smoke (#1740).

## Decisions

1. Ratchet new_* severity metrics on the existing Notaire way gate (idempotent create/update).
2. Leave overall legacy classification on #1535.

## Riesgos / Trade-offs

If Sonar Community metric keys differ, gate create may no-op for unknown metrics — script should tolerate and log.

## Testing Strategy

Failing infra unit guards first; then implement; no Playwright.

## Regression Strategy

Existing test_sonar_analysis SCM/mount guards stay green.

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
