---
description: "Task list for CU10 — movimientos documentación de entidades externas"
---

# Tasks: Registrar movimientos de documentación de entidades externas

**Input**: Design documents from `speckit/specs/002-cu10-movimientos-documentacion-entidades-externas/`

**Prerequisites**: `plan.md`, `spec.md`

**Tests**: Mandatory — TDD is a Constitution requirement, not optional here.

**Organization**: Tasks are grouped by user story (US1/US2/US3), wrapped in
the Notaire SDLC Gates per `CONSTITUTION.md` §5/§6.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1 / US2 / US3

### Gate 1 — Prerequisites

- [x] T001 GitHub Issue #863 exists, labeled `enhancement`/`BACKEND`/`FRONTEND`, linked to Use Case CU-10 (#163)
- [x] T002 Use Case documentation exists and is accurate (`docs/100-business/102-use-cases/CU10 – Registrar movimientos documentación de entidades externas.md`) — no update needed, description matches verified scope
- [x] T003 Acceptance Criteria defined as Given/When/Then scenarios in `spec.md`
- [x] T004 Issue #863 moved to IN PROGRESS (`gh issue edit 863 --add-label "in-progress"`)

### Crear branch

- [x] T005 `git checkout main && git pull origin main`
- [x] T006 `git checkout -b feat/863_cu10-movimientos-documentacion-entidades-externas`
- [x] T007 Branch name recorded in `traceability.md`

## Phase 1: Setup (Shared Infrastructure)

- [x] T008 Add `DtoDocumentoEntidadExterna`, `DtoGestionDocumentosEntidadesExternas`, `DtoMovimientoDocumentoEntidadExterna` to `notaire-shared`

## Phase 2: Foundational (Blocking Prerequisites)

- [x] T009 Add `DocumentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(Integer, String)` query method

### Tests for User Story phases (TDD — write first, observe failing)

- [x] T010 [P] [US1] Unit tests `DocumentoEntidadExternaServiceTest` for `obtenerDocumentos`
- [x] T011 [P] [US2] Unit tests `DocumentoEntidadExternaServiceTest$RegistrarMovimientoTests` for `registrarMovimiento`
- [x] T012 [P] [US3] Unit tests `DocumentoEntidadExternaServiceTest$IntentarCompletarDocumentacionTests` for `intentarCompletarDocumentacion`
- [x] T013 [US1] Integration test `GestionDocumentosEntidadesExternasIntegrationTest#shouldListDocumentosEntidadesExternas` + `#shouldReturn404WhenGestionDoesNotExist`
- [x] T014 [US2] Integration tests `#shouldRegistrarMovimiento`, `#shouldReturn400WhenDocumentDoesNotBelongToGestion`, `#shouldReturn404WhenDocumentDoesNotExist`
- [x] T015 [US3] Integration test `#shouldMarkAllDocumentsDeliveredWithoutFailingWhenNoWorkflow`

## Phase 3: User Story 1 - Consultar documentación de entidad externa de una gestión (Priority: P1) 🎯 MVP

**Goal**: Gestor/Escribano selects a gestión and sees número, encabezado, fecha de inicio, escribano, nomenclatura catastral y documentos de entidad externa.

**Independent Test**: Select a gestión with entidad-externa documents and (optionally) an `Inmueble`, verify the detail response.

### Implementation for User Story 1

- [x] T016 [US1] `DocumentoEntidadExternaService.obtenerDocumentos(idGestion)` — read-only, resolves nomenclatura catastral
- [x] T017 [US1] `GestionController.getDocumentosEntidadesExternas` — `GET /api/v1/gestiones/{id}/documentos-entidades-externas`

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Registrar el movimiento de un documento de entidad externa (Priority: P1)

**Goal**: Gestor/Escribano saves the movement data of one entidad-externa document.

**Independent Test**: `PUT` a valid movement, verify the persisted response; `PUT` on a document from another gestión returns 400.

### Implementation for User Story 2

- [x] T018 [US2] `DocumentoEntidadExternaService.registrarMovimiento` — validates pertenece-a-gestión + es-entidad-externa, applies and saves
- [x] T019 [US2] `GestionController.registrarMovimientoDocumentoEntidadExterna` — `PUT /api/v1/gestiones/{id}/documentos-entidades-externas/{idDocumentoPresentado}`

