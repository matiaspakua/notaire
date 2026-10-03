> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1051 OPEN; CU78 + CU84
- [x] 1.2 Use Case documentation exists and is accurate — CU78 / CU84 exist; update session/CSP notes at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta specs
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — optional short ADR if cookie+BFF vs rewrite needs lasting record; otherwise document in CU78/architecture notes
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1051 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1048→#1047→#1044** and no Playwright-heavy PR in flight
- [x] 2.2 `git checkout -b cursor/fix-1051-httponly-jwt-csp-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1051/` into `openspec/changes/fix-1051-httponly-jwt-csp/` and run `bash scripts/validate-sdlc-plan.sh fix-1051-httponly-jwt-csp`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from both delta specs (cookie login/logout/filter/proxy/localStorage; CSP no-eval/nonce; E2E login/logout)
- [x] 3.2 Write failing backend tests: login Set-Cookie attributes; logout clears cookie; filter accepts cookie without Bearer; Bearer still works
- [x] 3.3 Write failing frontend unit tests: auth-store does not persist token; api-client does not read JWT from localStorage; uses credentials/include for browser calls
- [x] 3.4 Write failing CSP/config asserts: production headers lack `unsafe-eval`; script-src includes nonce
- [x] 3.5 Run them and **observe them fail** on pre-change tree
- [x] 3.6 Confirm every `#### Scenario:` in both delta specs maps to at least one test

## 4. Implementación

- [x] 4.1 Backend: issue HttpOnly/SameSite/(Secure) cookie on successful login; clear on logout endpoint
- [x] 4.2 Backend: extend `JwtAuthenticationFilter` to read cookie OR Bearer
- [x] 4.3 Frontend proxy: ensure Cookie forward + Set-Cookie surfacing (rewrite verify or Route Handler BFF)
- [x] 4.4 Frontend: remove JWT from Zustand persist; login/logout wire cookie session; `api-client` credentials path
- [x] 4.5 Keep #1052 UX cookies (`notaire-auth-status` / `notaire-auth-role`) working with login/logout
- [x] 4.6 Production CSP: nonce-based `script-src`; remove `unsafe-eval` (dev-only exception documented if required for HMR)
- [x] 4.7 Update E2E auth helpers and TS-0002 / TS-0044 / TS-0093 (and any localStorage JWT injectors)
- [x] 4.8 Review CSRF posture docs/tests for cookie-era reality (SameSite + CORS; no stale “no auth cookie” claims)

## 5. Actualizar tests existentes

- [ ] 5.1 Identify backend login/security tests and frontend api-client/auth-store/session tests affected
- [ ] 5.2 Update assertions without weakening security guarantees
- [ ] 5.3 Remove tests made genuinely obsolete (e.g. localStorage bearer attachment as the only path), stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api`
- [ ] 6.2 `mvn jacoco:check -pl backend-api`
- [ ] 6.3 `mvn verify -pl backend-api`
- [ ] 6.4 `bash integration-test/scripts/test.sh` or Bruno/Bearer suite — Bearer path must stay green
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Update/add E2E for login cookie, logout clear, no localStorage JWT, CSP-related smoke as feasible
- [ ] 7.2 PR must pass full `playwright-e2e.yml` via heavy CI gate
- [ ] 7.3 Viewport checks per existing suite norms when UI auth chrome changes
- [ ] 7.4 Do **not** mark Playwright n/a — this change is Playwright-heavy

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger if login/logout cookie behavior is documented there
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for HttpOnly JWT + CSP hardening
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1051`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1051-httponly-jwt-csp-a383`
- [ ] 10.2 Open the PR titled `[#1051] security(frontend): HttpOnly JWT cookie + CSP nonce`, referencing Issue, CU78, CU84
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: UI login → HttpOnly cookie present, no JWT in localStorage; production CSP without `unsafe-eval`; logout clears cookie
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1051-httponly-jwt-csp`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU78, CU84)
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
