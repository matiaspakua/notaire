# ADR-020: OpenAPI / Swagger Exposure Policy

## Status
Accepted

## Context
`OpenApiConfig` publishes a full OpenAPI 3.0 spec (via springdoc) at
`/v3/api-docs` and an interactive "try it out" console at `/swagger-ui.html`
/ `/swagger-ui/**`. Exposing an interactive console that lets anonymous
visitors execute real requests against a live API is a materially different
risk than exposing the spec alone, so the exposure decision needed to be
environment-aware rather than always-on or always-off.

## Decision
Swagger UI and the raw OpenAPI spec are **publicly reachable in every
environment except production**, where they are denied outright (issue #671).

### Implementation
`SecurityAndCorsConfig.defaultSecurityFilterChain()` branches on
`isProduction()`:
- **Non-production** (`dev`, `test`, default): `/swagger-ui/**`,
  `/v3/api-docs/**`, `/swagger-ui.html` are `permitAll()` — no
  authentication required, matching the rest of the API's current
  coarse-grained-only authorization model (see ADR-008 / SAD §8.1).
- **Production**: the same paths are `denyAll()` — neither the spec nor the
  interactive console is reachable at all, regardless of credentials.

`OpenApiConfig` itself declares two servers (`localhost:8080` dev,
`localhost:8081` Docker local) and documents the uniform error-response
shape (`{error, message, timestamp, path}` — see ADR-010) directly in the
spec's `Info` description and reusable `400/404/409/500` response
components, so consumers reading the spec see the real contract, not a
generic one.

## Options Considered
- **Always public (including production)**: Rejected — exposes the full API
  surface and a live mutation console to anonymous internet visitors; the
  "try it out" button can execute real `POST`/`PUT`/`DELETE` calls, not just
  document them.
- **Always require authentication (even in dev)**: Rejected — adds friction
  for the primary consumers of Swagger UI (developers exploring the API
  locally, QA writing Bruno collections) without a corresponding security
  benefit in non-production environments.
- **Gate behind a feature flag / separate profile-specific bean**: Rejected
  as unnecessary complexity — a single `isProduction()` branch inside the
  existing filter chain achieves the same outcome with less surface area.

## Consequences
- **Pros**: Zero-friction API discovery for developers/QA locally and in
  CI/test environments; production has no discoverable API surface via
  Swagger, reducing recon value for an attacker.
- **Cons**: Production operators lose the interactive spec as a live
  reference — must consult the checked-in
  [REST API reference](../203-design/REST-API-REFERENCE.md) or a
  non-production environment instead. `isProduction()` is a single string
  comparison against `app.environment`; misconfiguring that property in a
  production deployment would silently re-expose Swagger.
