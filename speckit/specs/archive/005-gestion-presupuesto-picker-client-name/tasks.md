# Tasks: Gestión form's presupuesto picker must identify the client

**Input**: `plan.md`, `spec.md` from `speckit/specs/005-gestion-presupuesto-picker-client-name/`

## Task groups

### Gate 1 — Prerequisites

- [x] Issue #889 created, linked to CU02 — Iniciar Gestión, moved to `in-progress`.
- [x] `spec.md` written with Given/When/Then Acceptance Scenarios and Notaire Traceability.
- [x] `plan.md` written with Testing / Regression / Playwright / Deployment / Rollback Strategy.

### Crear branch

- [x] `git checkout main && git pull origin main`
- [x] `git checkout -b fix/889_presupuesto-picker-shows-client-name`

### Tests for User Story phases (TDD)

- [x] Write `CU02-GW05` in `TS-0011-gestiones-crud-workflow.spec.ts`.
- [x] Run it against pre-fix `gestiones/page.tsx` and confirm it **fails** (assertion timeout on the option text).

### Implementation for User Story phases

- [x] Update the presupuesto `SelectItem` label in `gestiones/page.tsx` to include `fullName(p.persona)` and `formatCurrency(p.monto)` when `p.persona` is present, falling back to the id-only label otherwise.
- [x] Re-run `CU02-GW05` and confirm it now **passes**.

### Actualizar tests existentes

- [x] No existing test asserted the previous `Presupuesto #{id}`-only label — none required updating.

### Ejecutar regresión

- [x] `npx tsc --noEmit` clean.
- [x] `npm run lint` clean.

### Ejecutar Playwright

- [x] Full `TS-0011-gestiones-crud-workflow.spec.ts` suite green (3 passed, 5 pre-existing skips, no new failures).
- [x] Full `frontend` Playwright suite green (397 passed, 0 failures) via `bash scripts/run_pipeline.sh`.

### Gate 3 — Actualizar documentación permanente

- [x] Update `CHANGELOG.md` with the fix entry.

### Commits atómicos

- [x] Commit 1: `test(gestiones): add failing E2E coverage for presupuesto picker client name` — TDD red commit.
- [x] Commit 2: `fix(gestiones): show cliente name in presupuesto picker` — `Closes #889`.

### Pull Request y validación CI

- [x] `bash scripts/run_pipeline.sh` green.
- [x] `git push -u origin fix/889_presupuesto-picker-shows-client-name`.
- [x] `gh pr create` referencing `Closes #889` — PR #890.
- [x] Wait for CI green on the PR — all checks (`Frontend CI`, `PR Validation`, `CI - Build, Test & Security`, `Playwright E2E — Full Suite`) green.

### Deploy

- [x] Merge PR once CI is green — merged as `f3d72f6` on `main`.

### Gate 5 — Smoke test y cierre

- [x] Smoke test against rebuilt `main` — see `traceability.md` (frontend-only change; `CU02-GW05` E2E already exercised the merged content).
- [x] Issue #889 auto-closed by the merge.
- [x] Archive: move `speckit/specs/005-gestion-presupuesto-picker-client-name/` to `speckit/specs/archive/`.

## Definition of Done

- [x] All TDD/implementation/regression/E2E/docs tasks above checked off.
- [x] `tsc`/lint clean.
- [x] Playwright spec green.
- [x] PR merged, CI green, Issue #889 closed.
- [x] Feature archived under `speckit/specs/archive/`.
