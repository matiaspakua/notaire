> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1618 exists, is labelled, and links to CU78
- [x] 1.2 CU78 exists; its permanent security controls will be corrected in this change
- [x] 1.3 Acceptance Criteria are the scenarios in the three delta specs
- [x] 1.4 Impact analysis identifies backend-api, frontend, security, and CD
- [x] 1.5 ADR not required; the existing BFF/Spring Security architecture is preserved
- [x] 1.6 Issue #1618 carries `in-progress`

## 2. Crear branch

- [x] 2.1 Updated local `main` from `origin/main`
- [x] 2.2 Created `fix/1618_code-scanning-remediation`
- [x] 2.3 Recorded the branch in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Write test cases for valid/missing CSRF, explicit CD permissions,
  literal-regex metacharacters, and planted SAST violations
- [ ] 3.2 Add the security policy, frontend API-client, and Playwright tests
- [ ] 3.3 Add Spring Security integration coverage for rejected and accepted writes
- [ ] 3.4 Run the new tests and observe the CSRF/policy/escaping assertions fail
- [ ] 3.5 Map every delta-spec scenario in `traceability.md`

## 4. Implementación

- [ ] 4.1 Configure cookie-based CSRF protection for the three Spring Security
  chains and preserve Bearer-token clients
- [ ] 4.2 Send the issued token on unsafe browser calls through the BFF and
  explicitly allow its header in CORS
- [ ] 4.3 Apply explicit read-only permissions to the three affected CD jobs
- [ ] 4.4 Quote dynamic URL text before constructing the test regular expression
- [ ] 4.5 Add narrow Semgrep/policy rules and fixtures for the three alert classes
- [ ] 4.6 Correct the stale English-source assertion so the main process suite
  checks the implemented monthly-report filename

## 5. Actualizar tests existentes

- [ ] 5.1 Update `SecurityAndCorsConfigTest`, `TS-0044`, API-client tests, and
  security-case tests listed in `design.md`
- [ ] 5.2 Preserve existing assertions; correct only the stale filename expected
  by `docs/tests/test_found_and_report_locals_english.py`
- [ ] 5.3 No tests are expected to become obsolete

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 Run `python3 security/run_gates.py --profile local` and no new disabled
  tests or unjustified skips

## 7. Ejecutar Playwright

- [ ] 7.1 Update `TS-0044-security-csrf-cors-qa.spec.ts` for issued-token,
  valid-write, and missing-token paths
- [ ] 7.2 `cd testing/e2e && npx playwright test TS-0044-security-csrf-cors-qa.spec.ts`
- [ ] 7.3 Verify the login/write flow at 320px, 768px, and 1024px
- [ ] 7.4 Run the full E2E suite because the shared API client changes

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update CU78, input-validation, API-authentication, cybersecurity-program,
  and security-tools docs named in `proposal.md`
- [ ] 8.2 No endpoint contract changes; verify no OpenAPI change is required
- [ ] 8.3 `CHANGELOG.md` is n/a because there is no user-facing feature
- [ ] 8.4 No permanent document is superseded or archived
- [ ] 8.5 Confirm permanent docs link rather than duplicate the threat controls
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --full`

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin <branch-name>`
- [ ] 10.2 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

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

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

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
