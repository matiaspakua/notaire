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
| Tasks | `tasks.md` | 52/69 complete (groups 10-12 are post-PR/merge) |
| Commits | `36419e6b`, `3bee6450`, `841d1b33`, `04fc5ec5`, `bbfb96ff`, `ce73b1e3`, `481d95c7`, `ec90b565`, `5dbe1787` + this ledger commit | local (unpushed) |
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
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | `04fc5ec5` |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | yes | `04fc5ec5`, `481d95c7` |
| `testing/e2e/tests/TS-0044-security-csrf-cors-qa.spec.ts` | yes | `3bee6450` |
| `testing/e2e/tests/TS-0094-admin-route-guard.spec.ts` | yes | `ce73b1e3` |
| `testing/e2e/tests/TS-0041-responsive-viewport-qa.spec.ts` | yes | `ec90b565` |
| `testing/e2e/tests/TS-0152-workflow-list-view.spec.ts` | yes | `ec90b565` |
| `CHANGELOG.md` | yes | `04fc5ec5` |
| `docs/tests/test_change_design_english.py` | yes | `5dbe1787` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `workspace/sdlc/validate-sdlc-plan.sh fix-1620-csrf-bearer-and-double-submit` |
| 2 | Failing tests written, test cases designed | yes | `CsrfProtectionIntegrationTest` observed failing before each fix (3 failures with the fix reverted) |
| 3 | Suite green, coverage held, docs updated | yes | backend suite green at HEAD; `TS-0044` 7/7; `TS-0041` and `TS-0152` green locally; docs corrected |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

No exception was taken. The Gate-1 backlog of 32 stale changes that used to keep
the full `validate-sdlc-plan.sh` red on `main` (issue #1265) was archived on
`main` by #1626 (`98bcb609`) while this branch was open; it is rebased onto that
commit, so the full validator no longer reports the backlog.

## Environment caveats

- This branch is rebased onto `main` at `537f1d46`, which already carried two
  unrelated fixes merged while it was open: #1626 (`98bcb609`) archived the #1265
  Gate-1 backlog and #1625 (`537f1d46`) restored the pinned JDK 26 images. The
  full `validate-sdlc-plan.sh` is therefore no longer red for that backlog, but it
  still reports exactly one problem, and it is not this change: the change
  directory `fix-1621-jdk26-pins-dependabot-prefix`, added by #1625 itself, is left
  unarchived while its issue (#1621) is CLOSED. Archiving another team's
  just-merged change is out of scope for a security remediation and is reported
  instead; the scoped run for this change
  (`validate-sdlc-plan.sh fix-1620-csrf-bearer-and-double-submit`) is green and is
  the Gate 1 evidence.
- The local backend suite ran with the sandbox Postgres container; the E2E stack
  (Postgres + backend jar + built frontend) was started locally to verify
  `TS-0044`.
- `markdown-lint` (a local-only gate, not in CI) ratchets per changed file, so
  touching `CHANGELOG.md` — Constitution-mandated for a user-visible change —
  drags in the legacy block's pre-existing `MD012`/`MD004` findings, plus one
  real duplicate `## [Unreleased]` heading (`MD024`) left by the pre-release-please
  hand-maintained log. All markdown authored by this change is clean; the 53
  legacy findings are reported, not reformatted, here: `MD004` conflicts with
  release-please's own `*` bullet output, so correcting it is a gate-config
  decision, not part of a security remediation.
- A third repo-wide red was repaired here (`5dbe1787`): #1626 archived the 32
  shipped changes but left `docs/tests/test_change_design_english.py` reading the
  removed active directory of `feat-1362-procedure-template-admin`, so the blocking
  `Process script and harness self-tests` step failed for every PR on `main` with
  `FileNotFoundError`. The shipped design keeps its English-naming ratchet by
  reading the archive path, following the `test_retired_*_english.py` precedent.
- Two full-suite E2E reds were repaired (`ec90b565`), not caused by this change:
  `TS-0152` only became reachable once the Bearer CSRF exemption let its API
  calls through (on `main` it dies earlier at the 403), and `TS-0041` fails on
  `main` regardless; both reproduce deterministically and are classified as
  pre-existing UI/test issues (hover-paused full-width login toast; Chromium 153
  not running the native-select default action for synthesized arrow keys),
  addressed with test-level fixes so the `playwright-e2e.yml` gate can go green.
