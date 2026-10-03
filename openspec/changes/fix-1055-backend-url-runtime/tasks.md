> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1055 OPEN; CU78; FRONTEND / DEVOPS / priority:medium / security / audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU78 exists; update permanent notes at implement for public URL non-disclosure
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a new ADR (follow ADR-005 Route Handler BFF); cite in docs update
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1055 --add-label "in-progress"`) — attempted; label ACL 403 for integration (expected)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1056** (or coordinator schedule); base includes #1051 (`c2c34de8`); no login/auth Playwright-heavy PR in flight
- [x] 2.2 `git checkout -b cursor/fix-1055-backend-url-runtime-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1055/` into `openspec/changes/fix-1055-backend-url-runtime/` and run `bash scripts/validate-sdlc-plan.sh fix-1055-backend-url-runtime`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the delta spec (runtime env proxy, fail-safe missing env, multi-env image, login leak removal, Cookie/Set-Cookie forward, Auth E2E, edge `/api/` skip)
- [x] 3.2 Write failing frontend unit tests for request-time upstream resolution + Cookie/Set-Cookie forwarding (proxy helper)
- [x] 3.3 Write failing assert that login page does not render Backend URL / internal `backend:8080` disclosure
- [x] 3.4 Add/adjust Playwright expectation for clean `/login` (or document extending existing security/login spec)
- [x] 3.5 Run new unit/source asserts and **observe them fail** on pre-change tree
- [x] 3.6 Confirm every `#### Scenario:` in the delta spec maps to at least one test (unit, Playwright, or existing edge skip coverage)

## 4. Implementación

- [x] 4.1 Add App Router catch-all Route Handler `frontend/src/app/api/v1/[...path]/route.ts` (Node runtime) + SRP helper for upstream URL + header/body forward
- [x] 4.2 Remove build-time `/api/v1` `rewrites()` destination bake from `next.config.ts` (no dual proxy)
- [x] 4.3 Wire runtime `BACKEND_URL` in Dockerfile/compose/cloud/prod compose; stop baking Docker-internal hosts via `NEXT_PUBLIC_API_URL` build args
- [x] 4.4 Remove Backend URL disclosure from `login/page.tsx`
- [x] 4.5 Preserve #1051 behavior: relative `/api/v1`, `credentials: 'include'`, Cookie/Set-Cookie relay; no localStorage JWT credential
- [x] 4.6 Confirm edge interceptor (middleware or post-#1056 `proxy.ts`) still skips `/api/**`
- [x] 4.7 Keep api-client comments accurate (BFF = Route Handler, not build rewrite)

## 5. Actualizar tests existentes

- [x] 5.1 Identify unit/E2E tests that assume `next.config` rewrites or login Backend URL text
- [x] 5.2 Update them without weakening auth/security assertions (#1051 cookie path)
- [x] 5.3 Remove tests made genuinely obsolete (rewrite-only assumptions), stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — expect unchanged green (no backend edits)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a delta; keep ratchet if full verify run
- [x] 6.3 `cd frontend && npm test && npm run lint && npm run typecheck && npm run build`
- [x] 6.4 Bruno/HTTP suite — n/a contract change on backend; note frontend-origin smoke in PR
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Run login/auth-related E2E plus new/updated `/login` URL non-disclosure assertion
- [ ] 7.2 PR must pass `playwright-e2e.yml` via heavy CI gate
- [ ] 7.3 Verify login at 320px / 768px / 1024px if layout copy changes
- [ ] 7.4 Do **not** mark Playwright n/a — UI + auth path touched; serialize vs other Auth E2E PRs

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no backend endpoint change)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) security/fix entry for runtime backend URL + login leak removed
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (frontend-focused path as applicable)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #1055`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1055-backend-url-runtime-69d3`
- [ ] 10.2 Open the PR titled `[#1055] fix(frontend): runtime backend URL proxy and remove login URL leak`, referencing Issue and CU78
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline published artifacts as applicable (frontend image)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `/login` shows no backend URL; login sets HttpOnly cookie; API via `/api/v1` works; runtime `BACKEND_URL` retarget without rebuild where feasible
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1055-backend-url-runtime`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU78)
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
