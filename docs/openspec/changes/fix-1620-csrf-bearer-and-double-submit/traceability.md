# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1620 | in-progress (OPEN, `priority:critical`, `security`, milestone 2026-11) |
| Use Case | CU78 – Security and Compliance | exists |
| Specification | `docs/openspec/changes/fix-1620-csrf-bearer-and-double-submit/` | written |
| Branch | `fix/1620_csrf-bearer-exemption` | created |
| Tasks | `tasks.md` | 34/42 complete (gates 4-5 open) |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A safe request issues the token cookie | `CsrfProtectionIntegrationTest#browserCookieDoubleSubmitShouldBeAccepted` (step 1) | passing |
| Login returns the token cookie beside the session cookie | `CsrfProtectionIntegrationTest#loginShouldIssueTheXsrfCookieBesideTheSessionCookie` | passing |
| The nonce stays stable while the session is unchanged | `CsrfProtectionIntegrationTest#authenticatedRequestShouldNotRotateTheXsrfCookie` | passing |
| Browser double-submit is accepted | `CsrfProtectionIntegrationTest#browserCookieDoubleSubmitShouldBeAccepted`; `TS-0044` "browser double-submit — the XSRF-TOKEN cookie echoed as X-XSRF-TOKEN is accepted" | passing |
| Cookie-authenticated request without the header is rejected | `CsrfProtectionIntegrationTest#cookieAuthenticatedRequestWithoutXsrfHeaderShouldBeRejected`; `TS-0044` "state-changing cookie request without X-XSRF-TOKEN is rejected with 403" | passing |
| Bearer-token write succeeds without an XSRF token | `CsrfProtectionIntegrationTest#bearerTokenRequestShouldBeExemptFromCsrf`; `TS-0044` "Bearer-token client is exempt from CSRF" | passing |
| The exemption does not weaken the cookie flow | `CsrfProtectionIntegrationTest#cookieAuthenticatedRequestWithoutXsrfHeaderShouldBeRejected` | passing |
| Logout succeeds without a CSRF token | `CsrfProtectionIntegrationTest#loginEndpointShouldBeExemptFromCsrf`; `RbacIntegrationTest`, `TS-0006` logout coverage | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | pending |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | yes | pending |
| `testing/e2e/tests/TS-0044-security-csrf-cors-qa.spec.ts` | yes | pending |
| `testing/e2e/tests/TS-0094-admin-route-guard.spec.ts` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `workspace/sdlc/validate-sdlc-plan.sh fix-1620-csrf-bearer-and-double-submit` |
| 2 | Failing tests written, test cases designed | yes | `CsrfProtectionIntegrationTest` observed failing before each fix (3 failures with the fix reverted) |
| 3 | Suite green, coverage held, docs updated | yes | backend `Tests run: 2356, Failures: 0`; `TS-0044` 7/7 locally; docs corrected |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

No exception was taken. The pre-existing Gate-1 backlog of 32 stale changes
(issue #1265) is unrelated to this change and is reported rather than repaired here.

## Environment caveats

- The full `validate-sdlc-plan.sh` run (no argument) stays red on `main` because
  32 pre-existing changes reference CLOSED issues (#1265); the scoped run for this
  change is the one used as Gate 1 evidence.
- The local backend suite ran with the sandbox Postgres container; the E2E stack
  (Postgres + backend jar + built frontend) was started locally to verify
  `TS-0044`.
