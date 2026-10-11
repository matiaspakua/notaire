> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #773 exists, labeled, linked to CU02/CU03/CU04/CU72
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (compose existing services; no architectural change)
- [x] 1.6 Move the Issue to IN PROGRESS (may 403; comment with branch when implement starts)

## 2. Crear branch

- [x] 2.1 `git fetch origin main && git checkout main && git pull origin main` (after #1518 / foreman clear)
- [x] 2.2 `git checkout -b cursor/feat-773-required-docs-on-confirm-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 `bash workspace/sdlc/validate-sdlc-plan.sh feat-773-required-docs-on-confirm`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate cases: seed from template, empty template, case-summary list, idempotent skip, PUT no wipe
- [x] 3.2 Write unit tests for `RequiredDocumentationOnConfirmService` (or chosen name)
- [x] 3.3 Write integration tests for complete-case → case-summary
- [x] 3.4 Run TEST_CMD and **observe fail** (exit ≠ 0); capture log for PR
- [x] 3.5 Confirm every `#### Scenario:` maps to at least one test

## 4. Implementación

- [x] 4.1 Add `RequiredDocumentationOnConfirmService` using `ProcedureTemplateRepository` + `SubmittedDocumentRepository` (not legacy `jpa`)
- [x] 4.2 Call it from `ManagementController.saveProcedure` after folder generation (new procedure only)
- [x] 4.3 Ensure pending flags: delivered/reentered/released/flagged false; name/expiry from `DocumentType`
- [x] 4.4 Idempotency: skip existing (procedure, documentType) pairs
- [x] 4.5 Light FE: surface checklist after complete-case via case-summary
- [x] 4.6 Bruno: extend `api-test/procedure-folders/02-complete-case.yml` + `api-test/managements/05b-case-summary.yml`
- [x] 4.7 Regenerate `backend-api/openapi/openapi.yaml` if needed (n/a — no annotation changes)
- [x] 4.8 Englishize touched identifiers/logs/messages
- [x] 4.9 Tests green on TEST_CMD

## 5. Actualizar tests existentes

- [ ] 5.1 Update complete-case / case-summary tests that assumed zero documents after confirm
- [ ] 5.2 Do not weaken assertions; document any legitimate expectation change
- [ ] 5.3 Remove obsolete tests only with stated reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — quality gates
- [ ] 6.4 Bruno/OpenCollection for touched folders (bru CLI ≥4.x)
- [ ] 6.5 No `@Disabled` without approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update focused E2E for post-confirm checklist (not full tutorial rewrite)
- [ ] 7.2 `cd testing/e2e && npx playwright test <spec>` — green
- [ ] 7.3 Verify affected dialog/panel at 1024px (and 768 if layout changes)
- [ ] 7.4 If FE surface deferred with foreman approval, record n/a with reason here

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update CU02, CU03, CU04, CU72 implementation notes (paths in proposal)
- [ ] 8.2 OpenAPI annotations/export if needed
- [ ] 8.3 `CHANGELOG.md` Unreleased (only after #1518 lock free)
- [ ] 8.4 Archive `feat-773-documentos-tramite` when closing #773 if still active
- [ ] 8.5 No duplicated permanent content
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` before push

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits; intermediate may use `Refs #773`
- [ ] 9.2 Final commit / PR body ends with `Closes #773`
- [ ] 9.3 No secrets, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/feat-773-required-docs-on-confirm-69d3`
- [ ] 10.2 Open draft PR `[#773] feat(managements): seed required documents on complete-case`
- [ ] 10.3 Wait for required workflows; serialize vs other heavy PRs
- [ ] 10.4 Gate 4 — CI green, review, no conflicts
- [ ] 10.5 Record PR number in `traceability.md`
- [ ] 10.6 If authz/PII surface changed, notify security peer #4

## 11. Deploy

- [ ] 11.1 Merge via PR only (foreman) after `check-heavy-ci` exit 0
- [ ] 11.2 Confirm CD published image if applicable
- [ ] 11.3 Record merge commit / tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: complete-case → case-summary shows template doc names
- [ ] 12.2 Rollback path still valid (revert-safe, no Flyway)
- [ ] 12.3 Close #773 via `Closes #773`
- [ ] 12.4 `openspec archive feat-773-required-docs-on-confirm`

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
