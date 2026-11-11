> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slice 3 (#2262) merged with the UI. TS-0046 passed against a real stack on 2026-11-11.

## Goals / Non-Goals

**Goals:** keep the permanent security mapping accurate.
**Non-Goals:** any behaviour change.

## Decisions

1. Documentation only; no code or contract changes.
2. The default-policy question is raised to the Owner on #1640, not decided here.

## Riesgos / Trade-offs

- [A doc claim outruns the code] → every claim names its test.

## Testing Strategy

Docs gates: validate-sdlc-plan, doc traceability check, markdown lint.

## Regression Strategy

workspace doc checks.

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
