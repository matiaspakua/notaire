> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`ApiRequestQuota` kept a `ConcurrentHashMap` in the filter's JVM. `LoginAttemptService` already uses a shared table and an atomic UPDATE. The quota follows that shape.

## Goals / Non-Goals

**Goals:**

- Two quota objects on one table reject a burst that was counted by the other.
- The table stores only the bucket key, the window start, and the count.
- The existing in-memory tests keep the same window rules.

**Non-Goals:**

- A Redis limiter.
- Changing the default 2000 requests / 60 seconds.
- Blocking deliberate failures of a known username.

## Decisions

`JpaApiRateWindowStore` advances the row with one UPDATE and inserts when the key is new. A unique-key race retries once, as the login store does. Writes use a new transaction so a later rollback of the HTTP request does not drop the count. Expired rows are deleted when a new call arrives. Unit tests keep `InMemoryApiRateWindowStore`.

## Riesgos / Trade-offs

- [Every API call writes a row] → the window is one row per caller, and expired rows are deleted. The default ceiling is 2000, so normal traffic does not fill the table.
- [The bucket key is a username] → the same class of identifier `login_attempts` already stores. The table has no request body.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| In-memory burst, retry-after, and the next window | unit | `ApiRequestQuotaTest` |
| A second instance sees the burst, and a new window is allowed | integration | `ApiQuotaSharedStoreIntegrationTest` |
| Columns are only the key, the start, and the count | integration | same |

TDD: the integration test failed to compile because `JpaApiRateWindowStore` did not exist.

## Regression Strategy

- `mvn -pl backend-api -Dtest=ApiRequestQuotaTest,ApiQuotaSharedStoreIntegrationTest test`
- `bash workspace/sdlc/preflight.sh --full` before push.

## Playwright Strategy

n/a — no product UI. The default ceiling is above the E2E traffic.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway V55 creates `api_rate_windows`.
- Existing single-instance installs start sharing as soon as a second instance uses the same database.

## Rollback Strategy

- Revert safe: yes. Drop `api_rate_windows` if the migration must be undone.
- Database rollback: `DROP TABLE api_rate_windows`.
