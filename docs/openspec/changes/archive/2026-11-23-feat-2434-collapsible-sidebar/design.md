> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

App.tsx Sidebar renders brand, nav and footer toggles; CSS width var --sidebar-width.

## Goals / Non-Goals

**Goals:** Give the diagram the width; keep navigation usable and accessible collapsed.
**Non-Goals:** Overlay drawer on mobile.

## Decisions

Data attribute `data-collapsed` on the shell drives CSS; labels become visually hidden, not removed, so names stay; title attribute gives the tooltip. Preference key `fd.sidebar` in localStorage (per-viewer convenience).

## Riesgos / Trade-offs

Low: presentation only.

## Testing Strategy

Playwright e2e (collapse, persistence, keyboard, axe light/dark, 375px).

## Regression Strategy

Existing nav e2e stays green.

## Playwright Strategy

framework/ai-sdlc/factory-dashboard/e2e/sidebar-collapse.spec.ts

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
