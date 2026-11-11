# Reduced motion keeps state transitions (#2073)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2073 |
| Use Case | RNF-05 — Aspecto visual |
| Branch | `fix/2073_reduced-motion-state` |
| Gate 1 status | passed |

## Objetivo

`globals.css` forces every animation and transition to `0.01ms` under
`prefers-reduced-motion: reduce`. That stops travel, but it also flattens focus
rings and color/opacity state changes into a blink. Keep transform travel off
while allowing perceptible state transitions.

## What Changes

- `frontend/src/app/globals.css`: replace the global `0.01ms` transition kill
  with a policy that removes transform travel and keeps color/opacity/focus
  transitions at `duration.fast` (chosen over an instant cut).
- Vitest: assert the chosen policy and that the login seal path still reports
  `transform: none` under reduced motion (#2069).
- Theme README / CHANGELOG: document the reduced-motion state-transition choice.
- MotionConfig `duration.instant` path stays unchanged.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Under reduced motion, no CSS/JS transform travel (slide, scale, rotate) | RNF-05 / #1368 / #2073 | Made explicit |
| Under reduced motion, focus/color/opacity state changes use `duration.fast` (not a global 0.01ms kill) | RNF-05 / #2073 | New |
| Login seal under reduced motion still has `transform: none` | #2069 / ui-login-form-patterns | Unchanged (regression) |

## Capabilities

### New Capabilities

- `ui-reduced-motion`: global CSS reduced-motion policy for state vs travel.

### Modified Capabilities

- (none — login seal requirement already in `ui-login-form-patterns`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | `globals.css` reduced-motion block; Vitest; theme README |
| `testing/e2e` | no | no new Playwright assertion required |
| `backend-api` | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `frontend/src/app/globals.css`
- `frontend/src/tests/unit/motion-tokens.test.ts` (and/or reduced-motion unit test)
- `frontend/src/tests/unit/login-page.test.tsx` (seal / transform regression)
- `frontend/src/theme/README.md`
- `CHANGELOG.md`
- `docs/openspec/changes/fix-2073-reduced-motion-state/**`
- `docs/openspec/changes/archive/**/fix-2073-reduced-motion-state/**`
- `docs/openspec/specs/ui-reduced-motion/**`

### Architecture review

Presentation-only CSS + token docs. No ADR. JS MotionConfig path unchanged.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed / Changed entry for reduced-motion state transitions |
| `frontend/src/theme/README.md` | Document duration.fast choice under reduced motion |
| `docs/openspec/specs/ui-reduced-motion/spec.md` | Sync delta on archive |

## Out of Scope

- Removing reduced-motion support.
- Broad motion redesign beyond globals.css + tokens needed for AC.
- Wilson / portal / Context Lake / `[committer] required=true`.
