> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Spanish was applied once, inside DocShell's article, by text node; attributes, the home and About pages and late content were missed.

## Goals / Non-Goals

**Goals:** Current numbers, Spanish everywhere, guards that keep both true.
**Non-Goals:** Redesign.

## Decisions

One MutationObserver on #main instead of per-layout walkers. Keys stay exact English text nodes. Numbers in live text are built in both languages where rendered. Spanish follows the site's conventions (voseo, technical anglicisms, 'solo' without accent).

## Riesgos / Trade-offs

Pairs keyed on text with a live number fall back to English when it changes; the Spanish smoke test fails then.

## Testing Strategy

Red first: factory-line counts, Mermaid semicolons, DOM localisation, generator floors and tools, roadmap snapshot mode; Spanish coverage smoke across 43 routes.

## Regression Strategy

Pages Vitest 140, Playwright smoke 190 passed, workspace generator tests.

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
