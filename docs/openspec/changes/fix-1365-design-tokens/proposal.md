# One design-token source: semantic status colours, no raw palette, no half-built dark mode

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1365 |
| Use Case | RNF-05 – Aspecto visual; RNF-09 – Uso de colores en la GUI; CU76 |
| Branch | `fix/1365_design_tokens` |
| Gate 1 status | draft |

## Objetivo

The brand primary is already `#0071E3` in `globals.css` (#1337), but colour still came from three places: the `:root` variables, `theme/tokens.ts`, and 54 raw Tailwind palette utilities in 13 files (14 different module-tile gradients on the dashboard, Badge success/warning/info, amber notices, the workflow editor's legend and errors). A partial `.dark` block existed with no toggle. Owner decision 2026-10-09: brand primary `#0071E3`, remove the half-built dark mode for now.

## What Changes

- `globals.css`: new `--success`, `--warning`, `--info` (+ `-foreground`) in `:root`, mapped in `@theme inline`; the partial `.dark` block is deleted.
- `theme/tokens.ts`: `BRAND_PRIMARY` is the only place the brand hex is written; `success/warning/info[700]` mirror the new CSS variables (typed hex mirror for SVG, canvas and inline styles).
- Every raw palette utility in `app/` and `components/` replaced by semantic ones (`bg-success/10 text-success`, `bg-warning/10 text-warning`, `bg-info/10 text-info`, `text-destructive`, `border-border`, `border-input`, `bg-muted`); the dashboard module tiles use one `bg-primary/10 text-primary` style; `ring-primary-300` (no CSS generated) becomes `ring-ring/40`.
- Guard `tests/unit/design-tokens.test.ts`: no `.dark` block or `dark:` utilities; status tokens declared and mapped; tokens.ts mirrors the CSS values; the brand hex is written once; no raw palette utilities, no numeric scale on semantic tokens, no `[#hex]` in `app/`, `components/`, `lib/`, `hooks/`; status colours pass WCAG AA as text on white, on their /10 tint and under white text.
- Playwright TS-0119; CHANGELOG entry; `theme/README.md`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Components use semantic colour utilities only | #1365 | New |
| No dark mode until it is designed and tracked separately | Owner decision 2026-10-09 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `frontend-design-system-hex-hygiene`: design-system colour hygiene.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | globals.css, tokens.ts, 13 pages/components, guard test |
| `testing` | yes | Playwright TS-0119 |

### Surface area

- /dashboard, /dashboard/pagos, /dashboard/documentos, /dashboard/administracion/{tramites,estados-gestion,workflows}, /login, sidebar, Badge

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `frontend/src/theme/README.md` | Semantic status tokens, guard, no dark mode |
