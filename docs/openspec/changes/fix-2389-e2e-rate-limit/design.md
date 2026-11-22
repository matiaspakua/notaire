> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Facts measured on `main`:

- **Rate limit**: `ApiRateLimitFilter` (lines 22–55) delegates to `ApiRequestQuota`
  which calls `ApiRateWindowStore.tryAcquire(key, maxRequests, windowSeconds)`. Two
  implementations: `InMemoryApiRateWindowStore` (default profile) and
  `JpaApiRateWindowStore` (production / test-h2 profile, `@Primary`).
- **JPA store**: `JpaApiRateWindowStore.tryAcquire()` (lines 33–68) loads the window
  row; if absent, calls `saveAndFlush` on a new `ApiRateWindow`, then `UPDATE ... SET
  request_count = request_count + 1`. The `saveAndFlush` collides on
  `api_rate_windows_pkey` (PK = `bucket_key`) when two threads arrive first
  simultaneously. It retries only twice (`DataIntegrityViolationException` catch).
- **Repository**: `ApiRateWindowRepository` in the legacy `repository` package
  (`backend-api/src/main/java/com/licensis/notaire/repository/`) — Spring Data JPA,
  methods: `findByBucketKey`, `advance`, `purgeExpired`.
- **Entity**: `ApiRateWindow` JPA entity with fields `bucketKey`, `windowStart`,
  `requestCount`. Public constructor currently used by `JpaApiRateWindowStore`.
- **Table**: `api_rate_windows` created by V55, PK on `bucket_key`.
- **Test profile**: `test-h2` uses H2 in `MODE=PostgreSQL`, which supports
  `ON CONFLICT ... DO NOTHING`.
- **E2e stack**: `workspace/stack/start.sh` reads `docker-compose.yml`. Backend
  service has no `NOTAIRE_API_RATE_LIMIT_MAX` env var — falls back to
  `application.properties` default of 2000.
- **Preflight**: `workspace/sdlc/preflight.sh` section "Server-backed suites"
  (line ~439) starts the stack via `workspace/stack/start.sh --no-admin` when
  `MODE_FULL=1`.

## Goals / Non-Goals

**Goals:**

- Full Playwright run on e2e stack never hits 429 (limit 20 000 >> observed peak 2025).
- Every 429 is logged at WARN with enough context to diagnose (key type, path) but
  zero personal data (no username, no IP).
- Concurrent first requests on the same key are all counted — no exception, no lost
  request.

**Non-Goals:**

- Changing the production default (2000) or `docker-compose.prod.yml`.
- Changing the rate-limit algorithm, window size, or keying strategy.
- Migrating `ApiRateWindowRepository` out of the legacy `repository` package.

## Decisions

1. **`ON CONFLICT (bucket_key) DO NOTHING` for window creation.** The existing
   `saveAndFlush` + retry loop is fragile: two threads can both read null, both try
   to insert, one fails, retries — but the retry window is narrow (2 attempts) and
   the UPDATE may also collide. A native `INSERT ... ON CONFLICT (bucket_key) DO
   NOTHING` is atomic and idempotent — exactly one row is created regardless of how
   many threads race. H2 in `MODE=PostgreSQL` supports this syntax, so the test-h2
   profile exercises the same path. The new repository method
   `insertWindowOnConflict` (`@Modifying @Query(nativeQuery = true)`) takes
   `(bucketKey, windowStart)`. After the INSERT, the existing `UPDATE` advances the
   counter. *Why not `saveOrMerge`:* `saveOrMerge` would still flush the INSERT
   concurrently, producing the same PK violation. *Why not `@Lock(PESSIMISTIC_WRITE)`
   on the SELECT:* adds lock contention on every request; `ON CONFLICT` is lock-free
   for the common case.

2. **429 log carries key type and path only.** The filter already knows whether the
   key is a user key or an IP key (from `ApiRequestQuota`). It also has access to
   `request.getRequestURI()`. The log line uses SLF4J parameterized format:
   `log.warn("Rate limit exceeded (keyType={}, path={})", keyType, path)`. The
   username and IP are never interpolated. *Why WARN not ERROR:* 429 is expected
   behavior under load; ERROR would pollute alerts.

