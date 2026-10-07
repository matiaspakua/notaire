> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1057 (audit-2026-09): **17 of ~57** icon-only `<Button>`s lack a
reliable accessible name. Verified inventory (2026-10-03, brace-aware scan;
strict = no `aria-label` / `title` / sr-only on the Button):

| # | File | Action | Gap |
|---|------|--------|-----|
| 1–2 | `administracion/usuarios/page.tsx` ~103–104 | Pencil / Trash2 | none |
| 3–4 | `administracion/roles/page.tsx` ~90–91 | Pencil / Trash2 | none |
| 5–6 | `escrituras/page.tsx` ~121–122 | Pencil / Trash2 | none |
| 7–8 | `pagos/page.tsx` ~109–110 | Pencil / Trash2 | none |
| 9–10 | `personas/page.tsx` ~174–183 | Pencil / Trash2 | none |
| 11 | `presupuestos/page.tsx` ~192–199 | Receipt (resumen) | none |
| 12–13 | `administracion/conceptos/page.tsx` ~105–106 | edit/delete NotaireIcon | img `alt` only |
| 14–15 | `administracion/documentos/page.tsx` ~112–117 | edit/delete NotaireIcon | img `alt` only |
| 16–17 | `administracion/tramites/page.tsx` ~112–117 | edit/delete NotaireIcon | img `alt` only |

