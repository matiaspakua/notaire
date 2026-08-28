# Implementation Plan: Lista de documentos y certificados necesarios por trámite

**Branch**: `feat/860_documentos-necesarios-tramite` | **Date**: 2026-08-28 | **Spec**: `spec.md`

**Input**: Feature specification `speckit/specs/001-cu03-documentos-necesarios/spec.md`

## Summary

Add a read-only frontend screen where a Recepcionista picks a `TipoDeTramite`
and sees/prints the list of required `TipoDeDocumento` (nombre, vence, días
de validez, quién entrega), sourced entirely from the existing
`GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}` endpoint. No
backend or database changes.

## Technical Context

**Language/Version**: TypeScript 5, React 19, Next.js 16 (frontend); no backend changes.

**Primary Dependencies**: `@tanstack/react-query` (existing hook pattern), `next-intl`, existing `@/theme` design system.

**Storage**: N/A — reuses existing PostgreSQL tables (`plantilla_tramites`, `tipos_de_documento`, `tipos_de_tramite`); no migration.

**Testing**: Vitest (hook unit test), Playwright (E2E).

**Target Platform**: Web (Next.js dashboard).

**Project Type**: Web application (existing `frontend/` + `backend-api/` split); this feature is frontend-only.

**Performance Goals**: List renders within the same budget as other dashboard tables (<2s interactive, per `.claude/rules/ui-ux-design.md`).

**Constraints**: Must follow `FormContainer`/`FormSection`/`FormField` and `theme/tokens.ts` — no hardcoded colors/spacing. Print view must not depend on a server round-trip.

**Scale/Scope**: One new screen, one new hook, one E2E spec. No new entities.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check Phase 1 design.*

- Issue #860 open, linked to Use Case CU-03 (#156) — Gate 1 satisfied.
- TDD required: hook test written and observed failing before implementation.
- UI change → Playwright E2E mandatory (`.claude/rules/ai-agent-workflow.md` Step 2 table).
- No Flyway migration → `.claude/rules/database-migrations.md` not triggered.
- No new REST endpoint → no OpenAPI/Swagger changes required; UI traceability is satisfied by documenting, in `docs/`, that the existing `PlantillaTramiteController` endpoint is now invoked from the UI for the first time.

No violations to justify — Complexity Tracking table intentionally omitted.

## Project Structure

### Documentation (this feature)

```text
speckit/specs/001-cu03-documentos-necesarios/
├── spec.md
├── plan.md
├── tasks.md
└── traceability.md
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── hooks/
│   │   └── usePlantillaTramite.ts       # NEW — GET /plantilla-tramite/tipo-tramite/{id}
│   ├── app/dashboard/
│   │   └── documentos-necesarios/
│   │       └── page.tsx                  # NEW — CU03 screen
│   ├── components/layout/AppSidebar.tsx  # +1 nav item (flat list, matches existing convention)
│   └── theme/                            # existing tokens/form-patterns, reused
└── tests/e2e/
    └── documentos-necesarios.spec.ts     # NEW — Playwright E2E

backend-api/                              # unchanged — endpoint already exists
```

**Structure Decision**: Reuse the existing `frontend/` app-router dashboard structure (`src/app/dashboard/<area>/page.tsx` + `src/hooks/`), matching the flat single-level `navItems` convention in `AppSidebar.tsx` (sibling of `documentos`, `gestiones`, etc. — not nested under a `tramites/` grouping). No new top-level directories.

## Complexity Tracking

*No Constitution Check violations — table intentionally empty.*

## Testing Strategy *(mandatory — CONSTITUTION.md Gate 3)*

| Spec Scenario | Test Level | Test Class / Spec |
|----------------|-----------|--------------------|
| US1 scenario 1 (lista mostrada con 4 columnas) | E2E | `frontend/tests/e2e/documentos-necesarios.spec.ts` |
| US1 scenario 2 (lista de trámites cargada) | E2E | `frontend/tests/e2e/documentos-necesarios.spec.ts` |
| US2 scenario 1 (impresión) | E2E | `frontend/tests/e2e/documentos-necesarios.spec.ts` (asserts print-triggering action + printable view content) |
| US3 scenario 1 (trámite sin documentos) | E2E | `frontend/tests/e2e/documentos-necesarios.spec.ts` |
| Hook data fetching / error shape | Unit (Vitest) | `frontend/src/hooks/usePlantillaTramite.test.ts` |

## Regression Strategy *(mandatory)*

No existing tests target this screen (it does not exist yet), so no existing
assertions are weakened. Regression surface: `useTiposTramite` is reused
read-only (no changes to that hook or its tests). Full frontend suite
(`npm run test`, `npm run typecheck`, `npm run lint`) re-run before PR, plus
`mvn verify -pl backend-api` to confirm the untouched backend still passes
(no backend files change, but the full gate still runs per `.claude/rules/ai-agent-workflow.md` Step 5).

## Playwright Strategy *(mandatory — UI change)*

New spec `frontend/tests/e2e/documentos-necesarios.spec.ts` covering:
1. Select a trámite with configured documents → table shows nombre/vence/días/quién entrega.
2. Trigger print action → printable content is present/verifiable in the DOM.
3. Select a trámite with no `PlantillaTramite` rows → empty-state message shown, no console error.
Screens verified at 320px, 768px, 1024px per `.claude/rules/ui-ux-design.md`.

## Deployment Strategy *(mandatory)*

Frontend-only change, no migration, no feature flag needed (additive new
route, does not touch existing screens). Ships via the normal `cd.yml`
pipeline once merged — no special rollout sequence.

## Rollback Strategy *(mandatory)*

Revert the PR (no destructive migration to roll back, no data impact — the
change is purely additive: one new route + one new hook, reading existing
data).
