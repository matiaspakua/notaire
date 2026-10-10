# Design

## Context

Dialogs compose `DialogContent` with `FormContainer` and `FormSection`/`FormHeader`; only reingreso-documentacion and documentos-entidades-externas used `DialogTitle`.

## Goals / Non-Goals

Goal: a named dialog and a localized close everywhere. Non-goals: the `FormDialog` wrapper and shared submit behaviour (dialog-forms UX issue), dialog motion (#1368).

## Decisions

`asChild` keeps the existing heading element and styles, so there is no visual change. An explicit `dialogTitle` prop (not automatic detection) avoids two titles sharing one Radix id in dialogs with several sections; the static scan keeps new dialogs from forgetting it.

## Riesgos / Trade-offs

A dialog whose first heading is a section rather than its purpose gets that section's name; the 39 headings were checked to be the dialog titles.

## Testing Strategy

`dialog.test.tsx` (6 failed: no name, close always "Cerrar", key missing, 39 untitled dialogs) and `TS-0106` (6 failed) written first.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

`TS-0106`: the create dialogs on personas, presupuestos, gestiones, escrituras and pagos are reachable as `getByRole("dialog", { name })` with a "Cerrar" button; in English, "New person" and "Close".

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
