# Dialogs animate in and out: tw-animate-css backs the shadcn motion classes

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1349 |
| Use Case | RNF-05 – Aspecto visual; RNF-06 – Diseño de ventanas |
| Branch | `fix/1349_dialog_motion` |
| Gate 1 status | draft |

## Objetivo

`animate-in`, `fade-in`, `zoom-in-95` and `data-[state=open]:animate-in` come from tw-animate-css, which was neither installed nor imported, so the compiled CSS had no rule for them: dialogs and confirm dialogs popped in and vanished mid-click with no transition, and the table and login fades were dead.

## What Changes

- devDependency `tw-animate-css@^1.4.0` (pure CSS, MIT); `@import "tw-animate-css";` in `globals.css` (option A of the issue). The lockfile gains only that entry.
- `ui/dialog.tsx` and `ui/alert-dialog.tsx`: the overlay fades (200ms in, 150ms out); the content fades and scales 0.95→1 on enter (200ms ease-out) and reverses on exit (150ms ease-in) through Radix `data-state`, so Radix keeps the content mounted until the exit animation ends. The alert-dialog overlay had no motion at all.
- `DataTable` and the login card keep a short fade (`fade-in-0`, 300/500ms), now real.
- Reduced motion: the existing `prefers-reduced-motion` rule in globals.css shortens every animation to 0.01ms, so motion-sensitive users get none.
- Playwright `TS-0117`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Motion classes in the code have CSS behind them | #1349, RNF-05 | Made explicit |
| Reduced motion removes perceptible animation | WCAG 2.3.3 | Kept |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-motion`: Enter/exit motion of overlays.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | package.json, globals.css, dialog, alert-dialog, DataTable, login |
| `testing` | yes | Playwright TS-0117 |

### Surface area

- Every dialog and confirm dialog
- Routes: /login, every DataTable

### Architecture review

No architecture change; one CSS-only devDependency.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
