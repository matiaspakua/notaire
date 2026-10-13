> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU78` / `#1620`, `priority:critical`, `security`, `in-progress`)
- [x] 1.2 Use Case documentation exists and is accurate — CU78 covers the CSRF/CORS posture, no edit needed
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/csrf-browser-double-submit/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR not required — defect fix inside the #1618 design, no new architectural decision (recorded in `design.md` Decisions)
- [x] 1.6 Issue is IN PROGRESS (`in-progress` label present)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/1620_csrf-bearer-exemption`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: double-submit accepted, header missing, Bearer exempt, safe-request cookie issuance, login cookie delivery, nonce stability
- [x] 3.2 Write the unit tests for every scenario in the delta spec (`UserControllerHashTest` updated for the new `login` signature)
- [x] 3.3 Write the integration tests — `CsrfProtectionIntegrationTest` (7 tests against the real filter chain)
- [x] 3.4 Run them and **observe them fail** — reverted fix: `Tests run: 6, Failures: 3` (`bearerTokenRequestShouldBeExemptFromCsrf` 403 instead of 201); cookie/rotation cases: `Failures: 1` each before their fix
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test (see `traceability.md` → Requirement coverage)

## 4. Implementación

- [x] 4.1 Exempt Bearer-token requests from CSRF (`ignoringRequestMatchers` header matcher) in `SecurityAndCorsConfig`
- [x] 4.2 Resolve a header-supplied `X-XSRF-TOKEN` verbatim while keeping the XOR mask for server-rendered tokens — via `CsrfConfigurer.spa()` (Spring Security 7's shipped version of the reference recipe)
- [x] 4.3 Materialize the deferred token before the chain in `CsrfCookieFilter` so the cookie survives committed/large responses (`apiGetBytes` PDFs)
- [x] 4.4 Stop rotating the nonce on every authenticated request (`sessionAuthenticationStrategy` no-op + rationale in the config Javadoc)
- [x] 4.5 Append the session cookie in `UserController#login` / `#logout` with `HttpServletResponse.addHeader` instead of the `ResponseEntity` header map (which replaces every `Set-Cookie`)
- [x] 4.6 Keep the `login`/`logout` CSRF exemption aligned with their `permitAll` authorization

## 5. Actualizar tests existentes

- [x] 5.1 Identify affected tests: `RbacIntegrationTest`, `JwtAuthIntegrationTest`, `HistoryWriteAuthorizationIntegrationTest`, `CsrfProtectionIntegrationTest`, `UserControllerHashTest`, `TS-0044`, `TS-0094`
- [x] 5.2 Update them without weakening assertions — the four `MockMvc` classes now authenticate with real cookies/Bearer instead of the permissive `csrf()` post-processor; no expected status was relaxed
- [x] 5.3 Reason documented: `SecurityMockMvcRequestPostProcessors.csrf()` reflectively swaps the shared `CsrfFilter` token repository, disabling enforcement for the rest of the JVM and hiding exactly this regression
- [x] 5.4 Restore the masked intent of `TS-0094`: its cookie-mode catalog write now echoes `X-XSRF-TOKEN`, so the expected 403 is an RBAC denial instead of the CSRF filter answering first (it kept passing for the wrong reason)
- [x] 5.5 Adapt `JwtAuthIntegrationTest` (3 cases) to a multi-cookie response: it read only the *first* `Set-Cookie` header, which is now `XSRF-TOKEN`; the assertions match the session cookie among all headers instead of weakening them

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — `Tests run: 2356, Failures: 0, Errors: 0`
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor honoured
- [x] 6.3 `mvn verify -pl backend-api` — Checkstyle, SpotBugs, formatting
- [ ] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite (runs in the PR's `playwright-e2e.yml` job, which executes the live stack)
- [x] 6.5 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 Update `testing/e2e/tests/TS-0044-security-csrf-cors-qa.spec.ts` — corrected header comment + 3 CSRF regression tests
- [x] 7.2 `npx playwright test TS-0044` — 7 passed locally against Postgres + backend + built frontend
- [x] 7.3 Responsive check — no UI surface changed; the spec's viewport coverage is unaffected
- [x] 7.4 `npx tsc --noEmit` and `npx eslint . --max-warnings=0` clean on `testing/e2e`
- [ ] 7.5 Full sharded suite green in CI (`playwright-e2e.yml`)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — no endpoint or payload changed, only response cookies
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 No superseded document needs archiving
- [x] 8.5 No information duplicated — permanent docs stay the single source of truth
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message references `#1620`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/1620_csrf-bearer-exemption`
- [ ] 10.2 Open the PR titled `[#1620] fix(security): restore CSRF protection for browser and Bearer clients`, referencing Issue and Use Case
- [ ] 10.3 Wait for every required workflow: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test the target environment: health endpoint, `POST /api/v1/users/login` returning both cookies, and a Bearer write without a CSRF header
- [ ] 12.2 Verify the rollback path is still available as described in `design.md` → Rollback Strategy
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1620-csrf-bearer-and-double-submit`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E (backend: 2356 tests; `TS-0044` verified locally)
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for the affected spec
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
