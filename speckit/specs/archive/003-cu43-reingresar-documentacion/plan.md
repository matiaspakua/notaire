# Implementation Plan: Reingresar documentación

**Branch**: `feat/865_cu43-reingresar-documentacion` | **Date**: 2026-08-28 | **Spec**: `spec.md`

**Input**: Feature specification `speckit/specs/003-cu43-reingresar-documentacion/spec.md`

## Summary

Add a `ReingresoDocumentacionService` + two `GestionController` endpoints
(`GET`/`POST` under `/api/v1/gestiones/{id}/reingreso-documentacion`) that
let a Gestor/Escribano see a gestión's trámites with their documentación
necesaria (via the existing `PlantillaTramite`) and create a new
`DocumentoPresentado` (`reingresado=true`) for a chosen `(trámite, tipo de
documento)` pair. A dedicated frontend screen consumes both endpoints. No
backend entity or Flyway migration is needed — the model already supports
every field (verified in `spec.md`).

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.0 (backend); TypeScript 5, React 19, Next.js 16 (frontend).

**Primary Dependencies**: Spring Data JPA (`repository` package), `@tanstack/react-query`, existing `@/theme` design system.

**Storage**: PostgreSQL 16, existing tables (`documentos_presentados`, `plantilla_tramites`, `tramites`, `gestiones_de_escritura`); no migration.

**Testing**: JUnit 5 + Mockito (unit), `@SpringBootTest` + H2 (integration), Playwright (E2E).

**Target Platform**: Backend REST API + Next.js dashboard.

**Project Type**: Web application (existing `backend-api/` + `frontend/` split).

**Performance Goals**: Same budget as other gestión detail screens (<2s interactive).

**Constraints**: New data access goes in `repository` (never `jpa`); DTOs named `DtoEntityName`; REST under `/api/v1/...`; frontend must follow `FormContainer`/`FormSection`/`FormField`/`FormActions` and `theme/tokens.ts`.

**Scale/Scope**: One new service, two endpoints, five new DTOs, one new frontend screen + hook, one E2E spec. No new entities, no new repository query methods (existing `PlantillaTramiteRepository.findById`/`findByTipoDeTramiteIdTipoTramite` and `TramiteRepository.findById`/`findByFkIdGestionIdGestion` cover everything).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check Phase 1 design.*

- Issue #865 open, linked to Use Case CU-43 (#196) — Gate 1 satisfied.
- TDD required: unit tests (`ReingresoDocumentacionServiceTest`) and integration tests (`GestionReingresoDocumentacionIntegrationTest`) written and observed failing before implementation.
- UI change → Playwright E2E mandatory (`.claude/rules/ai-agent-workflow.md` Step 2 table).
- No Flyway migration → `.claude/rules/database-migrations.md` not triggered.
- New REST endpoints → OpenAPI/Swagger annotations required (`@Operation` on both endpoints) and UI-endpoint traceability entry.
- New service uses existing `repository` package classes only, never `jpa`.

No violations to justify — Complexity Tracking table intentionally omitted.

## Project Structure

### Documentation (this feature)

```text
speckit/specs/003-cu43-reingresar-documentacion/
├── spec.md
├── plan.md
├── tasks.md
└── traceability.md
```

### Source Code (repository root)

```text
notaire-shared/
└── src/main/java/com/licensis/notaire/dto/
    ├── DtoDocumentoNecesario.java                    # NEW
    ├── DtoTramiteDocumentacionNecesaria.java          # NEW
    ├── DtoGestionReingresoDocumentacion.java          # NEW
    ├── DtoReingresoDocumentacionRequest.java          # NEW
    └── DtoDocumentoReingresado.java                   # NEW

backend-api/
├── src/main/java/com/licensis/notaire/
│   ├── service/ReingresoDocumentacionService.java     # NEW
│   └── api/GestionController.java                      # +2 endpoints
└── src/test/java/com/licensis/notaire/
    ├── unit/ReingresoDocumentacionServiceTest.java     # NEW
    ├── unit/AdditionalControllersTest.java              # updated constructor wiring
    └── integration/GestionReingresoDocumentacionIntegrationTest.java  # NEW

frontend/
├── src/
│   ├── hooks/useReingresoDocumentacion.ts              # NEW
│   ├── app/dashboard/reingreso-documentacion/
│   │   └── page.tsx                                     # NEW — CU43 screen
│   └── components/layout/AppSidebar.tsx                 # +1 nav item
└── tests/e2e/
    └── cu43-reingreso-documentacion.spec.ts              # NEW — Playwright E2E
```

