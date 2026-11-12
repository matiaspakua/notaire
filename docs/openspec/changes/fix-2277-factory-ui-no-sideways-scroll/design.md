> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The shells are static HTML with inline CSS served by the kit; there is no build step. The overflow was reproduced in a real browser and the one-line fix verified by style injection.

## Goals / Non-Goals

**Goals:** no sideways page scroll on any factory page at 360 px.
**Non-Goals:** visual redesign.

## Decisions

1. Fix `main` rather than only the media query column (`1fr` -> `minmax(0, 1fr)`): it covers every width and both shells.
2. A rendered-HTML contract test is the stdlib-only guard; the browser measurement is recorded as evidence.

## Riesgos / Trade-offs

- [CSS-text test is weaker than a layout test] → the browser measurement is in this change; the optional kit UI job can add a layout test later.

## Testing Strategy

`test_backoffice_html` and `test_runtime_html` (one test each, red before the fix).

## Regression Strategy

`env -u TRACEPARENT bash framework/ai-sdlc/verify.sh` (all kit tests).

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
