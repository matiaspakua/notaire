# Fix icon-only buttons missing accessible names

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1057 |
| Use Case | **CU gap noted:** issue cites cross-cutting RNF accessibility (WCAG AA / `.claude/rules/ui-ux-design.md`). Closest existing CU: **CU76** – Quality Assurance and Testing Infrastructure (accessibility validation). No dedicated CU-XX for icon naming — Gate 1 proceeds on issue + CU76 / RNF reference; implement updates CU76 AC rather than inventing a new CU. |
| Branch | `cursor/fix-1057-a11y-icon-names-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement after #1148 merges |

## Objetivo

Seventeen icon-only dashboard `<Button>`s expose no reliable accessible name
(`aria-label` / `title` / sr-only text). Screen readers announce only “button”,
failing WCAG 2.1 SC 4.1.2 (Name, Role, Value). Same defect class as #940
(dialog close). Unblocks skipped Playwright CU21 edit flows that cannot use
`getByRole('button', { name })`.

## What Changes

- Add translated `aria-label` (via existing `tc("edit")` / `tc("delete")` /
  page-specific keys) on every inventoried icon-only action button.
- Prefer explicit `aria-label` over relying solely on `NotaireIcon` / `<img alt>`
  inside the button (six admin pages today).
- Enable / tighten a `jsx-a11y` lint rule so unnamed icon buttons fail CI.
- Add Playwright coverage that locates actions by accessible name
  (`getByRole('button', { name })`); unskip CU21 edit cases in TS-0016 where
  that was the blocker.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every interactive control MUST have an accessible name for assistive tech | WCAG 2.1 SC 4.1.2; `.claude/rules/ui-ux-design.md` Accessibility (WCAG AA) | Made explicit for icon-only buttons |
| Icon-only actions MUST expose a translated label (not empty / not icon glyph alone) | #1057 AC; CU76 accessibility validation | New (document under CU76 AC) |
| Regression of unnamed icon buttons MUST be prevented by lint | #1057 AC (jsx-a11y) | New tooling rule |

## Capabilities

### New Capabilities

- `icon-button-accessible-names`: Dashboard icon-only buttons expose translated
  accessible names; lint + E2E prevent regressions.

### Modified Capabilities

- (none under `openspec/specs/` today cover client icon-button naming)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | 9 dashboard pages, eslint config, unit/E2E tests, optional i18n keys |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing `frontend-ci` / lint / Playwright jobs cover this |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: `eslint-plugin-jsx-a11y` already transitive via
  `eslint-config-next`; may enable/stricter rule in `frontend/eslint.config.mjs`

### Architecture review

Follows existing design-system Button + i18n (`tc` / `t`) patterns used on
pages that already set `aria-label={tc("edit")}`. No ADR. No form layout
change beyond a11y attributes.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Add/clarify AC: icon-only controls must expose accessible names; cite #1057 |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Register new TS (propose **TS-0096**; confirm free at implement — TS-0095 claimed by #1054) |
| `.claude/rules/ui-ux-design.md` | Optional one-line under Accessibility: icon-only buttons need `aria-label` |
| `CHANGELOG.md` | `[Unreleased]` a11y entry for icon button labels |

## Out of Scope

- Full axe/WCAG audit of every page (other audit issues remain separate).
- Renaming / restyling icons or replacing Lucide with `NotaireIcon` globally.
- Non-`<Button>` icon controls (raw `<button>`, links, IconButtons outside
  the inventoried set) unless found as the same pattern during implement.
- Backend, Flyway, OpenAPI.
- Starting implement / PR before #1148 merges.
