# Implementation Plan: Gestión form's presupuesto picker must identify the client

**Branch**: `fix/889_presupuesto-picker-shows-client-name` | **Date**: 2026-08-31 | **Spec**: `spec.md`

**Input**: Feature specification `speckit/specs/005-gestion-presupuesto-picker-client-name/spec.md`

## Summary

`frontend/src/app/dashboard/gestiones/page.tsx` renders the presupuesto
`<Select>` options in the nueva-Gestión form as `Presupuesto #{id}` only,
even though `Presupuesto.persona` is already returned by the API (fixed by
issue #883) and typed on the frontend. Change the option label to include
the client's `fullName()` and `formatCurrency(monto)` when `persona` is present,
falling back to the id-only label when it is not — using the two utility
functions already imported in the same file and already used for the
escribano picker in the same form.

## Technical Context

**Language/Version**: TypeScript 5, React 19, Next.js 16.

**Primary Dependencies**: none new — reuses `fullName`/`formatCurrency` from `@/lib/utils`, already imported in `gestiones/page.tsx`.

**Storage**: N/A — no backend/API/schema change.

**Testing**: Playwright E2E (golden path: create cliente + presupuesto through the real UI, open the Gestión picker, assert the option shows the client's name).

**Target Platform**: `/dashboard/gestiones` nueva-Gestión modal.

**Project Type**: Web application; this change touches only `frontend/`.

**Performance Goals**: N/A — pure rendering change, no new requests.

**Constraints**: No backend change; reuse existing helpers rather than introducing new formatting logic.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Issue #889 references CU02 — Iniciar Gestión. ✅
- TDD: `CU02-GW05` written first in `TS-0011-gestiones-crud-workflow.spec.ts`
  and confirmed failing against pre-fix code (assertion timeout — the option
  never carries the client name), then passing after the rendering fix. ✅
- No new entity/DTO/migration. ✅
- No existing test asserted the old `Presupuesto #{id}`-only label, so no
  regression update needed elsewhere.

No violations requiring justification — Complexity Tracking table omitted.

## Project Structure

### Documentation (this feature)

```text
speckit/specs/005-gestion-presupuesto-picker-client-name/
├── spec.md
├── plan.md
├── tasks.md
└── traceability.md
```

### Source Code (repository root)

```text
frontend/
├── src/app/dashboard/gestiones/page.tsx        # presupuesto SelectItem label now shows cliente + monto
└── tests/e2e/
    └── TS-0011-gestiones-crud-workflow.spec.ts # +CU02-GW05
```

**Structure Decision**: Single-file production change, frontend-only —
mirrors the minimal-diff shape of the prior two fixes (#879, #883).

## Testing Strategy *(mandatory — CONSTITUTION.md §3)*

| Scenario | Test Type | Test Name |
|----------|-----------|-----------|
| US1 scenario 1 (presupuesto with cliente shows cliente name in picker) | Playwright E2E | `TS-0011-gestiones-crud-workflow.spec.ts` — `CU02-GW05` |

US1 scenario 2 (presupuesto without cliente keeps showing id-only) is
covered by inspection/type-safety — `p.persona ? ... : ...` — and by the
existing `CU02-GW01`/`CU19-GW01` tests, which continue to exercise the
picker against presupuestos without a `persona` (the seed presupuesto) and
pass unchanged.

## Regression Strategy *(mandatory)*

No existing test asserted the previous `Presupuesto #{id}`-only label
verbatim, so no other test required updating. Full frontend E2E suite for
the gestiones/presupuestos modules re-run to confirm no other regression.

## Playwright Strategy *(mandatory — UI change)*

`CU02-GW05` in `TS-0011-gestiones-crud-workflow.spec.ts`:

1. Create a cliente and a presupuesto linked to that cliente through the
   real UI (`/dashboard/personas`, `/dashboard/presupuestos`).
2. Open the nueva-Gestión modal, open the presupuesto picker.
3. Assert an option matching the cliente's apellido is visible.

## Deployment Strategy *(mandatory)*

No infrastructure change. Standard frontend rebuild/redeploy picks up the
fix; no Flyway migration, no config/env changes.

## Rollback Strategy *(mandatory)*

Pure code revert (`git revert` of the fix commit) — no data migrated, no
schema changed.
