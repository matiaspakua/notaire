> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`frontend/src/app/dashboard/page.tsx` renders `DASHBOARD_MODULES` as identical
cards: `bg-primary/10 text-primary` icon wells and `group-hover:scale-110`.
`ExpirationsAlertCard` already provides a status accent above the grid. Root
`DESIGN.md` states the One Seal Rule: SF Blue is not a tint for every icon well;
mist (`#F5F5F7` / `colors.neutral[100]` / `bg-secondary`) is for inset wells.

## Goals / Non-Goals

**Goals:**
- Remove decorative icon scale from module cards.
- Limit SF Blue module wells to at most one; prefer mist for the rest (or all,
  when the expirations alert is the accent).
- Keep `rounded-[28px]` cards, typography, `data-testid="dashboard-modules"`,
  and keyboard focus on the module `Link`s.

**Non-Goals:**
- Removing the grid.
- Changing sidebar navigation.
- Metric-card hierarchy (called out in the issue narrative; not in acceptance).

## Decisions

1. **All module wells mist** — because `ExpirationsAlertCard` already exists as
   the urgent accent, no module well keeps `bg-primary/10 text-primary`. Use
   `bg-secondary text-muted-foreground` (mist fill + secondary ink).
2. **Drop `scale-110` and its transform transition** — no decorative hover scale
   on the icon well.
3. **Drop the left primary hover strip on every card** — it applied SF Blue to
   every module on hover; remove so mist cards do not all flash the seal.
4. **Keep link title hover `text-primary`** — links may use the seal per DESIGN.md.
5. **Static source + render Vitest** — assert class contracts on `page.tsx` and
   that module links remain in the document / focusable.

## Riesgos / Trade-offs

- [Risk] Cards look flatter without primary wells → Mitigation: intentional
  Operate hierarchy; expirations alert carries urgency.
- [Risk] Existing visual snapshots (if any) differ → Mitigation: no Playwright
  screenshot golden files for this grid; unit locks classes.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Module icon wells have no scale-110 | unit | `dashboard-home-module-grid.test.tsx` |
| At most one SF Blue module well (zero when alert is accent) | unit | `dashboard-home-module-grid.test.tsx` |
| Mist wells on non-accent modules | unit | `dashboard-home-module-grid.test.tsx` |
| Module links remain keyboard-reachable | unit | `dashboard-home-module-grid.test.tsx` |

- New unit tests: frontend Vitest
- New integration tests: n/a
- Coverage impact: frontend Vitest; JaCoCo unchanged

## Regression Strategy

- Existing tests affected: `dashboard-pages-render.test.tsx`, hex-hygiene,
  dashboard-nav (routing unchanged)
- Full suite command: `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: n/a (frontend-only)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update: `testing/e2e/tests/TS-0119-design-tokens.spec.ts` —
  module wells assert one shared mist tint (not brand primary ink)
- Golden path: home module grid without icon scale / with mist wells
- Edge / error paths: access-denied banner and stats error unchanged
- Viewports: existing responsive grid classes unchanged
- Command: `cd testing/e2e && npx playwright test` via preflight `--full`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open `/dashboard` and tab through module links

## Rollback Strategy

- Revert safe: yes — presentational only
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: home grid slightly flatter

## Migration Plan

1. Fail Vitest on scale-110 / primary well count / mist contract.
2. Update `page.tsx` module card classes.
3. Document in FRONTEND-DESIGN-SYSTEM + CHANGELOG.
4. Green tests + preflight `--full`.

## Open Questions

(none)
