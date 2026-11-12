> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Edit and delete already use aria-label; the receipt button was added with title only. The existing test finds the button by title, which keeps working because the title stays.

## Goals / Non-Goals

**Goals:** no icon-only button without an aria-label in the payments list.
**Non-Goals:** restyling or resizing the row actions.

## Decisions

1. Keep the title as the visible tooltip and add the aria-label (same translated key), so the existing getByTitle test is a PRESERVE test.

## Riesgos / Trade-offs

- [duplicate announcement of title and label] → browsers announce one name; both carry the same text.

## Testing Strategy

`payments-page-actions.test.tsx` (one new test, red first).

## Regression Strategy

Full Vitest suite with coverage floors.

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
