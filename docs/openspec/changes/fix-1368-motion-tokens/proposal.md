# One motion token system shared by CSS and motion/react; no exit wait on route change

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1368 |
| Use Case | RNF-05 – Aspecto visual; RNF-03 – Tiempo de respuesta; CU76 |
| Branch | `fix/1368_motion_tokens` (stacked on `fix/1365_design_tokens`) |
| Gate 1 status | draft |

## Objetivo

Motion lived in three disconnected places (unused `tokens.ts` `transitions`, `components/motion` at 0.4s enter / 0.2s exit, and 22 ad-hoc `duration-NNN` / `transition-all` classes plus `.apple-button { transition: all }` with a hover scale). Dialogs, selects, buttons and pages moved at different speeds, `transition: all` animated layout properties, and every route change waited for the old page's exit animation before the new one faded in.

## What Changes

- `globals.css`: `--motion-duration-{instant,fast,base,slow,exit,page}`, `--motion-ease-{standard,emphasized,exit}`, `--motion-distance-sm` in `:root`; `ease-*` mapped in `@theme`; `duration-fast|base|slow|exit|page` utilities (also drive tw-animate-css). `.apple-button` transitions background-color, box-shadow and transform only, with no hover scale (press stays `scale(0.98)`).
- `theme/motion.ts`: TS mirror for motion/react; `tokens.ts` `transitions` derive from it; `theme/index.ts` style helpers no longer transition `all`.
- `components/motion`: page fade-up 160ms / 4px with no exit; items fade up 4px at 180ms; `staggerDelay` caps the stagger at 6 items x 30ms; the dashboard layout drops `AnimatePresence mode="wait"`.
- Dialog and AlertDialog: enter `duration-slow ease-emphasized`, leave `duration-exit ease-exit`. All other ad-hoc durations replaced by tokens.
- Guard `tests/unit/motion-tokens.test.ts`; Playwright TS-0120; CHANGELOG; `theme/README.md` § Motion.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Durations and easings come from the motion tokens only | #1368 | New |
| Route changes never wait for an exit animation | #1368 | New |

## Capabilities

### New Capabilities

- `frontend-motion-tokens`: shared motion timing for the frontend.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | globals.css, theme/motion.ts, components/motion, UI primitives, dashboard layout |
| `testing` | yes | Playwright TS-0120 |

### Surface area

- Every dashboard route (page transition), dialogs, selects, inputs, buttons, data tables, login

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `frontend/src/theme/README.md` | Motion section |