**Structure Decision**: Same layering as CU10 (`002-cu10-...`): a new
top-level dashboard page listing gestiones, a row action opens a `Dialog`
that fetches `GET .../reingreso-documentacion` for that gestión (trámites +
documentación necesaria), and choosing a tipo de documento from a trámite's
list submits `POST .../reingreso-documentacion`. `useReingresoDocumentacion.ts`
mirrors `usePlantillaTramite.ts`/`useDocumentosEntidadExterna.ts`.

## Complexity Tracking

*No Constitution Check violations — table intentionally empty.*

## Testing Strategy *(mandatory — CONSTITUTION.md Gate 3)*

| Spec Scenario | Test Level | Test Class / Spec |
|----------------|-----------|--------------------|
| US1 scenario 1 (trámites con documentación necesaria) | Unit + Integration | `ReingresoDocumentacionServiceTest$ObtenerDocumentacionNecesariaTests#shouldReturnTramitesWithDocumentacionNecesaria`, `GestionReingresoDocumentacionIntegrationTest#shouldListTramitesWithDocumentacionNecesaria` |
| US1 scenario 2 (gestión inexistente → 404) | Unit + Integration | `...#shouldThrowWhenGestionNotFound`, `GestionReingresoDocumentacionIntegrationTest#shouldReturn404OnGetWhenGestionDoesNotExist` |
| US1 scenario 3 (trámite sin PlantillaTramite → lista vacía) | Unit + Integration | `...#shouldReturnEmptyDocumentacionWhenNoPlantilla`, `GestionReingresoDocumentacionIntegrationTest#shouldReturnEmptyDocumentacionWhenNoPlantilla` |
| US2 scenario 1 (reingreso válido crea DocumentoPresentado) | Unit + Integration | `ReingresoDocumentacionServiceTest$ReingresarTests#shouldCreateDocumentoPresentadoWhenValid`, `GestionReingresoDocumentacionIntegrationTest#shouldReingresarWhenValid` |
| US2 scenario 2 (tipo no forma parte de la plantilla → 400) | Unit + Integration | `...#shouldThrowWhenTipoDocumentoNotInPlantilla`, `GestionReingresoDocumentacionIntegrationTest#shouldReturn400WhenTipoDocumentoNotInPlantilla` |
| US2 scenario 3 (trámite ajeno a la gestión → 400) | Unit + Integration | `...#shouldThrowWhenTramiteDoesNotBelongToGestion`, `GestionReingresoDocumentacionIntegrationTest#shouldReturn400WhenTramiteDoesNotBelongToGestion` |
| US2 scenario 4 (trámite inexistente → 404) | Unit + Integration | `...#shouldThrowWhenTramiteNotFound`, `GestionReingresoDocumentacionIntegrationTest#shouldReturn404WhenTramiteDoesNotExist` |
| Frontend hook data fetching | Unit (Vitest) | `frontend/src/hooks/useReingresoDocumentacion.test.tsx` |
| E2E golden path | Playwright | `frontend/tests/e2e/cu43-reingreso-documentacion.spec.ts` |

## Regression Strategy *(mandatory)*

New service/endpoints are additive — no existing controller method or DTO is
changed. `AdditionalControllersTest` required updating its `GestionController`
constructor mock wiring (new 16th constructor parameter), a compile-fix, not
a behavior change to existing tests. Full backend suite (`mvn verify -pl
backend-api`) re-run to confirm no regression: 1611/1611 tests passing,
JaCoCo ratchet floor held. Full frontend suite (`npm run test`, `npx tsc
--noEmit`, `npm run lint`) re-run before PR.

## Playwright Strategy *(mandatory — UI change)*

New spec `frontend/tests/e2e/cu43-reingreso-documentacion.spec.ts` covering:
1. Open a gestión's CU43 screen → trámites with their documentación necesaria render.
2. Reingresar a valid tipo de documento → success feedback, new document reflected.
3. A trámite with no `PlantillaTramite` shows an empty documentación necesaria state, no console error.
Screens verified at 320px, 768px, 1024px per `.claude/rules/ui-ux-design.md`.

## Deployment Strategy *(mandatory)*

Additive change only — new endpoints, new screen, no changes to existing
routes or schema. Ships via the normal `cd.yml` pipeline once merged, no
feature flag or special rollout sequence needed.

## Rollback Strategy *(mandatory)*

Revert the PR. No destructive migration, no data impact — the change adds a
service, two endpoints, and one screen; it does not modify existing
persisted data shapes.
