# SSE events stream carries security headers

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2405 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2405_sse_security_headers` |
| Gate 1 status | passed |

## Objetivo

The SSE events route (`GET /api/workspaces/:ws/factory/events`) uses `reply.hijack()` to pipe the upstream
`text/event-stream` through. Hijacking bypasses Fastify's `onSend` hook, so the CSP, X-Content-Type-Options,
Referrer-Policy and Cache-Control headers set on every other response are missing from the SSE stream.
This fix ensures the stream response carries the same security headers.

## What Changes

- Extract the four security headers into a shared `SECURITY_HEADERS` constant in `app.ts`.
- The `onSend` hook iterates over `SECURITY_HEADERS` instead of calling `reply.header()` inline.
- The SSE events route spreads `SECURITY_HEADERS` into `reply.raw.writeHead()` before piping the stream,
  merged with SSE-specific headers (`Content-Type: text/event-stream`, `X-Accel-Buffering: no`).
- Add a server test asserting all security headers are present on the SSE response.

No **BREAKING** change: the response shape is unchanged; headers are additive.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every HTTP response from the dashboard, including hijacked SSE streams, carries the standard security headers (CSP, X-Content-Type-Options, Referrer-Policy, Cache-Control) | CU76 | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `factory-dashboard`: adds a requirement that the SSE events stream carries the same security headers as other responses.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `framework/ai-sdlc` | yes | `factory-dashboard/src/server/app.ts`, `factory-dashboard/tests/server.test.ts`, `CHANGELOG.md` |

### Surface area

- Entities: none
- Endpoints: `GET /api/workspaces/:ws/factory/events` (headers only, no shape change)
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `framework/ai-sdlc/factory-dashboard/src/server/app.ts`
- `framework/ai-sdlc/factory-dashboard/tests/server.test.ts`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/fix-2405-sse-security-headers/**`
- `docs/openspec/specs/factory-dashboard/spec.md`

### Architecture review

No architectural change. The fix follows the existing pattern: a shared constant consumed by both the hook
and the route. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/openspec/specs/factory-dashboard/spec.md` | Add requirement and scenario for SSE security headers |
| `framework/ai-sdlc/CHANGELOG.md` | Fixed entry for #2405 |

## Out of Scope

- Other hijacked or streaming routes (none exist currently).
- Frontend Next.js security headers (covered by `frontend-security-headers` spec).
