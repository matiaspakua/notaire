# Text tokens reach WCAG AA contrast on every page

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1341 |
| Use Case | RNF-09 – Uso de colores en la GUI; CU76 |
| Branch | `fix/1341_text_contrast` |
| Gate 1 status | draft |

## Objetivo

axe `color-contrast` (serious) failed on 35 of 36 dashboard routes once the #1337 semantic classes rendered: DataTable headers (`neutral[600]` #86868B, 3.3:1), the sidebar subtitle and role (`--sidebar-muted`, 4.4:1), every red delete icon (`--destructive`, 4.2:1), report card descriptions, the workflow legend, the required-field asterisk (`error[500]`, 3.4:1), field error text (`error[600]`, 3.6:1) and the dashboard "Ver todo" link (brand blue on gray, 4.3:1).

## What Changes

- `tokens.ts`: `neutral[600]` becomes #6E6E73 (5.1:1 on white, 4.7:1 on `neutral[100]`); new `error[700]` #C81E1E for error text; `semantic.form.errorText` and the `FormField` asterisk use it; `neutral[500]` is documented as borders/disabled only.
- `globals.css`: `--sidebar-muted` 40%, `--destructive` 0 74% 45% (also fixes white text on destructive buttons), new `--primary-text` (registered as `text-primary-text`) for links and blue text on light grays; the brand `--primary` fill is unchanged (owner decision).
- `table.tsx` headers use `text-muted-foreground` instead of an inline gray; `link` and `apple-secondary` button variants use `text-primary-text`; three `text-neutral-300/400` texts and the inactive workflow badge (`neutral-500` on `neutral-100`) are replaced.
- Vitest `token-contrast.test.ts`: WCAG ratio of each text token on each light background, plus static scans for light-gray text utilities.
- Playwright `TS-0107`: every visible text element on /login and five dashboard routes reaches 4.5:1 (3:1 large) at 1440px and 390px.
- `.claude/rules/ui-ux-design.md` colour table states each text token's contrast; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Readable text reaches 4.5:1 (3:1 large) on its background | #1341, RNF-09, WCAG 1.4.3 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-accessibility`: Dashboard text and controls meet WCAG 2.1 AA.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | theme tokens, globals.css, table/button/breadcrumb, 3 pages |
| `testing` | yes | Playwright TS-0107 |
| `backend-api` | no |  |

### Surface area

- Routes: all /dashboard/** and /login (visual only)
- API: none

### Architecture review

No architecture change. Builds on #1337 (Tailwind v4 `@theme inline` mapping).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `.claude/rules/ui-ux-design.md` | Colour table with contrast figures |
