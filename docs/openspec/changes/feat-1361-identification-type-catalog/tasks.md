> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — RF-53 / CU78 cover admin catalogs and access control
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — label already present; agent comment may 403

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/1361-identification-type-catalog-69d3`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [ ] 3.2 Write Vitest for mutations + 409 delete toast (`identification-type-catalog.test.tsx`)
- [ ] 3.3 Write Playwright `TS-0127-identification-type-catalog.spec.ts` (CRUD + non-admin)
- [ ] 3.4 Run them and **observe them fail** on the pre-change build
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Extend `useIdentificationTypes` with create/update/delete mutations + invalidation
- [ ] 4.2 Add `administration/identification-types/page.tsx` (FormContainer pattern, data-testids)
- [ ] 4.3 Link module card on administration index; i18n es/en keys
- [ ] 4.4 Remove three bucket-C allowlist lines for identification-types POST/PUT/DELETE

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected (people identification-type Vitest, admin smoke if any)
- [ ] 5.2 Update them without weakening assertions
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected

## 6. Ejecutar regresión

- [ ] 6.1 `cd frontend && npm test` / Vitest affected files
- [ ] 6.2 `bash frontend/verify.sh` — typecheck + unit
- [ ] 6.3 Reachability: `python3 -m pytest contracts/tests/test_api_reachability.py` (or project wrapper)
- [ ] 6.4 Bruno identification-types smoke optional
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `TS-0127-identification-type-catalog.spec.ts` per design.md
- [ ] 7.2 `cd testing/e2e && npx playwright test TS-0127-identification-type-catalog` — all green
- [ ] 7.3 Verify the catalog at 390px and 1024px
- [ ] 7.4 Hold product PR until #1518 heavy-CI / CHANGELOG serialize clears (or foreman clears)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update E2E-TEST-MAPPING.md with TS-0127
- [ ] 8.2 OpenAPI unchanged unless annotations need a note — n/a expected
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) in the product PR only (not while #1518 holds the lock)
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — n/a
- [ ] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --fix` before product push

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Product commits end with `Closes #1361` (Gate 1 docs commits may use `Refs #1361`)
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/1361-identification-type-catalog-69d3`
- [ ] 10.2 Open the PR titled `[#1361] …`, referencing Issue and Use Case
- [ ] 10.3 Wait for every required workflow to pass
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: admin CRUD one throwaway identification type on target env
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-1361-identification-type-catalog`

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
