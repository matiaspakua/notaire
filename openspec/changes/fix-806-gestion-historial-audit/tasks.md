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
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 806 --add-label "in-progress"`) — attempted; GraphQL label ACL 403 for integration (recorded)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/fix-806-gestion-historial-audit-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: orphan writes + estado-actual fallback (happy / edge / 404)
- [x] 3.2 Write unit tests if a pure helper is extracted — n/a; reuse `ManagementBitacoraServiceTest`
- [x] 3.3 Write `ManagementHistorialOrphanWriteIntegrationTest` for all delta scenarios
- [x] 3.4 Run them and **observe them fail** — 5 failures / 10 tests before implementation
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 On plain POST create: after save, if status present, `managementBitacoraService.registerStatus`
- [x] 4.2 On plain PUT update: capture previous status id; after save, register only when status id changed (including first set)
- [x] 4.3 On PUT complete-case update: same previous-vs-new status comparison + registerStatus
- [x] 4.4 Implement `estado-actual` entity-status fallback (History empty → synthesize; missing entity / null status → 404)
- [x] 4.5 Translate Spanish comments/strings in touched controller/service code to English (keep path segments)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected (bitácora / management controller IT)
- [x] 5.2 Update them without weakening assertions — no expectation changes required
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 Bruno/HTTP suite — no Bruno asserts 404-on-empty estado-actual; COVERAGE.md note updated
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Confirm TS-0028 still covers CU13 bitácora UI (`useHistorial` + dialog on gestiones page)
- [ ] 7.2 Run focused Playwright when stack available; otherwise rely on CI `playwright-e2e.yml`
- [x] 7.3 Viewports already covered by TS-0028
- [x] 7.4 No new UI surface — confirmation only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update CU13 use case for orphan writes + estado-actual fallback
- [x] 8.2 Update OpenAPI summaries on touched endpoints
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) Fixed entry
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — n/a
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (as capacity allows)

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #806`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-806-gestion-historial-audit-69d3`
- [ ] 10.2 Open draft PR `[#806] fix(api): gestion historial on orphan status writes` with `Closes #806`
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
- [ ] 12.4 Archive the change: `openspec archive fix-806-gestion-historial-audit`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
