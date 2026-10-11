# Row action icons render 0px wide on 6 admin pages (#1525)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1525 |
| Use Case | RNF-07 – Usability; CU76 – UI/UX |
| Branch | `fix/1525_notaire_icon_shrink` |
| Gate 1 status | draft |

## Objetivo

On desktop, the edit and delete buttons in the action column of the folios, documents, templates, management-statuses, procedures and concepts tables showed no icon. Tailwind preflight gives every img max-width: 100%, so a replaced element contributes 0 to its button's min-content width; the ghost size="sm" button in the narrow action column shrank to its padding and the NotaireIcon image was drawn 0px wide. The buttons stayed clickable, so users clicked empty cells. NotaireIcon now can't shrink.

## What Changes

- `frontend/src/components/ui/notaire-icon.tsx`: `shrink-0 max-w-none` on the image, so it keeps its declared size and the button can't shrink below icon + padding (central fix: every NotaireIcon caller benefits, no page changes).
- Vitest `notaire-icon.test.tsx`: guard on the classes, size attributes and caller classes.
- Playwright `TS-0140-row-action-icons.spec.ts`: on management-statuses, concepts and procedures, at 1440px and 390px, every icon in the first rows' action buttons is at least 12x12.
- CHANGELOG held (serialize); testing INVENTORY regenerated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An icon keeps its declared size inside any button | ui-ux-design.md (visual consistency) | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-touch-targets`: Row action buttons are usable: sized hit areas and visible icons.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | NotaireIcon, guard test |
| `testing/e2e` | yes | TS-0140 |
| `backend-api` | no | — |

### Surface area

- Endpoints: none
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: one shared component's CSS classes.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Held (serialize) |
| `docs/300-development/303-testing/INVENTORY.md` | TS-0140 listed |
