> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The SSE events route in `app.ts` uses `reply.hijack()` to bypass Fastify's response lifecycle
and pipe the upstream stream directly. The `onSend` hook that sets security headers is never
invoked for hijacked responses, so the stream lacks CSP, X-Content-Type-Options, Referrer-Policy
and Cache-Control.

## Goals / Non-Goals

**Goals:**
- The SSE events stream response carries the same security headers as all other dashboard responses.
- No duplication: one shared `SECURITY_HEADERS` constant used by both the `onSend` hook and the events route.

**Non-Goals:**
- Changing the CSP directive values.
- Adding security headers to other routes (they already get them from `onSend`).

## Decisions

1. **Shared constant over function** — `SECURITY_HEADERS` is a `Record<string, string>` constant.
   A function would add indirection for a static value. The constant is spread into `writeHead()`
   for the SSE route and iterated in the `onSend` hook.
2. **`reply.raw.writeHead()` for SSE** — After `reply.hijack()`, Fastify's `reply.header()` no longer
   works. The only way to set headers is `reply.raw.writeHead()`, which we already use for the
   SSE-specific headers. Spreading `SECURITY_HEADERS` there merges both sets.

## Riesgos / Trade-offs

- [Risk] If `SECURITY_HEADERS` is later modified at runtime, both consumers see the change. → Not a risk:
  the object is `const` and never mutated.
- [Risk] Header collision if SSE-specific headers overlap with security headers. → `Cache-Control` appears
  in both; the spread order (`...SECURITY_HEADERS` first, then SSE headers) lets the SSE-specific value
  win, which is `"no-store"` — same value, so no conflict.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| SSE stream carries security headers | integration | `framework/ai-sdlc/factory-dashboard/tests/server.test.ts` |

- New test: `"carries security headers on the SSE stream response"` — starts the app with a mocked fetcher
  that returns a `text/event-stream`, signs in, hits the events route, and asserts all five headers
  (CSP, x-content-type-options, referrer-policy, cache-control, x-accel-buffering).
- Coverage impact: Vitest; existing tests already cover the events route logic. This test adds the header assertions.

## Regression Strategy

- Existing tests affected: none — the behavior change is additive (headers were missing, now present).
- Full suite command: `cd framework/ai-sdlc/factory-dashboard && npx vitest run`
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. This is a server-side header fix.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `curl -I` the events endpoint should show the security headers.

## Rollback Strategy

- Revert safe: yes — reverting the commit restores the previous `onSend` hook and removes the spread from `writeHead()`.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none (headers are additive)

## Open Questions

None.
