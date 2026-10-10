# Semantic Tailwind colour classes generate CSS again (#1337)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1337 |
| Use Case | RNF-05 – Aspecto visual; RNF-09 – Uso de colores en la GUI |
| Branch | `fix/1337_tailwind_theme_tokens` |
| Gate 1 status | draft |

## Objetivo

`globals.css` declares the shadcn HSL tokens in `:root`, but Tailwind v4 only generates colour utilities for colours registered in an `@theme` block. `bg-primary`, `text-primary-foreground`, `text-muted-foreground`, `text-destructive`, `bg-secondary`, `border-border`, `ring-ring` and the rest compiled to nothing: primary buttons rendered white, the active sidebar item had no pill, delete icons were black. Register the tokens so the existing classes work, and move the brand primary to #0071E3 (owner decision, 2026-10-09) so white text on it passes WCAG AA.

## What Changes

- `frontend/src/app/globals.css`: an `@theme inline` block maps the 19 shadcn tokens to `--color-*` (`hsl(var(--x))`), so opacity modifiers compile to `color-mix()`.
- `--primary`, `--ring` and `--sidebar-active` become `210.1 100% 44.5%` (#0071E3, 4.7:1 with white) instead of `211 100% 50%` (3.9:1).
- No component markup changes.
- Vitest `theme-css-tokens.test.ts`, Playwright `TS-0101-design-tokens-applied.spec.ts`, CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Brand primary is #0071E3 | Owner decision 2026-10-09 (#1336) | Changed |
| White text on primary reaches 4.5:1 | WCAG 1.4.3, #1337 | Made explicit (unit test) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-design-tokens`: Semantic colour utilities resolve to the design-system tokens.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | globals.css and tests |
| `testing` | yes | Playwright TS-0101 |
| `backend-api` | no | — |

### Surface area

- Routes: every page (shared button, badge, sidebar, icon colours)
- API / entities / configuration: none

### Architecture review

No architecture change: CSS configuration only. Unifying `globals.css` with `src/theme/tokens.ts` stays in #1365.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