Reference fixed pattern: `items/page.tsx`, `gestiones/page.tsx`,
`presupuestos` edit/delete already use `aria-label={tc("edit"|"delete")}`.
`pagos` “emitir recibo” already has `title=` — keep or promote to `aria-label`.
Related: #940 (closed), TS-0016 CU21 tests skipped for missing edit name,
TS-0042 covers search labels (#608) not icon buttons.
`eslint.config.mjs` extends `eslint-config-next` (jsx-a11y present) but does
not enforce icon-button naming for this pattern today.

## Goals / Non-Goals

**Goals:**

- All 17 inventoried buttons expose a translated accessible name via
  `aria-label` (preferred) so `getByRole('button', { name })` works.
- Lint rule fails CI when new icon-only Buttons omit a name.
- Playwright proves naming on representative pages; unskip CU21 edit where
  blocked only by this bug.

**Non-Goals:**

- Broader a11y (contrast, focus rings, live regions) beyond icon names.
- Changing Button variants/sizes or visual design tokens.
- Backend or API contract changes.
- Implementing before #1148 is on `main`.

## Decisions

1. **Explicit `aria-label` on every inventoried Button (not title-only, not img-alt-only)**
   - Why: #1057 AC requires translated `aria-label`; Playwright and SR tooling
     are most reliable with `aria-label`. Img `alt` inside `NotaireIcon` can
     contribute to the name but is easy to break if icons become decorative /
     `aria-hidden`.
   - Alternative rejected: keep img-alt-only for the 6 admin rows — fails AC
     wording and leaves inconsistent patterns.

2. **Reuse common i18n keys `tc("edit")` / `tc("delete")`; add page key for Receipt**
   - Why: `messages/es.json` / `en.json` already have common edit/delete.
   - Receipt resumen needs a dedicated translated string (e.g. presupuestos
     `resumen` / summary) — do not hardcode Spanish in JSX (design-system /
     i18n rules).
   - Alternative rejected: hardcoded `"Editar"` strings (already present on
     some pages like suplencias) — prefer `tc()` for consistency.

3. **Enable jsx-a11y rule at error for this class of bug**
   - Prefer `jsx-a11y/control-has-associated-label` and/or ensure Next’s
     recommended a11y set flags unlabeled buttons; tune for icon children.
   - Alternative rejected: custom ESLint rule or Codemod-only without lint —
     AC asks for jsx-a11y prevention.

4. **Focused E2E TS-0096 (confirm free id) + unskip TS-0016 CU21 edit**
   - Mirror TS-0042 style: visit pages, assert
     `getByRole('button', { name: /editar|eliminar|…/i })`.
   - Cover at least usuarios (issue exemplar) + one img-alt-only admin page +
     presupuestos resumen; full 17-page matrix optional if lint covers the rest.

## Riesgos / Trade-offs

- **[Risk] Count drifts from 17** → Re-run inventory at implement; fix all
  strict-missing icon-only Buttons found, not only the table snapshot.
- **[Risk] jsx-a11y noisy false positives** → Scope rule to `src/app` /
  document overrides; fix real gaps rather than disabling globally.
- **[Risk] i18n locale mismatch in Playwright** → Assert against ES default
  (or use message keys via test helpers) consistent with TS-0042.
- **[Risk] Unskipping TS-0016 exposes unrelated CU21 bugs** → Unskip only
  tests blocked solely by accessible name; leave others skipped with reason.
- **[Trade-off] img-alt-only already “works” for some AT** → Still add
  `aria-label` for AC + consistency; keep meaningful `alt` or set decorative
  - aria-label — pick one clear naming source (prefer aria-label on Button).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Icon-only edit has accessible name | E2E | `TS-0096-icon-button-accessible-names.spec.ts` (id confirm) |
| Icon-only delete has accessible name | E2E | same |
| Presupuesto resumen has accessible name | E2E | same |
| Admin NotaireIcon row actions named | E2E | same (conceptos or tramites) |
| Lint rejects unnamed icon button | unit / lint | eslint fixture or documented `npm run lint` gate |
| CU21 edit reachable by role name | E2E | unskip TS-0016 CU21-GW01 (and GW02 if still valid) |

- New unit tests: optional RTL test that a sample row action Button has
  `aria-label` — prefer lint + E2E if unit adds little.
- New integration tests: n/a (no backend).
- Coverage impact (JaCoCo): n/a — frontend-only.

## Regression Strategy

- Existing tests affected: TS-0016 (unskip); any test using fragile
  `locator('button').nth(n)` for edit — prefer role+name after fix.
- Full suite command: `cd frontend && npm test` / `npm run lint`; backend
  `mvn verify -pl backend-api` only as sanity (no Java delta expected).
- HTTP/Bruno API suite: n/a — no API change.
- Legacy paths: none (`frontend-swing` removed).

## Playwright Strategy

- Specs to add/update:
  - Add `frontend/tests/e2e/TS-0096-icon-button-accessible-names.spec.ts`
    (confirm TS id free vs #1054’s TS-0095).
  - Update `TS-0016-usuarios-escribanos-workflow.spec.ts` — unskip CU21 edit
    tests blocked by missing name.
  - Update `E2E-TEST-MAPPING.md`.
- Golden path: admin login → usuarios / personas / presupuestos →
  `getByRole('button', { name: /editar|eliminar|resumen|…/i })` visible.
- Edge: locale EN if language switcher covered; at least ES default.
- Viewports: assert controls still visible at 320 / 768 / 1024 (names are
  a11y attrs — visual unchanged).
- Command: `cd frontend && npx playwright test TS-0096 TS-0016`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only image/deploy; no schema coupling
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open Usuarios, confirm edit/delete
  announce with name (SR or Playwright smoke)

## Rollback Strategy

- Revert safe: yes — attribute + lint + test only; no data migration
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: low (labels only)

## Migration Plan

1. Copy this draft into `openspec/changes/fix-1057-a11y-icon-names/`.
2. Validate with `bash scripts/validate-sdlc-plan.sh fix-1057-a11y-icon-names`.
3. TDD: failing E2E/lint → add aria-labels → green → lint rule on.
4. Docs + CHANGELOG → PR with `Closes #1057`.

## Open Questions

- Exact jsx-a11y rule id / options that catch Lucide-only children without
  false-positive flood — resolve during implement by running lint on a
  known-bad fixture.
- Whether to set `NotaireIcon` `alt=""` + `aria-hidden` when parent Button
  owns `aria-label` (avoid duplicate announcements) — prefer yes if dual
  naming is observed in SR testing.
