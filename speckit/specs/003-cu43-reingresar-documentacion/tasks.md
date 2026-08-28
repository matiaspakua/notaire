---
description: "Task list for CU43 — reingresar documentación"
---

# Tasks: Reingresar documentación

**Input**: Design documents from `speckit/specs/003-cu43-reingresar-documentacion/`

**Prerequisites**: `plan.md`, `spec.md`

**Tests**: Mandatory — TDD is a Constitution requirement, not optional here.

**Organization**: Tasks are grouped by user story (US1/US2), wrapped in
the Notaire SDLC Gates per `CONSTITUTION.md` §5/§6.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1 / US2

### Gate 1 — Prerequisites

- [x] T001 GitHub Issue #865 exists, labeled `enhancement`/`BACKEND`/`FRONTEND`, linked to Use Case CU-43 (#196)
- [x] T002 Use Case documentation exists and is accurate (`docs/100-business/102-use-cases/CU43 – Reingresar documentación.md`)
- [x] T003 Acceptance Criteria defined as Given/When/Then scenarios in `spec.md`
- [x] T004 Issue #865 moved to IN PROGRESS (`gh issue edit 865 --add-label "in-progress"`)

### Crear branch

- [x] T005 `git checkout main && git pull origin main`
- [x] T006 `git checkout -b feat/865_cu43-reingresar-documentacion`
- [x] T007 Branch name recorded in `traceability.md`

## Phase 1: Setup (Shared Infrastructure)

- [x] T008 Add `DtoDocumentoNecesario`, `DtoTramiteDocumentacionNecesaria`, `DtoGestionReingresoDocumentacion`, `DtoReingresoDocumentacionRequest`, `DtoDocumentoReingresado` to `notaire-shared`

## Phase 2: Foundational (Blocking Prerequisites)

- [x] T009 Verify existing `PlantillaTramiteRepository.findByTipoDeTramiteIdTipoTramite`, `TramiteRepository.findByFkIdGestionIdGestion` cover every query need — no new repository methods required

### Tests for User Story phases (TDD — write first, observe failing)

- [x] T010 [P] [US1] Unit tests `ReingresoDocumentacionServiceTest$ObtenerDocumentacionNecesariaTests` for `obtenerDocumentacionNecesaria`
- [x] T011 [P] [US2] Unit tests `ReingresoDocumentacionServiceTest$ReingresarTests` for `reingresar`
- [x] T012 [US1] Integration tests `GestionReingresoDocumentacionIntegrationTest#shouldListTramitesWithDocumentacionNecesaria`, `#shouldReturn404OnGetWhenGestionDoesNotExist`, `#shouldReturnEmptyDocumentacionWhenNoPlantilla`
- [x] T013 [US2] Integration tests `#shouldReingresarWhenValid`, `#shouldReturn400WhenTipoDocumentoNotInPlantilla`, `#shouldReturn400WhenTramiteDoesNotBelongToGestion`, `#shouldReturn404WhenTramiteDoesNotExist`

## Phase 3: User Story 1 - Ver documentación necesaria de los trámites de una gestión (Priority: P1) 🎯 MVP

**Goal**: Gestor/Escribano selects a gestión and sees its trámites, each
with its documentación necesaria (per `PlantillaTramite`).

**Independent Test**: Select a gestión with trámites whose tipo de trámite
has documentación necesaria configured, verify the grouped response.

### Implementation for User Story 1

- [x] T014 [US1] `ReingresoDocumentacionService.obtenerDocumentacionNecesaria(idGestion)` — read-only, groups documentación necesaria per trámite
- [x] T015 [US1] `GestionController.obtenerDocumentacionReingreso` — `GET /api/v1/gestiones/{id}/reingreso-documentacion`

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Reingresar un tipo de documento (Priority: P1)

**Goal**: Gestor/Escribano picks a trámite + tipo de documento and the
system creates a new `DocumentoPresentado` (`reingresado=true`).

