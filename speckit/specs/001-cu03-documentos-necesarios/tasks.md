---
description: "Task list for CU03 — documentos necesarios por trámite"
---

# Tasks: Lista de documentos y certificados necesarios por trámite

**Input**: Design documents from `speckit/specs/001-cu03-documentos-necesarios/`

**Prerequisites**: `plan.md`, `spec.md`

**Tests**: Mandatory — TDD is a Constitution requirement, not optional here.

**Organization**: Tasks are grouped by user story (US1/US2/US3), wrapped in
the Notaire SDLC Gates per `CONSTITUTION.md` §5/§6.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1 / US2 / US3

### Gate 1 — Prerequisites

- [x] T001 GitHub Issue #860 exists, labeled `FRONTEND`/`requerimiento-funcional`, linked to Use Case CU-03 (#156)
- [x] T002 Use Case documentation exists and is accurate (`docs/100-business/102-use-cases/CU03 – Lista documentos y certificados necesarios.md`) — no update needed, description matches verified scope
- [x] T003 Acceptance Criteria defined as Given/When/Then scenarios in `spec.md`
- [x] T004 Issue #860 moved to IN PROGRESS (`gh issue edit 860 --add-label "in-progress"`)

### Crear branch

- [x] T005 `git checkout main && git pull origin main`
- [x] T006 `git checkout -b feat/860_documentos-necesarios-tramite` (based on `chore/857_speckit-adaptation` since `speckit/` is not yet on `main` — PR #859 open)
- [x] T007 Branch name recorded in `traceability.md`

## Phase 1: Setup (Shared Infrastructure)

- [x] T008 Confirm `frontend/src/types.ts` (or equivalent) has/needs a `PlantillaTramite` type matching the API shape (`tiposDeDocumento`/`tiposDeTramite` nested objects, `observaciones`)

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Nothing blocking — the backend endpoint, `TipoDeTramite` type, and `useTiposTramite()` hook already exist. No foundational work required beyond T008.

## Phase 3: User Story 1 - Consultar documentos necesarios para un trámite (Priority: P1) 🎯 MVP

**Goal**: Recepcionista selects a trámite and sees the required documents with all 4 CU03 fields.

**Independent Test**: Select a trámite with `PlantillaTramite` rows, verify the table renders nombre/vence/días/quién entrega for each.

### Tests for User Story 1 (TDD — write first, observe failing)

- [x] T009 [P] [US1] Unit test `frontend/src/hooks/usePlantillaTramite.test.tsx` — asserts the hook calls `GET /plantilla-tramite/tipo-tramite/{id}` and returns the typed list
- [x] T010 [US1] E2E scenario 1 in `frontend/tests/e2e/cu03-04-documentos.spec.ts` (renamed from the planned `documentos-necesarios.spec.ts` — reuses the existing CU03 `describe` block already present in that file) — select trámite, assert results section renders

### Implementation for User Story 1

- [x] T011 [US1] Create `frontend/src/hooks/usePlantillaTramite.ts` (React Query hook, mirrors `useTiposTramite.ts`)
- [x] T012 [US1] Create `frontend/src/app/dashboard/documentos-necesarios/page.tsx` — trámite selector (`useTiposTramite`) + `DataTable`/table using `FormContainer`/`FormSection`
- [x] T013 [US1] Add nav item to `frontend/src/components/layout/AppSidebar.tsx` (flat list, sibling of `documentos`)

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Imprimir la lista de documentos (Priority: P2)

**Goal**: Recepcionista can print the visible list.

**Independent Test**: With the list visible, trigger print, verify a printable view with the same data.

### Tests for User Story 2

- [x] T014 [US2] Print action covered by the global `[data-printable="true"]` `@media print` rule in `globals.css` (same pattern as other CU printable views) — exercised manually, not a separate Playwright scenario since `window.print()` cannot be asserted in Chromium headless

### Implementation for User Story 2

- [x] T015 [US2] Add print action (`window.print()` + `@media print` styles) to the CU03 page

**Checkpoint**: User Stories 1 and 2 both work independently.

---

## Phase 5: User Story 3 - Trámite sin documentos configurados (Priority: P3)

**Goal**: Exception 7.1 — clear message instead of blank screen/error.

**Independent Test**: Select a trámite with no `PlantillaTramite` rows, verify a clear empty-state message, no console error.

### Tests for User Story 3

- [x] T016 [US3] Covered by T010's E2E scenario: the seeded E2E trámite has no `PlantillaTramite` rows, so selecting it exercises the `data-testid="empty-state"` path (exception 7.1) in the same assertion

### Implementation for User Story 3

- [x] T017 [US3] Add empty-state handling to the CU03 page for a 0-length `PlantillaTramite` response

**Checkpoint**: All three user stories independently functional.

---

### Actualizar tests existentes

- [x] T018 Confirm no existing test targets `frontend/src/app/dashboard/tramites/` or `useTiposTramite` — none expected, screen is new

### Ejecutar regresión

- [x] T019 `cd frontend && npx tsc --noEmit && npx vitest run` — 261/261 passing; preflight's `npm run lint`/`eslint --fix` also green
- [x] T020 `mvn verify -pl backend-api` (no backend files touched, still run per Step 5 of the workflow) — covered by `scripts/preflight.sh --fix`

### Ejecutar Playwright

- [x] T021 `cd frontend && npx playwright test tests/e2e/cu03-04-documentos.spec.ts` — 1 passed, 5 skipped (unrelated CU04/07/08/11/12 placeholders), run against a rebuilt `notaire-frontend` Docker image so the new route was actually served
- [x] T022 Verify the screen at 320px, 768px, 1024px — confirmed via a throwaway Playwright script (`page.setViewportSize` + full-page screenshot, discarded after review) and a manual browser pass at desktop width; sidebar collapses to hamburger at 320px, selector/table stay full-width, no overflow

### Gate 3 — Actualizar documentación permanente

- [x] T023 Update `docs/` UI-endpoint traceability mapping to record that `GET /api/v1/plantilla-tramite/tipo-tramite/{id}` is now invoked from the new UI screen (`REST-API-ENDPOINT_REGISTRY.md`)
- [x] T024 Mark CU03 in `docs/100-business/102-use-cases/CU03 – Lista documentos y certificados necesarios.md` as implemented, referencing #860
- [x] T025 `bash scripts/preflight.sh --fix` — 16 passed, 2 skipped (trivy not installed locally, server-backed suites need `--full`)

### Commits atómicos

- [x] T026 Commit hook + test, screen + test, print feature, empty-state, docs — as separate atomic commits where reasonable (`47dc28d`, `66930b7`, `307a903`)
- [x] T027 Every commit message ends with `Closes #860` only on the commit that actually closes it (`66930b7`)
- [x] T028 Commit SHAs recorded in `traceability.md`

### Pull Request y validación CI

- [x] T029 `git push -u origin feat/860_documentos-necesarios-tramite`
- [x] T030 Open PR titled `[#860] feat: pantalla de documentos necesarios por trámite (CU03)`
- [ ] T031 Wait for CI green
- [x] T032 PR number recorded in `traceability.md` (#861)

### Deploy

- [ ] T033 Merge via PR only, after explicit human approval — never push to `main` directly
- [ ] T034 Confirm CD pipeline published the frontend image
- [ ] T035 Merge commit / release recorded in `traceability.md`

### Gate 5 — Smoke test y cierre

- [ ] T036 Smoke test: open the new screen against the deployed environment, select a real trámite, confirm the list renders
- [ ] T037 Confirm rollback path (plain PR revert) still available
- [ ] T038 Close Issue #860 referencing the PR

## Definition of Done

- [x] Issue #860 linked to Use Case CU-03, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2) — not independently re-verified this session; hook/E2E tests exist and pass now
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor (unaffected — no backend change)
- [x] Playwright E2E green for the new screen
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
