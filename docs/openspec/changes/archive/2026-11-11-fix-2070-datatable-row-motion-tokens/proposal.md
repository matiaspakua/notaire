# DataTable row motion uses shared tokens

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2070 |
| Use Case | RNF-05 — Aspecto visual |
| Branch | `fix/2070_datatable-row-motion-tokens` |
| Gate 1 status | passed |

## Why

DataTable animates every row with a hardcoded 300ms duration, Material ease [0.4, 0, 0.2, 1], a 6px rise, and a stagger that can reach 300ms. The shared contract in theme/motion.ts is 180ms, cubic-bezier(0.2, 0, 0, 1), 4px, and at most six steps of 30ms. On long pages this is the motion clerks see most often, and it does not match login, dialogs, or route transitions (#1368).

## What Changes

- Wire DataTable row/card entrance to motion.duration.base, motion.ease.standard, motion.distance.sm, and staggerDelay.
- Keep reduced-motion rows untransformed via existing MotionConfig.
- Add a Vitest that fails if the hardcoded 0.3s / Material ease returns.
- Document the DataTable motion contract in the theme README / design system.

## Business Rules

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| DataTable row and card entrances use shared motion tokens (duration.base, ease.standard, distance.sm, staggerDelay) | RNF-05 / #1368 / #2070 | New |

## Capabilities

### New Capabilities

- `frontend-motion-tokens`: shared frontend motion timing, including DataTable row entrance.

### Modified Capabilities

- (none)

## Impact Analysis

### Affected modules

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | DataTable rowTransition + Vitest; theme README note |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `frontend/src/components/shared/DataTable.tsx`
- `frontend/src/tests/unit/data-table-motion.test.ts`
- `frontend/src/theme/README.md`
- `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`
- `CHANGELOG.md`
- `docs/openspec/changes/fix-2070-datatable-row-motion-tokens/**`
- `docs/openspec/changes/archive/**/fix-2070-datatable-row-motion-tokens/**`
- `docs/openspec/specs/frontend-motion-tokens/**`

### Architecture review

Presentation-only. Reuses ADR-011 / #1368 motion tokens. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `frontend/src/theme/README.md` | Note DataTable row entrance uses motion tokens / staggerDelay |
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Mention DataTable row entrance tokens if motion section exists |
| `CHANGELOG.md` | Unreleased bug-fix note for DataTable motion tokens |

## Out of Scope

- Redesigning table density
- Wilson / portal / Context Lake
- Enabling `[committer] required=true`
- Changing MotionConfig reduced-motion wiring (#2073 already set)
