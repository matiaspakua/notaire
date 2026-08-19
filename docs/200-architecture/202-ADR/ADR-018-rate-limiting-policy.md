# ADR-018: Rate-Limiting Policy

## Status
Accepted

## Context
The system has no general-purpose API rate limiter (no Bucket4j, no gateway
throttling, no `RateLimiter` beans anywhere in `backend-api`). The only
throttling-like behavior is `LoginAttemptService`, an in-memory,
single-instance lockout keyed by username. This ADR records that as a
deliberate, scoped decision rather than an oversight, and states what is
explicitly out of scope today.

## Decision
Implement **login-lockout only**, not general API rate limiting.

### `LoginAttemptService`
- In-memory `ConcurrentHashMap<String, AttemptRecord>` keyed by username
  (not IP) — no database or distributed cache involved.
- **5 failed attempts within 15 minutes** locks that username out; the login
  endpoint returns `429 Too Many Requests` while locked.
- Single-instance only: state is not shared across replicas. Horizontally
  scaling the backend (QS-09 in the SAD) would let an attacker bypass the
  lockout by hitting a different instance — acceptable today given the
  current single-instance deployment, but a real limitation the moment
  `docker-compose up --scale backend=N` (or any multi-replica deployment)
  is used.

### Explicitly out of scope
- No per-IP or per-endpoint rate limiting on any other `/api/v1/**` route.
- No API gateway or reverse-proxy throttling layer (no Nginx/Envoy/Kong in
  front of the backend).
- No distributed rate-limit store (Redis, etc.) — would be required before
  `LoginAttemptService`'s lockout is meaningful across multiple replicas.

## Options Considered
- **Bucket4j / Resilience4j RateLimiter on all endpoints**: Deferred —
  no observed abuse pattern beyond credential-stuffing on `/login` today;
  adding blanket rate limiting without real traffic data risks over-throttling
  legitimate bulk operations (e.g. report generation, list endpoints used by
  the dashboard).
- **API gateway-level throttling (Nginx `limit_req`, Kong, etc.)**:
  Deferred — no gateway currently sits in front of the backend; introducing
  one purely for rate limiting is a bigger infrastructure change than the
  current threat model justifies.

## Consequences
- **Pros**: Simple, zero extra infrastructure, addresses the one concrete
  threat (login brute-forcing) that was actually identified.
- **Cons**: No protection against scraping/abuse of other endpoints
  (list/report endpoints); lockout state doesn't survive backend restarts or
  scale-out. Revisit this ADR before any multi-replica production deployment
  or if abuse of non-login endpoints is observed.
