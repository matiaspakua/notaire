> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1056 OPEN; CU84; FRONTEND / priority:medium / tech-debt / audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU84 exists; update middleware→proxy naming at implement if cited
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (convention rename)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1056 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — base `0607cf0a` (#1045 merged; #1051 already on main with CSP nonce in edge file)
- [x] 2.2 `git checkout -b cursor/chore-1056-middleware-to-proxy-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1056/` into `openspec/changes/chore-1056-middleware-to-proxy/` and run `bash scripts/validate-sdlc-plan.sh chore-1056-middleware-to-proxy`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the delta spec (proxy file/export, middleware removed, redirects, admin deny, `/api/` skip, build warning gone, Auth E2E, HttpOnly non-regression)
- [x] 3.2 Write failing frontend asserts: expect `proxy.ts` + `export function proxy`; expect no `middleware.ts` (fail on pre-change tree)
- [x] 3.3 Add/adjust unit coverage for edge redirect matrix (import `proxy` with mocked `NextRequest` if stable) — keep `admin-access` tests as decision core
- [x] 3.4 Capture pre-change `npm run build` log showing middleware deprecation (documents red for warning AC)
- [x] 3.5 Run new file/export asserts and **observe them fail** on pre-change tree
- [x] 3.6 Confirm every `#### Scenario:` in the delta spec maps to at least one test (unit, build-log, or Playwright)

## 4. Implementación

- [x] 4.1 From `frontend/`, run `npx @next/codemod middleware-to-proxy` (confirm flags/workdir via dry-run)
- [x] 4.2 Verify `frontend/src/proxy.ts` exports `proxy`; remove any leftover `middleware.ts`
- [x] 4.3 Preserve route-guard logic: public paths, status/role cookies, admin deny, `/api/**` skip, matcher
- [x] 4.4 If #1051 already added CSP nonce (or cookie-related edge headers), ensure they survive the rename
- [x] 4.5 Grep-update comments/docs that refer to this file as “middleware” (do not rename zustand `persist` middleware wording)
- [x] 4.6 Confirm `npm run build` log has **no** middleware-convention deprecation warning
- [x] 4.7 Keep Auth E2E helpers compatible (UX cookies + HttpOnly path if present)

## 5. Actualizar tests existentes

- [x] 5.1 Identify frontend unit/E2E comments and asserts that name `middleware.ts` for route guards
- [x] 5.2 Update them without weakening auth/security assertions
- [x] 5.3 Remove tests made genuinely obsolete (unlikely — rename only), stating the reason — none obsolete

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no backend edits); frontend-only change
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a delta
- [x] 6.3 `cd frontend && npm test && npm run lint && npm run typecheck && npm run build`
- [x] 6.4 Bruno/HTTP suite — n/a (no API contract change); note in PR
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Run auth-related E2E (login/logout, admin guard, session helpers) and full suite required by heavy CI
- [ ] 7.2 PR must pass `playwright-e2e.yml` via heavy CI gate
- [x] 7.3 Viewport checks per existing suite norms if auth chrome changes (expect none)
- [x] 7.4 Do **not** mark Playwright n/a — AC requires Auth E2E green; Playwright-heavy serialize

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint change)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) chore entry for middleware→proxy
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (frontend-focused path as applicable)

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1056`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/chore-1056-middleware-to-proxy-e6f8`
- [ ] 10.2 Open the PR titled `[#1056] chore(frontend): migrate middleware.ts to proxy.ts`, referencing Issue and CU84
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline published artifacts as applicable (frontend image once #1043 exists)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: UI login/logout redirects; no middleware deprecation in build; after #1051, HttpOnly cookie API path still works
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive chore-1056-middleware-to-proxy`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU84)
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
