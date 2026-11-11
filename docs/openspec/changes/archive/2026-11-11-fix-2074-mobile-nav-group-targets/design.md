> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`AppSidebar` shares one `body(onNavigate, animateActive, idPrefix)` for the
desktop `<aside>` (`idPrefix=desktop`) and the Radix mobile dialog sheet
(`idPrefix=mobile`). Group disclosure buttons currently use `min-h-8`
everywhere. Issue #2074 targets only the mobile sheet.

## Goals / Non-Goals

**Goals:**
- Mobile sheet group toggles ≥44px tall.
- Desktop density stays compact (`min-h-8`) without visual noise.
- Grouped-sidebar behaviour and TS-0121 remain green.

**Non-Goals:**
- Reordering or renaming nav groups.
- Changing nav link row heights or the close button (already 36px).
- Applying `pointer-coarse` media queries as the sole signal (viewport sheet ≠
  pointer type; narrow desktop windows still use the sheet).

## Decisions

Branch the min-height on `idPrefix`: `mobile` → `min-h-11` (44px), `desktop` →
`min-h-8` (32px). Prefer this over `pointer-coarse:` alone because the sheet is
shown by the `md` breakpoint, not by pointer type.

Alternative considered: always `min-h-11` — rejected because acceptance allows
(and prefers) compact desktop density.

## Riesgos / Trade-offs

[Risk] Taller group headers slightly reduce visible items in the mobile sheet →
acceptable; WCAG/product size wins over a few pixels of scroll.
[Risk] Dual class paths drift → Vitest asserts both prefixes' min-height classes.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Mobile sheet group toggle ≥44px | unit | `app-sidebar.test.tsx` |
| Desktop group toggle stays compact | unit | `app-sidebar.test.tsx` |
| Grouped sidebar still works | E2E | `TS-0121-grouped-sidebar.spec.ts` |

- New unit tests: height-class assertions on mobile vs desktop toggles
- New integration tests: n/a
- Coverage impact: frontend Vitest only; JaCoCo unchanged

## Regression Strategy

- Existing tests affected: `app-sidebar.test.tsx` (add cases); `TS-0121` (must stay green)
- Full suite command: `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: n/a (no API surface)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update: none new; run `TS-0121-grouped-sidebar.spec.ts`
- Golden path covered: admin sections, collapse persist, dashboard grid, non-admin
- Edge / error paths covered: n/a for this hit-area fix
- Viewports: TS-0121 at 1440px; mobile height covered by Vitest on the sheet DOM
- Command: `cd testing/e2e && npx playwright test tests/TS-0121-grouped-sidebar.spec.ts`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend image only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open mobile menu, expand/collapse a group

## Rollback Strategy

- Revert safe: yes (CSS/class-only)
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: touch comfort only

## Migration Plan

None beyond the class change and tests.