**Independent Test**: Reingresar a tipo de documento that is part of the
chosen trámite's `PlantillaTramite`; verify the created `DocumentoPresentado`.

### Implementation for User Story 2

- [x] T016 [US2] `ReingresoDocumentacionService.reingresar` — validates trámite exists, belongs to the gestión, and the tipo de documento is part of its `PlantillaTramite`; creates and saves the `DocumentoPresentado`
- [x] T017 [US2] `GestionController.reingresarDocumentacion` — `POST /api/v1/gestiones/{id}/reingreso-documentacion`

**Checkpoint**: Both user stories independently functional.

---

## Phase 5: Frontend

- [x] T018 [US1] `frontend/src/hooks/useReingresoDocumentacion.ts` — React Query hooks for `GET`/`POST`
- [x] T019 [US1][US2] `frontend/src/app/dashboard/reingreso-documentacion/page.tsx` — gestión list + detail Dialog (trámites with documentación necesaria) + reingreso sub-Dialog, using `FormContainer`/`FormSection`/`FormField`/`FormActions`
- [x] T020 Nav item added to the dashboard sidebar

### Actualizar tests existentes

- [x] T021 Update `AdditionalControllersTest` — `GestionController` constructor gained a 16th parameter (`ReingresoDocumentacionService`), mock added to keep existing controller tests compiling and passing

### Ejecutar regresión

- [x] T022 `mvn test -pl backend-api` — 1611/1611 passing, 0 failures, 0 errors
- [x] T023 `mvn verify -pl backend-api` — BUILD SUCCESS, JaCoCo ratchet floor held
- [x] T024 `cd frontend && npx tsc --noEmit && npx vitest run` — tsc clean; 23 files / 269 tests passing

### Ejecutar Playwright

- [x] T025 `cd frontend && npx playwright test tests/e2e/cu43-reingreso-documentacion.spec.ts` — golden path + edge path both passing
- [x] T026 Verify the screen at 320px, 768px, 1024px — verified via throwaway Playwright viewport spec (no horizontal scroll, header visible at all three breakpoints), then removed

### Gate 3 — Actualizar documentación permanente

- [x] T027 Update `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` — record the two new endpoints and the UI screen that invokes them
- [x] T028 Mark CU43 in `docs/100-business/102-use-cases/CU43 – Reingresar documentación.md` as implemented, referencing #865

### Commits atómicos

- [ ] T029 Commit backend (service+repository verification+controller+DTOs+tests), frontend (hook+screen+E2E), docs — as separate atomic commits where reasonable
- [ ] T030 Every commit message ends with `Closes #865` only on the commit that actually closes it
- [ ] T031 Commit SHAs recorded in `traceability.md`

### Pull Request y validación CI

- [ ] T032 `bash scripts/preflight.sh --fix`
- [ ] T033 `git push -u origin feat/865_cu43-reingresar-documentacion`
- [ ] T034 Open PR titled `[#865] feat: reingresar documentación (CU43)`
- [ ] T035 Wait for CI green
- [ ] T036 PR number recorded in `traceability.md`

### Deploy

- [ ] T037 Merge via PR only, after explicit human approval — never push to `main` directly
- [ ] T038 Confirm CD pipeline published backend + frontend images
- [ ] T039 Merge commit / release recorded in `traceability.md`

### Gate 5 — Smoke test y cierre

- [ ] T040 Smoke test: open the new screen against the deployed environment, reingresar a real document, confirm it persists
- [ ] T041 Confirm rollback path (plain PR revert) still available
- [ ] T042 Close Issue #865 referencing the PR

## Definition of Done

- [x] Issue #865 linked to Use Case CU-43, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression
- [ ] E2E green — pending Phase 5 frontend implementation
- [x] Coverage held (JaCoCo ratchet floor unaffected/improved)
- [ ] Playwright E2E green for the new screen
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
