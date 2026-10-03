> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #800 exists, labeled, and linked to CU27/CU32/CU04/CU72
- [x] 1.2 Use Case documentation exists — CU27/CU32 to note enabled/returned
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR: n/a — no new architectural pattern
- [x] 1.6 Move Issue #800 to IN PROGRESS — attempted; label ACL denied (recorded)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/feat-800-tipo-documento-defaults-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the Requirement coverage table in `traceability.md`
- [x] 3.2 Write Vitest for EMPTY defaults (`enabled: true`, `returned: false`) and type field
- [x] 3.3 Write backend IT for persist/read of enabled+returned; extend Playwright scenarios
- [x] 3.4 Run them and **observe them fail**
- [x] 3.5 Confirm every `#### Scenario:` maps to at least one test

## 4. Implementación

- [x] 4.1 `DtoDocumentType`: add `returned` getter/setter
- [x] 4.2 `DocumentType.setAtributos` / `getDto`: map `returned`; Englishize touched Spanish comments
- [x] 4.3 `DocumentTypeController.create`: default `enabled`/`returned` only when omitted (do not force overwrite)
- [x] 4.4 Frontend type: add `returned?` to `TipoDeDocumento`
- [x] 4.5 Form: EMPTY defaults + CheckboxFields for enabled/returned; wire save/openEdit; i18n; Englishize toasts

## 5. Actualizar tests existentes

- [x] 5.1 Confirm `DocumentTypeReferentialIntegrityTest` create helper still works with defaults
- [x] 5.2 Confirm `SubmittedDocumentControllerTest` inheritance tests remain green
- [x] 5.3 Remove obsolete tests — n/a

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api -Dtest=DocumentTypeReferentialIntegrityTest,SubmittedDocumentControllerTest`
- [ ] 6.2 `mvn jacoco:check -pl backend-api` as capacity allows / via preflight
- [ ] 6.3 `mvn verify -pl backend-api` as capacity allows / via preflight
- [x] 6.4 Bruno — no contract break for existing clients
- [x] 6.5 No `@Disabled` or skipped tests without justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/extend Playwright for enabled+returned create/edit
- [ ] 7.2 Run the document-type form Playwright specs
- [ ] 7.3 Viewport sanity on the dialog (320 / 768 / 1024)
- [ ] 7.4 Golden path + edge paths from design.md covered

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update CU27 / CU32 for enabled/returned fields
- [x] 8.2 OpenAPI — DTO field surfaces via Jackson; confirm create/update docs still accurate
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded documents — n/a
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` as capacity allows

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #800`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/feat-800-tipo-documento-defaults-69d3`
- [ ] 10.2 Open draft PR `[#800] feat(frontend): expose document-type enabled and returned fields` with `Closes #800`
- [ ] 10.3 Wait for required workflows — coordinator watches heavy CI
- [ ] 10.4 Gate 4 — CI green, code review, no conflicts — coordinator merges
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — coordinator; do not merge from this agent
- [ ] 11.2 Confirm the CD pipeline published the image — coordinator
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test on target environment after merge
- [ ] 12.2 Verify rollback path still available
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-800-tipo-documento-defaults`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression; E2E for UI
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
