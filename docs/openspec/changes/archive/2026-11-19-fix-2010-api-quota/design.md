> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

SR-15 was open: expensive API routes had no quota. Login lockout is per username and lives in the shared database. A general limiter that is wrong if it pretends to be distributed.

## Goals / Non-Goals

**Goals:**

- A burst over the ceiling returns 429.
- A call in the next window is allowed.
- The threat model says the counter is per JVM.

**Non-Goals:**

- Redis or another shared counter.
- Replacing `LoginAttemptService`.

## Decisions

Fixed window, in memory. Key is the authenticated name after the JWT filter, otherwise `HttpServletRequest.getRemoteAddr()`. Default 2000 requests / 60 seconds so the local suite from one address stays under the ceiling. Operators tighten it with `NOTAIRE_API_RATE_LIMIT_MAX`.

## Riesgos / Trade-offs

- [One replica does not see another's count] → SR-15 stays Partial and #2010 stays open.
- [Anonymous callers behind one proxy share an address] → authenticated callers do not. The remote address is the servlet's, not a forwarded header.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Burst, other key, next window | unit | `ApiRequestQuotaTest` |
| Filter returns 429 and skips the chain | unit | `ApiRequestQuotaTest` |

TDD: the test did not compile until the quota class existed.

## Regression Strategy

- Quota test and `SecurityAndCorsConfigTest`.
- `python3 -m unittest security.tests.test_threat_register`
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI. The default ceiling is above the local suite's call rate.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: the quota unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
