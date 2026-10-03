> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 953 --add-label "in-progress"`) — defer until implement

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after queue ahead clears
- [x] 2.2 `git checkout -b cursor/test-953-bruno-zero-coverage-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-953/` into `openspec/changes/test-953-bruno-zero-coverage/` and run `bash scripts/validate-sdlc-plan.sh test-953-bruno-zero-coverage`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases per controller: ops exposed, happy path, delete/404 or action errors where applicable
- [x] 3.2 Author Bruno YAML with chai assertions first (observe missing endpoint/folder failures)
- [x] 3.3 Integration via Bruno against running API — n/a separate JUnit unless product fix needs it
- [x] 3.4 Run `bru run` on the new folder(s) and **observe fail** before completing fixtures
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one Bruno/docs check

## 4. Implementación

- [x] 4.1 Cluster A — Bruno folders: roles; workflow-definitions; workflow-nodes; workflow-transitions; workflow-validation
- [x] 4.2 Cluster B — copies; submitted-documents; testimonies; testimony-movements
- [x] 4.3 Cluster C — notebooks; auxiliary-protocol; procedure-folders; document-cost-templates
- [x] 4.4 Cluster D — registration-drafts (minutas); gestiones core CRUD/actions; reportes representative PDFs
- [x] 4.5 Ensure fixtures/teardown unique keys; reuse `00-auth` token; avoid colliding with `history`/`people` vars
- [x] 4.6 If a trivial product defect blocks green lifecycle, fix minimally or open follow-up Issue and document

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing Bruno folders affected by shared vars/fixtures
- [x] 5.2 Update them without weakening assertions
- [x] 5.3 Remove obsolete stubs if any, stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — if Java product code changed; else sanity
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — if Java product code changed
- [ ] 6.3 `mvn verify -pl backend-api` — if Java product code changed
- [x] 6.4 `cd backend-api/api-test && bru run . -r --env Development` — **required**; run twice for idempotence
- [x] 6.5 No `@Disabled` or skipped Bruno cases without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no Playwright spec edits required
- [ ] 7.2 PR must still pass required workflows including Playwright job if triggered
- [x] 7.3 n/a responsive UI checks
- [ ] 7.4 Do not skip heavy CI; serialize only if runner contention requires it

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `COVERAGE.md`, `TEST-PLAN.md` §7, `CU-API-MATRIX.csv` per proposal.md
- [ ] 8.2 OpenAPI/Swagger — n/a unless a product fix changes annotations
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) test-infra entry
- [ ] 8.4 Archive superseded documents — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain SSOT
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units (prefer one cluster per commit), Conventional Commits
- [ ] 9.2 Every commit message ends with `Closes #953` (or final commit if multi-commit policy prefers last-only — still reference #953)
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/test-953-bruno-zero-coverage-69d3`
- [ ] 10.2 Open the PR titled `[#953] test(api): Bruno coverage for zero-coverage controllers`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass including Bruno
- [ ] 10.4 Gate 4 — CI green, review approved; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm CD as applicable (no app behavior change expected)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: Bruno suite green on CI; COVERAGE TODO cleared for the 16
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive test-953-bruno-zero-coverage`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU76)
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a product UI; CI job still green)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
