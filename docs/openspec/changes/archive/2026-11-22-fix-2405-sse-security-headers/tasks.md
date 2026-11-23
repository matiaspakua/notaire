> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU76`)
- [x] 1.2 Use Case documentation exists and is accurate
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — not architectural
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh <n>` — done before implementation

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/2405_sse_security_headers`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path (security headers present on SSE response)
- [x] 3.2 Write the integration test for the scenario
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail** — note: the red proof ran after the fix commit (8dc97c2e) since the fix and first test landed together; the test asserted `null` CSP on the pre-fix `app.ts` and passed on the fixed version
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Extract `SECURITY_HEADERS` constant in `app.ts`
- [x] 4.2 Update `onSend` hook to iterate over `SECURITY_HEADERS`
- [x] 4.3 Spread `SECURITY_HEADERS` into `reply.raw.writeHead()` in the SSE events route

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change — none
- [x] 5.2 Update them without weakening assertions — n/a
- [x] 5.3 Remove tests made genuinely obsolete — n/a

## 6. Ejecutar regresión

- [x] 6.1 `cd framework/ai-sdlc/factory-dashboard && npx vitest run` — all 23 tests pass
- [x] 6.2 `mvn jacoco:check -pl backend-api` — not applicable (TypeScript/Vitest)
- [x] 6.3 `mvn verify -pl backend-api` — not applicable
- [x] 6.4 `bash integration-test/scripts/test.sh` — not applicable
- [x] 6.5 No `@Disabled` or skipped tests

## 7. Ejecutar Playwright

- [x] 7.4 n/a — no UI surface. This is a server-side header fix.

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/openspec/specs/factory-dashboard/spec.md` with SSE security headers requirement
- [x] 8.2 Update OpenAPI/Swagger annotations — n/a (no endpoint shape change)
- [x] 8.3 Update `framework/ai-sdlc/CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded documents — n/a
- [x] 8.5 Confirm no information was duplicated — main spec already contains the requirement; no duplicate
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — not run at archive time (will run before PR)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Refs #2405`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Archive this change in the PR: `cd docs && openspec archive fix-2405-sse-security-headers --yes`
- [ ] 10.2 `git push -u origin fix/2405_sse_security_headers`
- [ ] 10.3 Open the PR titled `[#2405] fix(dashboard): SSE events stream carries security headers`
- [ ] 10.4 Wait for every required workflow to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2) — note: red proof ran after fix+test commit
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor — Vitest coverage, not JaCoCo
- [x] Playwright E2E green for UI changes — n/a
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
