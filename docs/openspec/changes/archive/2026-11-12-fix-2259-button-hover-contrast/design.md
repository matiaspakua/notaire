> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Button variants are class strings built with cva; colours come from CSS variables in globals.css. `primary-text` (hsl 210 100% 38.4%) already exists as the accessible blue for text on light grays and is a Tailwind colour utility (`bg-primary-text`).

## Goals / Non-Goals

**Goals:** no hovered primary button below AA contrast.
**Non-Goals:** changing the rest-state colours or the owner brand decision on primary[600].

## Decisions

1. Reuse the existing `primary-text` token for hover: darker than rest, no new colour in the palette, contrast above 4.5:1.
2. The test reads the real class strings and CSS variables, so a future opacity change fails it.

## Riesgos / Trade-offs

- [Hover looks darker than before] → conventional; the owner's rest colour (primary[600]) is unchanged.

## Testing Strategy

`button-hover-contrast.test.ts` (3 tests; default and apple red first) plus the existing button and token-contrast tests.

## Regression Strategy

Full Vitest suite with coverage floors; Chromium hover measurement.

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
