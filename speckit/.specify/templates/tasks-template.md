---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. Tests are OPTIONAL - only include them if explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

## Notaire SDLC Gates *(mandatory — CONSTITUTION.md §5, §6)*

<!--
  Governed by CONSTITUTION.md — §5 Official SDLC Workflow, §6 Quality Gates.
  This mirrors the 12 mandatory groups scripts/validate-sdlc-plan.sh enforces
  for openspec/schemas/notaire-sdlc/templates/tasks.md, adapted to SpecKit's
  own T-ID / user-story phase format instead of renumbering it away:

    OpenSpec group          -> SpecKit equivalent below
    1  Gate 1 Prerequisites -> "Gate 1 — Prerequisites" (before Phase 1)
    2  Crear branch         -> "Crear branch" (before Phase 1)
    3  Escribir tests (TDD) -> the "Tests for User Story N" sub-phases
    4  Implementación       -> the "Implementation for User Story N" sub-phases
    5  Actualizar tests     -> "Actualizar tests existentes" (after last story)
    6  Ejecutar regresión   -> "Ejecutar regresión" (after last story)
    7  Ejecutar Playwright  -> "Ejecutar Playwright" (after last story)
    8  Gate 3 documentación -> "Gate 3 — Actualizar documentación permanente"
    9  Commits atómicos     -> "Commits atómicos"
    10 PR y validación CI   -> "Pull Request y validación CI"
    11 Deploy               -> "Deploy"
    12 Gate 5 smoke + cierre-> "Gate 5 — Smoke test y cierre"

  scripts/validate-speckit-plan.sh checks all 12 headings plus Definition of
  Done are present. Do not delete any of them.
-->

### Gate 1 — Prerequisites

- [ ] T001 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [ ] T002 Use Case documentation exists and is accurate — create or update it first if not
- [ ] T003 Acceptance Criteria defined as Given/When/Then scenarios in `spec.md`
- [ ] T004 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`)

### Crear branch

- [ ] T005 `git checkout main && git pull origin main`
- [ ] T006 `git checkout -b <type>/<issue-number>_<description>`
- [ ] T007 Record the branch name in `traceability.md`

<!--
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit-tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize [language] project with [framework] dependencies
- [ ] T003 [P] Configure linting and formatting tools

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T004 Setup database schema and migrations framework
- [ ] T005 [P] Implement authentication/authorization framework
- [ ] T006 [P] Setup API routing and middleware structure
- [ ] T007 Create base models/entities that all stories depend on
- [ ] T008 Configure error handling and logging infrastructure
- [ ] T009 Setup environment configuration management

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (OPTIONAL - only if tests requested) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T010 [P] [US1] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T011 [P] [US1] Integration test for [user journey] in tests/integration/test_[name].py

### Implementation for User Story 1

- [ ] T012 [P] [US1] Create [Entity1] model in src/models/[entity1].py
- [ ] T013 [P] [US1] Create [Entity2] model in src/models/[entity2].py
- [ ] T014 [US1] Implement [Service] in src/services/[service].py (depends on T012, T013)
- [ ] T015 [US1] Implement [endpoint/feature] in src/[location]/[file].py
- [ ] T016 [US1] Add validation and error handling
- [ ] T017 [US1] Add logging for user story 1 operations

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (OPTIONAL - only if tests requested) ⚠️

- [ ] T018 [P] [US2] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T019 [P] [US2] Integration test for [user journey] in tests/integration/test_[name].py

### Implementation for User Story 2

- [ ] T020 [P] [US2] Create [Entity] model in src/models/[entity].py
- [ ] T021 [US2] Implement [Service] in src/services/[service].py
- [ ] T022 [US2] Implement [endpoint/feature] in src/[location]/[file].py
- [ ] T023 [US2] Integrate with User Story 1 components (if needed)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (OPTIONAL - only if tests requested) ⚠️

- [ ] T024 [P] [US3] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T025 [P] [US3] Integration test for [user journey] in tests/integration/test_[name].py

### Implementation for User Story 3

- [ ] T026 [P] [US3] Create [Entity] model in src/models/[entity].py
- [ ] T027 [US3] Implement [Service] in src/services/[service].py
- [ ] T028 [US3] Implement [endpoint/feature] in src/[location]/[file].py

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation updates in docs/
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimization across all stories
- [ ] TXXX [P] Additional unit tests (if requested) in tests/unit/
- [ ] TXXX Security hardening
- [ ] TXXX Run quickstart.md validation

---

### Actualizar tests existentes

- [ ] TXXX Identify existing tests affected by the change (see plan.md — Regression Strategy)
- [ ] TXXX Update them without weakening assertions; document why any old expectation was wrong
- [ ] TXXX Remove tests made genuinely obsolete, stating the reason

### Ejecutar regresión

- [ ] TXXX `mvn test -pl backend-api` — unit + integration
- [ ] TXXX `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] TXXX `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] TXXX No `@Disabled` or skipped tests without documented, approved justification

### Ejecutar Playwright

- [ ] TXXX Add/update the E2E specs listed in plan.md — Playwright Strategy
- [ ] TXXX `cd frontend && npx playwright test` — all green
- [ ] TXXX Verify the affected screens at 320px, 768px and 1024px
- [ ] TXXX If the change has no UI surface, record "n/a — no UI surface" here with the reason

### Gate 3 — Actualizar documentación permanente

- [ ] TXXX Update every permanent document listed in `spec.md` / `plan.md`
- [ ] TXXX Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI
- [ ] TXXX Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [ ] TXXX Archive superseded documents into `docs/archive/`
- [ ] TXXX Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] TXXX `bash scripts/preflight.sh --fix` — mirrors every CI gate

### Commits atómicos

- [ ] TXXX Commit in small, self-contained units, Conventional Commits format
- [ ] TXXX Every commit message ends with `Closes #<issue-number>`
- [ ] TXXX No secrets, no commented-out code, no unrelated changes
- [ ] TXXX Record the commit SHAs in `traceability.md`

### Pull Request y validación CI

- [ ] TXXX `git push -u origin <branch-name>`
- [ ] TXXX Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] TXXX Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] TXXX Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] TXXX Record the PR number in `traceability.md`

### Deploy

- [ ] TXXX Merge via the Pull Request only — never push to `main`
- [ ] TXXX Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] TXXX Record the merge commit and release/tag in `traceability.md`

### Gate 5 — Smoke test y cierre

- [ ] TXXX Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] TXXX Verify the rollback path is still available as described in `plan.md`
- [ ] TXXX Close the GitHub Issue, referencing the PR
- [ ] TXXX Move this feature's `speckit/specs/<this-dir>/` to `speckit/specs/archive/`
      now that its Issue is closed and its PR merged — `validate-speckit-plan.sh`
      requires every non-archived feature's Issue to stay OPEN, so skipping this
      step breaks the validator for every branch, not just this one

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"

# Launch all models for User Story 1 together:
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
