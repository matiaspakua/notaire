## Context

#1463 completed the server log → Loki → Tempo path and deferred client beacons
to #1483. Browser `logger.error` from `api-client` only hits the browser console;
Promtail never sees it. Epic #1461 still needs client errors in Loki for CU77.

## Goals / Non-Goals

**Goals:**

- `POST /api/client-errors` validates level/message/size and rate-limits per client.
- Accepted entries become structured JSON logs with `service: "notaire-frontend"`.
- Guard/unit tests cover malformed, oversized, unknown fields, and rate limit.
- Monitoring README documents the endpoint and a LogQL example.

**Non-Goals:**

- Closing epic #1461 or implementing #1469 live E2E proof.
- Distributed rate limiting across replicas.
- Full UI Error Boundary product UX.
- CHANGELOG while serialize prefers holding ops-only paths.

## Decisions

1. **Route outside `/api/v1/**`.** Catch-all BFF proxies to Spring; beacon stays
   `frontend/src/app/api/client-errors/route.ts` like `/api/metrics`.
   - Alternative: Spring controller — rejected (adds CORS/auth surface; logs should
     stay on the frontend container that owns browser context).

2. **Allowlist + hard caps.** Accept only `level` (`ERROR`|`WARN`), `message`
   (required, ≤1024), optional `name` (≤128), `path` (pathname only, ≤256),
   `stack` (≤4096). Raw body ≤8192. Unknown keys → 400. Oversized → 413/400.
   - Alternative: accept any JSON — rejected (log injection / abuse).

3. **In-memory rate limit keyed by client IP.** Default 30 requests / 60s window
   using `x-forwarded-for` first hop, else `x-real-ip`, else `"unknown"`. 429 when
   exceeded. Single-instance posture matches ADR-018 login lockout.
   - Alternative: Redis — rejected for local/dev scope of this slice.

4. **Response 204 with empty body.** Do not echo payload. Log via `logger` with a
   `client_error …` message prefix so Promtail’s `output.source: message` still
   yields a queryable line.

5. **Optional browser helper.** `reportClientError` POSTs JSON with `keepalive`
   / `fetch`; wire `api-client` failure path to call it (best-effort, never throw).

## Riesgos / Trade-offs

- [Unauthenticated write] → Mitigated by allowlist, caps, rate limit; no PII fields.
- [Per-pod rate limit] → Acceptable for local/single replica; documented.
- [Stack traces in logs] → Truncated; never promoted to Loki labels.
- [Log injection via message] → Cap length; structured logger JSON-encodes the line.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Valid POST logs `service=notaire-frontend` | unit | `client-error-beacon.test.ts` |
| Missing message / bad level / bad JSON | unit | same |
| Oversized body or message | unit | same |
| Unknown fields rejected | unit | same |
| Rate limit returns 429 | unit | same |
| Log line has no cookie/Authorization/query | unit | same |

Red Vitest first, then implement lib + route.

## Regression Strategy

`cd frontend && npm test` for unit suite. No backend/Maven. No infra compose
change; `infra/verify.sh` unchanged.

## Playwright Strategy

n/a — no product UI surface in this slice. Optional reporter is library-only;
Playwright path filters should skip unless frontend unit CI runs (Frontend CI).

## Deployment Strategy

Redeploy/restart the frontend container so the new route is live. No Flyway.
No observability compose change.

## Rollback Strategy

Revert the PR / remove the route. No schema or data migration. Rate-limit map is
process-local and vanishes on restart.
