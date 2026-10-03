> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 801 --add-label "in-progress"`) — attempted; GraphQL label ACL 403 for integration (recorded)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/fix-801-submitted-document-mapping-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: null-procedure getDto; DocumentType association + mappedBy
- [x] 3.2 Write unit tests in `SubmittedDocumentEntityTest` for every scenario
- [x] 3.3 Write integration tests where applicable — n/a (unit covers acceptance; existing IT covers bootstrap)
- [x] 3.4 Run them and **observe them fail** — compile failure (missing get/setDocumentType) then NPE on released unboxing before full fix
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Replace Integer column with `@ManyToOne DocumentType documentType` + `@JoinColumn(name = "fk_id_document_type")`
- [x] 4.2 Fix `DocumentType` `mappedBy` to `documentType`; drop Cascade ALL on that OneToMany
- [x] 4.3 Compatibility accessors `getFkIdDocumentType` / setters / nullable delegate to association
- [x] 4.4 Null-guard procedure in `getDto`; remove dead `DtoDocumentType` construction
- [x] 4.5 Rename repository `existsByFkIdDocumentType` → `existsByDocumentTypeIdDocumentType`; update DocumentTypeController + SimpleControllersTest
- [x] 4.6 Translate Spanish identifiers/comments/strings in touched code to English (keep Spanish REST paths)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected (SimpleControllersTest mocks, any ID accessor assumptions)
- [x] 5.2 Update them without weakening assertions
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (via verify)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (via verify)
- [x] 6.3 `mvn verify -pl backend-api` — BUILD SUCCESS
- [x] 6.4 Bruno/HTTP suite — n/a (no API contract change); noted in status
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface (JPA / legacy DTO only)
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface
- [x] 7.4 Recorded "n/a — no UI surface" — entity mapping fix only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update CU72 for optional procedure + DocumentType association integrity
- [x] 8.2 OpenAPI — n/a (no endpoint contract change)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) Fixed entry for #801
- [x] 8.4 Archive superseded documents — n/a
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — as capacity allows / CI

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #801`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/fix-801-submitted-document-mapping-69d3`
- [x] 10.2 Open draft PR `[#801] fix(jpa): SubmittedDocument DocumentType mapping and getDto null-guard` with `Closes #801` — #1195
- [ ] 10.3 Wait for required workflows — coordinator watches heavy CI
- [ ] 10.4 Gate 4 — CI green, code review, no conflicts — coordinator merges
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — coordinator; do not merge from this agent
- [ ] 11.2 Confirm the CD pipeline published the image — coordinator
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test on target environment after merge
- [ ] 12.2 Verify rollback path still available
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-801-submitted-document-mapping`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression (`mvn verify -pl backend-api`); E2E n/a
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes — n/a no UI surface
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4) — draft #1195
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
