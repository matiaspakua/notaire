# Testimonies: register a testimony manually (#1555)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1555 |
| Use Case | CU07 – Registrar testimonio |
| Branch | `feat/1555_manual_testimony` |
| Gate 1 status | draft |

## Objetivo

CU07 registers testimonies. The UI only created them through `POST /testimonies/{idDeed}/generate`; the bare `POST /testimonies` (#1250 bucket C) had no UI caller, so a historic testimony with chosen values could not be registered. Owner answered Q3: yes.

## What Changes

- `useCreateTestimony()` posts `TestimonyCreate` (deed, number, notes, flagged, verified) and refreshes the list.
- `RegisterTestimonyDialog`: DeedPicker (required), number (positive integer), notes, verified; inline errors for missing values and 400 `field: message` bodies; 404/409 as toasts.
- `/dashboard/testimonies`: 'Registrar testimonio' button next to the unchanged 'Generar' flow.
- Allowlist: `POST /api/v1/testimonies` removed. i18n `testimonios.registrar.*` (es/en).
- Tests: Vitest `register-testimony.test.tsx` (5); Playwright TS-0146 (golden, edge, 390px). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A testimony copies an existing deed: a missing deed answers 400, an unknown one 404 | CU07 | Existing (backend), now surfaced |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `testimony-manual-registration`: Registering a testimony manually from the UI.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | hook, dialog, page button, i18n |
| `testing/e2e` | yes | TS-0146 |
| `contracts` | yes | allowlist |

### Surface area

- Endpoints / entities / Flyway / configuration: none (reuses `POST /testimonies`)

### Architecture review

Frontend only; the dialog is its own component so the page stays declarative.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `contracts/api-reachability-allowlist.yaml` | bucket C entry removed |