3. **E2e limit via env var, not code.** `preflight.sh` exports
   `NOTAIRE_API_RATE_LIMIT_MAX=20000` before the stack start, but only when the
   variable is not already set (`export NOTAIRE_API_RATE_LIMIT_MAX=${NOTAIRE_API_RATE_LIMIT_MAX:-20000}`).
   `docker-compose.yml` backend service passes
   `NOTAIRE_API_RATE_LIMIT_MAX: ${NOTAIRE_API_RATE_LIMIT_MAX:-2000}`. This way:
   (a) manual `docker-compose up` still defaults to 2000; (b) preflight gets 20000;
   (c) the user can override by setting the env before calling preflight. Production
   files are untouched.

## Riesgos / Trade-offs

- [H2 `ON CONFLICT DO NOTHING` syntax] → H2 in `MODE=PostgreSQL` supports it; the
  test-h2 profile already uses this mode. Verified in the integration test.
- [20000 is still finite — a future larger suite could exceed it] → 20000 is 10× the
  observed peak (2025). If it becomes tight again, the env var makes it trivial to
  raise further without code changes.
- [Removing the retry loop changes error semantics] → The retry loop was masking the
  race, not solving it. With `ON CONFLICT DO NOTHING`, the race is resolved at the
  DB level — no retry needed. The test proves 32 concurrent threads all succeed.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| 429 logged with key type and path, no PII (user key) | unit | `ApiRateLimitFilterTest` — user-key scenario |
| 429 logged with key type and path, no PII (ip key) | unit | `ApiRateLimitFilterTest` — ip-key scenario |
| Concurrent first requests all counted | integration | `JpaApiRateWindowStoreConcurrencyTest` (test-h2, `@SpringBootTest`) |
| docker-compose.yml has env var with default 2000 | Python unittest | `workspace/tests/test_e2e_rate_limit_env.py` |
| preflight.sh exports limit >= 20000 | Python unittest | `workspace/tests/test_e2e_rate_limit_env.py` |
| prod default stays 2000 | Python unittest | `workspace/tests/test_e2e_rate_limit_env.py` |

- New unit tests: `ApiRateLimitFilterTest` — captures WARN log via Logback
  `ListAppender`, asserts key type and path present, username and IP absent.
- New integration test: `JpaApiRateWindowStoreConcurrencyTest` — 32 threads released
  by `CountDownLatch`, same new key, `maxRequests` high; asserts count == 32 via
  `JdbcTemplate` read of `api_rate_windows`, no exceptions thrown. Uses
  `REQUIRES_NEW` (store already does), no `@Transactional` on the test.
- Python test: `workspace/tests/test_e2e_rate_limit_env.py` — stdlib + pytest,
  follows style of `test_ci_ddl_auto_none.py`.

- Coverage impact: new code at ≥ 80 %; JaCoCo ratchet held.

## Regression Strategy

- Existing tests affected: none expected — the filter's behavior (429 response) is
  unchanged; the store's behavior (counting) is the same, just race-free.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk: none; changes are confined to security package and the
  existing repository class.

## Playwright Strategy

n/a — no UI surface. The change affects backend rate-limiting internals and e2e
infrastructure configuration.

## Deployment Strategy

- Flyway migration required: no.
- Deployment order / coupling: code and config ship together; no migration.
- Configuration or `.env` keys to add: none (uses existing `NOTAIRE_API_RATE_LIMIT_MAX`).
- Feature flag: no.
- Smoke test after deploy (Gate 5): verify backend starts, rate limit still works
  at 2000 (production default), and the e2e stack starts with 20000.

## Rollback Strategy

- Revert safe: yes. Reverting the commit restores the previous filter log, the retry
  loop, and the default e2e limit.
- Database rollback: none needed — no schema change.
- Data written under the new behavior after revert: none.
- Blast radius if rollback is delayed: limited to e2e test reliability and log
  format; production behavior unchanged.

## Migration Plan

1. Gate 1 — this OpenSpec change (spec only, no code).
2. Gate 2 — write failing tests (unit for 429 log, integration for concurrency, Python for env).
3. Gate 3 — implement the three fixes (env var in preflight + docker-compose, 429 log, ON CONFLICT).
4. Full suite + preflight.
5. PR, review, merge.

## Open Questions

None.
