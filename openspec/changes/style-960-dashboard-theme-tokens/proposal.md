# Replace hardcoded hex with theme tokens in dashboard/table

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #960 |
| Use Case | **CU76** Quality Assurance and Testing Infrastructure (RF #78 Uso de colores en la GUI) |
| Branch | `cursor/style-960-dashboard-theme-tokens-69d3` |
| Gate 1 status | passed (artifacts complete; validate-sdlc-plan) |

## Objetivo

Dashboard layout/home and the shared `Table` UI still hardcode Apple palette hex
values (`#F5F5F7`, `#1d1d1f`, etc.) instead of `theme.colors.*` / semantic
Tailwind wired to the design system. Replace them for maintainability and
RF #78 / frontend-design compliance without redesigning the UI.

## What Changes

- Replace hardcoded `#RRGGBB` color utilities in
  `frontend/src/app/dashboard/layout.tsx`,
  `frontend/src/app/dashboard/page.tsx`, and
  `frontend/src/components/ui/table.tsx` with theme tokens or semantic Tailwind
  classes already mapped to tokens.
- Add a Vitest hex-hygiene guard so audited globs stay free of `#RRGGBB`
  (excluding `theme/` and issue-number references).
- Preserve table header/hover opacity intent via color-mix / opacity utilities
  derived from tokens.
- Update `CHANGELOG.md` (and optional design-system doc pointer).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| GUI colors MUST come from the centralized design system tokens, not ad-hoc hex in app/components | CU76 / RF #78; frontend-design skill; ui-ux-design rules | Made explicit |
| Visual appearance of dashboard home and tables MUST remain equivalent (no redesign) | #960 AC | Made explicit |

## Capabilities

### New Capabilities

- `frontend-design-system-hex-hygiene`: audited dashboard/table surfaces must not
  embed `#RRGGBB` literals; colors resolve through `theme` tokens or semantic
  Tailwind already wired to tokens.

### Modified Capabilities

- (none under `openspec/specs/` today encode this hygiene rule)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | three style files + Vitest hygiene + CHANGELOG |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING**: no

### Architecture review

Follows existing design-system patterns (`@/theme/tokens`, semantic Tailwind as
in dashboard reportes and other pages). No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry: dashboard/table hex → theme tokens (#960) |
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Optional one-line pointer that dashboard/table now use tokens (if a natural place exists) |

## Out of Scope

- Broader hex cleanup outside the three audited files (other app/components
  surfaces stay as-is until separately issued).
- Redesigning dashboard layout, home modules, or table structure.
- Changing `tokens.ts` palette values.
- Backend or API changes.
