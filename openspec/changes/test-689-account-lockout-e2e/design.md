> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #689, Use Case CU78. Backend behaviour exists (#560); the stack runs with the defaults of 5 attempts and 15 minutes. The login page maps HTTP 429 to the `lockoutError` toast.

## Goals / Non-Goals

**Goals:** E2E proof of the lockout on two widths, isolated from real accounts.
**Non-Goals:** testing lockout expiry (15 minutes; covered by the backend unit test with an injectable clock), changing the thresholds.

## Decisions

1. The test locks a throwaway username (`lockout-<timestamp>-<viewport>`): the backend records failures for unknown usernames too, so no real account is touched. Rejected: locking `admin` (would break every later spec for 15 minutes), lowering the threshold through configuration (changes the stack under test).
2. The test loops up to `maxAttempts + 1` submissions and stops when the lockout toast appears, so it does not hard-code the default of 5.
3. The "locked account" scenario is asserted on the throwaway username: once locked, the backend answers 429 before it looks at the password, so any further attempt, whatever the password, still shows the lockout. Rejected: creating a second real user just to test a correct password (needs admin API setup for no extra signal).

## Riesgos / Trade-offs

- The toast text is localized; the test matches both Spanish and English wording.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Lockout message on desktop | e2e | `TS-0100-login-account-lockout.spec.ts` |
| Lockout message on mobile | e2e | `TS-0100-login-account-lockout.spec.ts` |
| A locked account stays locked | e2e | `TS-0100-login-account-lockout.spec.ts` |

- Coverage impact: none (E2E)

## Regression Strategy

- Full suite command: `cd testing/e2e && npx playwright test`; `bash scripts/run_pipeline.sh`
- The other login specs must stay green (they use `admin`, never locked)

## Playwright Strategy

This change is the Playwright spec: golden path (lockout), edge (mobile width), error path (locked account stays locked).

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): the Playwright job is green in CI

## Rollback Strategy

- Revert the PR.
