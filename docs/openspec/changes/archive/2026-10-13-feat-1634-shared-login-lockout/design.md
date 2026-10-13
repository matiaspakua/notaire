> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`LoginAttemptService` is a Spring component constructed with the two `security.login.*` values; `UserController` calls `isLocked`, `onLoginFailed` and `onLoginSucceeded`. Existing unit tests build it with a fake clock.

## Goals / Non-Goals

**Goals:** lockout that holds across replicas and restarts with the same threshold and duration.
**Non-Goals:** IP-based limiting; Redis; changing the threshold.

## Decisions

1. **PostgreSQL table, not Redis.** No new infrastructure; the database is already shared by every replica.
2. **Portable JPA with a row lock** (`PESSIMISTIC_WRITE`) instead of a PostgreSQL-only upsert, so the same code runs on the H2 test profile; an insert race retries once.
3. **`REQUIRES_NEW` writes.** Login failures must persist even if the surrounding request rolls back, and a read-only outer transaction must not break them (the lesson of #1635).
4. **Idle counters expire after one lock duration**, which gives the previously implicit window an explicit bound.
5. **In-memory store kept** for the existing fake-clock unit tests.

## Riesgos / Trade-offs

- [A database outage blocks logins] → login already needs the database.
- [Row contention on one hot username during an attack] → serialised by design; that is the correct behaviour.

## Testing Strategy

`LoginAttemptSharedStoreIntegrationTest` (two services, one store, restart, success, purge, columns) written first; the existing `LoginAttemptServiceTest` and `LoginRateLimitIntegrationTest` stay green; database check `70_login_attempts.sql` on real PostgreSQL.

## Regression Strategy

`mvn verify -pl backend-api`; Bruno lockout requests and the Playwright lockout spec through `workspace/stack/native.sh`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

`V48__login_attempts.sql`, `CREATE TABLE IF NOT EXISTS`, rollback by dropping the table.

## Open Questions

None.
