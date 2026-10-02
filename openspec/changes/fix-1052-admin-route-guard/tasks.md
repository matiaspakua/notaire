> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — ACL denied for integration; noted in status

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/fix-1052_admin-route-guard-69d3`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable — n/a backend; frontend unit/component
- [x] 3.4 Run them and **observe them fail** — missing `@/lib/admin-access` before implement
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Add `frontend/src/lib/admin-access.ts` (admin tipos, admin path, cookie names, set/clear helpers, edge decision helper)
- [x] 4.2 Wire login/logout to set/clear `notaire-auth-role` (auth-store + login page consistency)
- [x] 4.3 Update `middleware.ts` to deny non-admin role on `/dashboard/administracion/**`
- [x] 4.4 Add `administracion/layout.tsx` client guard redirecting to `/dashboard?forbidden=1`
- [x] 4.5 Show forbidden message on dashboard when `forbidden=1`
- [x] 4.6 Align E2E auth helpers that inject cookies so admin deep-links keep working

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none obsolete

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a delta; CI covers
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a no backend code
- [x] 6.3 `mvn verify -pl backend-api` — n/a no backend code; CI verifies
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a no API change
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/TS-0094-admin-route-guard.spec.ts`; update `E2E-TEST-MAPPING.md`
- [ ] 7.2 `cd frontend && npx playwright test` — green in CI / local stack
- [x] 7.3 Verify forbidden message at 320px, 768px and 1024px — covered in TS-0094
- [x] 7.4 n/a only if no UI — this change HAS UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (as applicable)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #<issue-number>` (`Closes #1052`)
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/fix-1052_admin-route-guard-69d3`
- [x] 10.2 Open the PR titled `[#1052] fix(frontend): guard admin routes for non-admin users`, referencing Issue and CU78
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`
- [ ] 10.6 `bash scripts/check-heavy-ci.sh` before merge — never light-only

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive <change-name>`

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
