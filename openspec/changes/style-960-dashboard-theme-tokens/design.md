> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #960 (FRONTEND, tech-debt, priority:low). Verified on updated `origin/main`:
hex `#RRGGBB` under `app/` + `components/` (excluding `theme/` and issue comments)
appears only in:

| File | Hex values |
|------|------------|
| `frontend/src/app/dashboard/layout.tsx` | `#F5F5F7` |
| `frontend/src/app/dashboard/page.tsx` | `#1d1d1f`, `#86868b`, `#424245`, `#ff3b30`, `#0071e3` |
| `frontend/src/components/ui/table.tsx` | `#F5F5F7`, `#86868b`, `#1d1d1f` (with `/60` `/40` opacity) |

Token map lives in `frontend/src/theme/tokens.ts` (neutral/primary/error scales).

## Goals / Non-Goals

**Goals:**

- Remove `#RRGGBB` from the three files using theme tokens or semantic Tailwind.
- Guard with a Vitest hex-hygiene test (TDD: red on tip, then green).
- Preserve table opacity intent; keep visual parity (no redesign).

**Non-Goals:**

- Expanding cleanup beyond the audited surfaces.
- Changing palette values in `tokens.ts`.
- Redesigning dashboard IA or table structure.

## Decisions

1. **Prefer semantic Tailwind** where already used elsewhere (`text-foreground`,
   `text-muted-foreground`, `text-primary`, `text-destructive`, `bg-background` /
   secondary-equivalent) for page text/links — matches dashboard pages such as
   auditoria/items.
2. **Prefer `import { theme } from "@/theme/tokens"` + style props** when exact
   token match or opacity math is needed (layout background; table header/hover
   via `color-mix(in srgb, ${theme.colors.neutral[100]} NN%, transparent)` or
   equivalent opacity utility).
3. **`#424245` → nearest `theme.colors.neutral[800]` (`#3A3A3C`)** — accepted
   slight shift; do not invent a new hex.
4. **Hex hygiene Vitest** scans `frontend/src/app/**` and
   `frontend/src/components/**`, excludes `theme/`, matches `#RRGGBB` only
   (6 digits) so issue refs like `#960` are ignored.
5. **English-only** on touched code: translate non-i18n Spanish strings/comments
   in edited files (e.g. sidebar toggle `aria-label`) to English; leave
   `messages/es.json` alone.

## Riesgos / Trade-offs

- [Semantic HSL vs exact Apple hex] → slight shade drift — Mitigation: prefer
  `theme.colors.*` style props for backgrounds/opacity-critical surfaces.
- [Playwright contention] → style-only diff; serialize awareness; keep smoke
  light (viewport evidence via computerUse or cheap Playwright if stack up).
- [False positives in hygiene] → Mitigation: 6-digit only; exclude `theme/`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Dashboard layout no hex | unit | `frontend/src/tests/unit/hex-hygiene.test.ts` |
| Dashboard home no hex | unit | same |
| Table no hex + opacity via tokens | unit | same (source scan) |
| Audited globs hex-free | unit | same |
| Visual parity 320/768/1024 | manual / Playwright smoke | computerUse or targeted e2e note |

- New unit tests: `hex-hygiene.test.ts`
- New integration tests: n/a
- Coverage impact: frontend Vitest only; JaCoCo n/a

## Regression Strategy

- Existing tests affected: none expected (style-only). Confirm
  `dashboard-nav.test.ts`, `data-table.test.ts`, page unit tests still pass.
- Full suite command: `cd frontend && npm test && npm run lint && npm run typecheck`
- Backend: unchanged — skip heavy Maven unless preflight requires.
- HTTP/Bruno: n/a
- Legacy paths: do not recreate Swing

## Playwright Strategy

- Style-only UI change; full suite may run in heavy CI — keep diff style-only.
- Prefer cheap smoke or computerUse screenshots at 320 / 768 / 1024 on
  `/dashboard` (auth required). Document evidence if full Playwright not cheap
  in this VM.
- Viewports: 320px / 768px / 1024px
- Command (if run): `cd frontend && npx playwright test` (targeted smoke only
  when stack available)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: standard frontend CD after merge
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open dashboard home; confirm colors and
  table hover still look correct

## Rollback Strategy

- Revert safe: yes — pure presentation; revert PR restores prior classNames
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: cosmetic only

## Migration Plan

1. Gate 1 artifacts + validate-sdlc-plan
2. Write failing hex-hygiene test; observe red
3. Replace hex in three files; observe green
4. CHANGELOG (+ optional design-system pointer)
5. Viewport evidence; commit; push; draft PR
