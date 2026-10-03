> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (no architectural change)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 945 --add-label "in-progress"`) — attempted; label ACL 403 for integration (documented)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after queue ahead clears
- [x] 2.2 `git checkout -b cursor/fix-945-persona-validation-toast-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-945/` into `openspec/changes/fix-945-persona-validation-toast/` and run `bash scripts/validate-sdlc-plan.sh fix-945-persona-validation-toast`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (non-409 toast, fallback, 409 curated, dialog open, Dedup-EDGE)
- [x] 3.2 Write/adjust frontend unit tests if the Personas handler is refactored; otherwise rely on Dedup-EDGE as the failing proof first
- [x] 3.3 Write the integration tests where applicable — n/a backend
- [x] 3.4 Run Dedup-EDGE (and any new unit tests) and **observe them fail** or flake before fixing
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Re-verify `handleSaveError` on updated `main`: non-409 uses `extractApiError(err) ?? t("errorSave")`
- [x] 4.2 Fix any residual gap so empty-DNI submit shows a message matching Dedup-EDGE (toast and/or form-level); keep dialog open
- [x] 4.3 Preserve 409 localized `duplicateDocument` path (do not replace with raw English via extractApiError)
- [x] 4.4 Optionally migrate to `presentMutationError` only if 409 curated UX is preserved
- [x] 4.5 Do not change backend validation contracts or invent unrelated form fields

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected (TS-0015 Dedup-EDGE; any Persona save-error units)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (sanity; no backend delta expected)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a if no backend code
- [x] 6.3 `mvn verify -pl backend-api` — n/a if no backend code; CI verifies
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API change)
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Stabilize/fix `Dedup-EDGE` in `TS-0015-personas-clientes-workflow.spec.ts`; update `E2E-TEST-MAPPING.md` if needed
- [x] 7.2 `cd frontend && npx playwright test` — all green (or focused TS-0015 then full CI)
- [x] 7.3 Verify toast/form error visibility at least at 768px and 1024px
- [x] 7.4 This change HAS UI surface — do not mark Playwright n/a

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #945`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-945-persona-validation-toast-69d3`
- [ ] 10.2 Open the PR titled `[#945] fix(frontend): surface persona validation errors on non-409`, referencing Issue and CU17
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm CD as applicable (frontend image if publishing)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: Personas → create without DNI → specific validation feedback; with valid data → create OK
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-945-persona-validation-toast`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU17 / CU61)
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (Dedup-EDGE)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
