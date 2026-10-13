> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The backend authenticates browser traffic from an HttpOnly JWT cookie forwarded
by the same-origin Next BFF, while non-browser clients use Bearer tokens. The
current Spring configuration disables CSRF in all three filter chains; its
documentation incorrectly describes the API as cookie-less. The local SAST
runner invokes only two project rules and treats CodeQL as presence evidence,
not a pre-PR result. See `proposal.md` for motivation.

## Goals / Non-Goals

**Goals:**
- Protect state-changing cookie-authenticated browser calls without exposing the
  JWT or creating a server session.
- Keep Bearer-token tooling and the existing BFF route viable.
- Move detection of the exact alert classes into deterministic local tests.

**Non-Goals:**
- Change JWT lifecycle, roles, database state, or the API resource contract.
- Block all historic CodeQL findings or change Trivy/ZAP severity policy.

## Decisions

1. Use Spring Security's cookie-based double-submit CSRF token for `/api/**`
   and force issue/rotation through the existing BFF. This protects ambient
   cookie authentication while retaining stateless JWT verification. Rejected:
   relying on SameSite=Lax alone, because it is defense in depth and contradicts
   the cookie-auth trust boundary.
2. Keep CSRF enabled on actuator and default filter chains with Spring defaults.
   They do not have a browser JSON-write flow, so a custom exception list would
   add risk without a consumer need. Rejected: disabling only selected paths,
   which leaves the CodeQL finding and weakens the default posture.
3. Add narrow Semgrep fixtures/rules plus Python workflow-policy tests to catch
   known patterns locally. Rejected: relying only on CodeQL, whose asynchronous
   findings do not fail the current PR gate.
4. Give each affected CD job explicit read-only permissions, leaving the release
   job's pre-existing write scope in place. Rejected: a broad workflow-level
   write grant, which violates least privilege.

## Riesgos / Trade-offs

- [CSRF token not emitted by the BFF path] → add browser E2E coverage for token
  issuance, a valid write, and a missing-token rejection.
- [A Bearer client is unintentionally forced into a browser token flow] → limit
  the CSRF matcher to cookie-authenticated browser requests and cover the
  existing Bruno/Bearer suite.
- [Static rules become noisy] → use planted fixtures and narrow patterns; review
  false positives through the existing CU78 security exception process.
- [Old autofix branches conflict after the corrected remediation lands] → retain
  them as traceable alert evidence and update or supersede only with an explicit
  PR-body record.

## Testing Strategy

<!-- How the scenarios in the delta spec become tests. Every Acceptance Criterion
     (each `#### Scenario:`) must map to at least one test.
     TDD is mandatory (Constitution P1): tests are written and observed FAILING
     before implementation. -->

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Read-only CD jobs declare read-only permissions | Python unit | `security/tests/test_cd_workflow_permissions.py` |
| Release publication retains only its required write access | Python unit | `security/tests/test_cd_workflow_permissions.py` |
| URL text containing a regex metacharacter is matched literally | Playwright | `TS-0070-supervised-tour-tutorial.spec.ts` |
| Escaping preserves ordinary URL matching | Playwright | `TS-0070-supervised-tour-tutorial.spec.ts` |
| Unsafe cookie request without a CSRF token is rejected | Spring integration | new CSRF integration test |
| Same-origin browser write includes a valid CSRF token | Playwright | `TS-0044-security-csrf-cors-qa.spec.ts` |
| JWT remains unreadable to browser script | Playwright | `TS-0044-security-csrf-cors-qa.spec.ts` |
| Planted disabled CSRF configuration fails SAST | Python/Semgrep fixture | `security/tests/test_security_cases.py` |
| CD job without explicit permissions fails policy validation | Python unit | `security/tests/test_cd_workflow_permissions.py` |
| Dynamic regex construction without quoting fails SAST | Python/Semgrep fixture | `security/tests/test_security_cases.py` |

- New unit tests: frontend API-client token-header behavior and security-policy
  tests under `security/tests/`.
- New integration tests: a Spring `webAppContextSetup(...springSecurity())`
  test for rejected and accepted cookie-authenticated unsafe requests.
- Coverage impact: the backend test executes the filter configuration and must
  retain the JaCoCo ratchet floor (80% line / 65% branch).

## Regression Strategy

<!-- What existing behavior could this break, and how it is proven not to.
     Name the existing test classes that must be updated and why. Never weaken an
     assertion to force green - if an existing test now fails legitimately, say why
     the old expectation was wrong. -->

- Existing tests affected: `SecurityAndCorsConfigTest`, `TS-0044`, the API
  client unit tests, security-case tests, and the stale report-local English
  source assertion. The latter is corrected to the already implemented file
  name, not weakened.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: Bearer-based Bruno and Swing clients; both must remain
  outside the browser CSRF matcher.

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: `TS-0044-security-csrf-cors-qa.spec.ts`.
- Golden path covered: login issues a token and an authenticated unsafe request
  through the BFF succeeds.
- Edge / error paths covered: an authenticated unsafe request without the token
  is rejected; JWT remains script-inaccessible.
- Viewports: 320px / 768px / 1024px for the existing login flow.
- Command: `cd testing/e2e && npx playwright test TS-0044-security-csrf-cors-qa.spec.ts`.

## Deployment Strategy

<!-- How this reaches an environment. Order of operations matters when a schema
     migration and code change ship together. -->

- Flyway migration required: no.
- Deployment order / coupling: deploy backend and frontend together because the
  browser client must send the issued header when CSRF becomes enforced.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy: login through the BFF, perform one authorized write,
  and verify a token-less unsafe request is rejected.

## Rollback Strategy

<!-- How to undo this if the smoke test or production behavior fails.
     A revert is only a valid rollback if it is actually safe - state whether it is. -->

- Revert safe: yes for code/configuration, but it deliberately restores the
  cookie-CSRF exposure and therefore requires owner security approval.
- Database rollback: none needed.
- Data written under the new behavior after revert: none.
- Blast radius if rollback is delayed: browser writes fail if frontend and
  backend versions are split; deploy them together and validate immediately.

## Migration Plan

1. Land test and policy evidence before the configuration change.
2. Deploy the coupled frontend/backend artifact.
3. Run the browser write and missing-token smoke checks.

## Open Questions

None. The existing same-origin BFF and browser-cookie transport resolve the
required token propagation path.