**Checkpoint**: User Stories 1 and 2 both work independently.

---

## Phase 5: User Story 3 - Transición automática a "Documentacion Completa" (Priority: P2)

**Goal**: Once all entidad-externa documents of a gestión are delivered, best-effort auto-transition, never invalidating the saved movement.

**Independent Test**: Deliver the last pending document; with a workflow transition defined, gestión moves state; without one, the movement still persists and no error surfaces.

### Implementation for User Story 3

- [x] T020 [US3] `DocumentoEntidadExternaService.intentarCompletarDocumentacion` — separate top-level call (invoked from the controller, not nested inside `registrarMovimiento`'s transaction) so a failed `GestionTransitionService.transicionar` never marks the movement's transaction rollback-only
- [x] T021 [US3] Wire `GestionController` to call `intentarCompletarDocumentacion` after `registrarMovimiento` returns

**Checkpoint**: All three user stories independently functional.

---

## Phase 6: Frontend

- [ ] T022 [US1] `frontend/src/hooks/useDocumentosEntidadExterna.ts` — React Query hooks for `GET`/`PUT`
- [ ] T023 [US1][US2][US3] `frontend/src/app/dashboard/documentos-entidades-externas/page.tsx` — gestión list + detail Dialog (header + document table) + movement sub-Dialog, using `FormContainer`/`FormSection`/`FormField`/`FormActions` (mirrors `movimientos-testimonio/page.tsx`'s Dialog-by-id pattern)
- [ ] T024 Nav item added to the dashboard sidebar

### Actualizar tests existentes

- [x] T025 Update `AdditionalControllersTest` — `GestionController` constructor gained a 15th parameter (`DocumentoEntidadExternaService`), mock added to keep existing controller tests compiling and passing

### Ejecutar regresión

- [x] T026 `mvn test -pl backend-api` — 1595/1595 passing, 0 failures, 0 errors
- [x] T027 `mvn verify -pl backend-api` — BUILD SUCCESS
- [ ] T028 `cd frontend && npx tsc --noEmit && npx vitest run`

### Ejecutar Playwright

- [ ] T029 `cd frontend && npx playwright test tests/e2e/cu10-documentos-entidades-externas.spec.ts`
- [ ] T030 Verify the screen at 320px, 768px, 1024px

### Gate 3 — Actualizar documentación permanente

- [ ] T031 Update `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` — record the two new endpoints and the UI screen that invokes them
- [ ] T032 Mark CU10 in `docs/100-business/102-use-cases/CU10 – Registrar movimientos documentación de entidades externas.md` as implemented, referencing #863

### Commits atómicos

- [ ] T033 Commit backend (service+repository+controller+DTOs+tests), frontend (hook+screen+E2E), docs — as separate atomic commits where reasonable
- [ ] T034 Every commit message ends with `Closes #863` only on the commit that actually closes it
- [ ] T035 Commit SHAs recorded in `traceability.md`

### Pull Request y validación CI

- [ ] T036 `bash scripts/preflight.sh --fix`
- [ ] T037 `git push -u origin feat/863_cu10-movimientos-documentacion-entidades-externas`
- [ ] T038 Open PR titled `[#863] feat: registrar movimientos de documentación de entidades externas (CU10)`
- [ ] T039 Wait for CI green
- [ ] T040 PR number recorded in `traceability.md`

### Deploy

- [ ] T041 Merge via PR only, after explicit human approval — never push to `main` directly
- [ ] T042 Confirm CD pipeline published backend + frontend images
- [ ] T043 Merge commit / release recorded in `traceability.md`

### Gate 5 — Smoke test y cierre

- [ ] T044 Smoke test: open the new screen against the deployed environment, register a real movement, confirm it persists
- [ ] T045 Confirm rollback path (plain PR revert) still available
- [ ] T046 Close Issue #863 referencing the PR

## Definition of Done

- [x] Issue #863 linked to Use Case CU-10, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression
- [ ] E2E green — pending Phase 6 frontend implementation
- [x] Coverage held (JaCoCo ratchet floor unaffected/improved)
- [ ] Playwright E2E green for the new screen
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
