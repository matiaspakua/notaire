> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1490, CU75. Label `in-progress` was rejected by GitHub permissions and is recorded in traceability.md
- [x] 1.2 Use Case documentation exists and is accurate — CU75 already covers Flyway versioning
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — not architectural; no ADR
- [x] 1.6 Move the Issue to IN PROGRESS — attempted; permissions blocked the label. Issue stays open as #1490

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — branched from fetched `origin/main` (`d0742dae`)
- [x] 2.2 `git checkout -b cursor/english-codebase-01e0`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths — see design.md Testing Strategy
- [x] 3.2 Write `EnglishSurfaceRatchetTest` for the delta scenarios
- [x] 3.3 Integration coverage stays with the existing Testcontainers suite once `V45` is applied
- [x] 3.4 Run the ratchet and observe it fail before the rename
- [x] 3.5 Confirm every `#### Scenario:` maps to a row in `traceability.md`

## 4. Implementación

- [x] 4.1 Rewrite Spanish `/api/v1` and `/dashboard` path segments to English outside historical migrations, archives, and `CHANGELOG.md`
- [x] 4.2 Move dashboard route folders to the English segments
- [x] 4.3 Add `V45__rename_remaining_spanish_identifiers.sql` and point JPA mappings, `data.sql`, and the uniqueness integration test at the new names
- [x] 4.4 Rename Spanish Java types (`AdministradorJpa`, `CarpetasEnWaitException`, `DtoValido`, `IPersistenciaJpa`, Spanish DTO records) and their imports
- [x] 4.5 Update `CU-API-MATRIX.csv`, `validate-cu-api-matrix.py` required bases, OpenAPI, and the #1490 accepted-breaking file
- [x] 4.6 Confirm the ratchet passes

## 5. Actualizar tests existentes

- [x] 5.1 Update path assertions via the same codemod (unit, Bruno, Playwright, `UseCaseRouteCatalog`)
- [x] 5.2 Keep status and body assertions; only the path and renamed types change
- [x] 5.3 Do not delete coverage. Historical migration tests that replay V1–V14 stay on the old SQL

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api -Dtest=EnglishSurfaceRatchetTest,UseCaseRouteCatalogTest` and the path-sensitive controller tests that the rename touches
- [x] 6.2 `mvn -pl backend-api test` for the module when the environment can run it
- [x] 6.3 `python3 workspace/sdlc/validate-cu-api-matrix.py` and `python3 -m unittest workspace/tests/test_validate_cu_api_matrix.py`
- [x] 6.4 Bruno collection paths updated in `backend-api/api-test`; full HTTP suite when the stack is up
- [x] 6.5 No `@Disabled` added

## 7. Ejecutar Playwright

- [x] 7.1 E2E specs updated by the path codemod under `testing/e2e/tests/`
- [ ] 7.2 Run the Playwright specs that hit login and the renamed dashboard routes when the app can boot
- [ ] 7.3 Check the people screen and the navigation at 320, 768, and 1024 when a browser is available
- [x] 7.4 UI surface exists; this group is not n/a

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Update `CU-API-MATRIX.csv` and `backend-api/api-test/COVERAGE.md`
- [x] 8.2 Update OpenAPI paths and the accepted-breaking file
- [x] 8.3 Add the `[Unreleased]` breaking entry in `CHANGELOG.md`
- [x] 8.4 Nothing to archive
- [x] 8.5 Permanent docs stay the source of truth; this change folder is the plan only
- [x] 8.6 `bash workspace/sdlc/validate-sdlc-plan.sh refactor-1490-english-codebase`

## 9. Commits atómicos

- [x] 9.1 Conventional Commits, one commit for the plan and one for the rename
- [x] 9.2 Messages end with `Closes #1490` only on the commit that should close the issue; intermediate commits use `Issue: #1490` so the issue stays open until the PR merges. The final PR body uses `Closes #1490`
- [x] 9.3 No secrets and no unrelated edits
- [x] 9.4 Record SHAs in `traceability.md` after the commits exist

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/english-codebase-01e0`
- [x] 10.2 Open the PR titled `[#1490] refactor(codebase): translate remaining surface to English`
- [ ] 10.3 CI workflows run on the PR
- [ ] 10.4 Gate 4 waits on review
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 CD publishes the image after merge
- [ ] 11.3 Record the merge commit when it exists

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke `GET /actuator/health` and `POST /api/v1/users/login` after deploy
- [ ] 12.2 `R45` reverse rename stays available
- [ ] 12.3 Close #1490 from the merged PR
- [ ] 12.4 Archive the change after merge

## Definition of Done

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
