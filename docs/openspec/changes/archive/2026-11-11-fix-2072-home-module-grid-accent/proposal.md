# Home module grid: one accent, mist wells

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2072 |
| Use Case | RNF-05 – Aspecto visual |
| Branch | `fix/2072_home-module-grid-accent` |
| Gate 1 status | passed |

## Objetivo

The dashboard home module grid paints every card with a San Francisco Blue
icon well and a hover `scale-110`, so the catalog repeats the sidebar without
hierarchy. Ranked **P1** in the 2026-11-07 Operate UI pass (login tip is #2069).

## What Changes

- Drop decorative `scale-110` from module icon wells; keep token radius (`rounded-[28px]` / `2xl`) and type.
- Give mist wells (`bg-secondary` / mist `#F5F5F7`) to module icons by default.
- At most one module icon well may use San Francisco Blue (`bg-primary/10 text-primary`); when the existing expirations alert is the accent surface, every module well stays mist.
- Keep module links keyboard-reachable; existing dashboard unit tests stay green.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Module icon wells do not use decorative scale on hover | RNF-05; DESIGN.md One Seal Rule | Made explicit |
| At most one home accent uses San Francisco Blue among module wells (or the expirations alert stands as the accent) | RNF-05; DESIGN.md | Made explicit |
| Non-accent module wells use mist | RNF-05; DESIGN.md mist wells | Made explicit |

## Capabilities

### New Capabilities

- `dashboard-home-module-grid`: home module cards keep token chrome, drop icon scale, and limit SF Blue accent wells.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | `dashboard/page.tsx` module grid styling + Vitest |
| `frontend-swing` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github/workflows`) | no | – |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING: no

### Write set

- `frontend/src/app/dashboard/page.tsx`
- `frontend/src/tests/unit/dashboard-home-module-grid.test.tsx`
- `testing/e2e/tests/TS-0119-design-tokens.spec.ts`
- `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`
- `docs/openspec/changes/fix-2072-home-module-grid-accent/**`
- `docs/openspec/specs/dashboard-home-module-grid/**`
- `CHANGELOG.md`

### Architecture review

Presentation-only Operate polish under ADR-011 tokens. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Note home module grid mist wells + no icon scale |
| `CHANGELOG.md` | User-visible home grid accent hierarchy |

## Out of Scope

- Removing the module grid entirely
- Wilson / portal / Context Lake / `[committer] required=true`
- Redesigning metric cards beyond what acceptance requires (acceptance is module cards)
