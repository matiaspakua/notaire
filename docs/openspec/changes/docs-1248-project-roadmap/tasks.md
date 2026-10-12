# Tasks — project roadmap

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1248 exists and is linked to CU76
- [x] 1.2 CU76 already documents repository and process work
- [x] 1.3 Acceptance criteria are the scenarios in `specs/project-roadmap/spec.md`
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No ADR — process documentation only
- [x] 1.6 n/a — #1248 is not moved to in-progress (see traceability Exceptions)

## 2. Crear branch

- [x] 2.1 `main` matched `origin/main` at `bc43db94`
- [x] 2.2 Branch `cursor/project-management-sdlc-566d` (cloud branch template)
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Scenarios enumerated in the delta spec
- [x] 3.2 `workspace/tests/test_roadmap_classify.py` written first
- [x] 3.3 n/a — no HTTP integration surface
- [x] 3.4 Observed failure: `FileNotFoundError` for `roadmap_classify.py` before implementation
- [x] 3.5 Each scenario maps to a test method in traceability.md

## 4. Implementación

- [x] 4.1 Implement `workspace/sdlc/roadmap_classify.py`
- [x] 4.2 Implement dry-run `workspace/sdlc/organize-roadmap.py` and generate the snapshot
- [x] 4.3 Write `docs/300-development/305-project-management/`
- [x] 4.4 Add `project-manager` agent, skill, and fleet registration
- [x] 4.5 Add the GitHub Pages roadmap route and nav entry

## 5. Actualizar tests existentes

- [x] 5.1 No existing product tests assert the old milestone prose
- [x] 5.2 Assertions were not weakened
- [x] 5.3 No obsolete tests removed

## 6. Ejecutar regresión

- [x] 6.1 n/a — no backend-api change; `python3 -m unittest workspace.tests.test_roadmap_classify`
- [x] 6.2 n/a — JaCoCo does not cover this Python module
- [x] 6.3 n/a — no Java sources
- [x] 6.4 n/a — no API change
- [x] 6.5 No tests skipped

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no product UI surface
- [x] 7.2 n/a — product Playwright suite does not render the Pages docs tab
- [x] 7.3 n/a — no product screen
- [x] 7.4 n/a — no product UI surface. The Pages route is checked by the unit test that reads the TSX sources

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Permanent docs listed in the proposal are updated in this change
- [x] 8.2 n/a — no endpoints
- [x] 8.3 n/a — not user-visible product behavior
- [x] 8.4 Nothing superseded moved to the archive; SAD §11.3 now points at the new guide
- [x] 8.5 The guide is the source for counts; the SAD keeps the phase narrative and links out
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` before push

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 `Refs #1248` (not `Closes #1248` — see Exceptions)
- [x] 9.3 No secrets
- [x] 9.4 `527fef7a` recorded in traceability.md

## 10. Pull Request y validación CI

- [ ] 10.1 Push the branch
- [ ] 10.2 Open the PR with Issue #1248 and CU76
- [ ] 10.3 CI on the PR
- [ ] 10.4 Review
- [ ] 10.5 Record the PR number

## 11. Deploy

- [ ] 11.1 Merge only via PR
- [ ] 11.2 Pages publish follows the existing pages workflow after merge; no new image
- [ ] 11.3 Record the merge when it happens

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Unit test plus plan validator are the smoke for this docs change
- [ ] 12.2 Revert is safe (design.md Rollback)
- [ ] 12.3 Do not close #1248
- [ ] 12.4 Archive the OpenSpec change after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1)
- [x] Tests written first and seen failing, then passing
- [x] Permanent docs updated for the manager guide
- [ ] Preflight and PR recorded
- [x] #1248 deliberately left open
