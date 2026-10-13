# Fix the CSRF regression: Bearer-token exemption and browser double-submit

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1620 |
| Use Case | CU78 – Security and Compliance |
| Branch | `fix/1620_csrf-bearer-exemption` |
| Gate 1 status | passed — `openspec validate --strict` and `validate-sdlc-plan.sh` |

## Objetivo

PR #1619 enabled CSRF protection on the `/api/**` chain (CU78) but left the filter
in a state where **both** supported clients are rejected with `403` on every
mutating request: Bearer-token clients are not exempt, and the browser
double-submit flow is refused because the default `XorCsrfTokenRequestAttributeHandler`
expects a masked value in the `X-XSRF-TOKEN` header while the client echoes the raw
`XSRF-TOKEN` cookie. `main` is red in `Playwright E2E — Full Suite` (Bruno API tests
and all three UI E2E shards) because of it.

## What Changes

- Exempt requests carrying `Authorization: Bearer …` from CSRF validation; the
  credential is attached explicitly by the client and cannot be replayed
  cross-site, so CSRF does not apply (Bruno API tests, E2E seed helpers, mobile,
  server-to-server).
- Add `SpaCsrfTokenRequestHandler` (the Spring Security reference recipe) so a
  header-supplied `X-XSRF-TOKEN` resolves verbatim while server-rendered values
  keep the XOR mask. The frontend `buildHeaders()` contract
  (`frontend/src/lib/api-client.ts`) now matches the backend. Spring Security 7
  ships this recipe as `CsrfConfigurer.spa()`, so the chain uses the framework
  API instead of a local copy.
- Stop Spring Security from replacing the `XSRF-TOKEN` cookie on every
  authenticated request (`CsrfAuthenticationStrategy` →
  `sessionAuthenticationStrategy`). Rotation is a session-fixation defence for a
  session-scoped token; this nonce is stateless, so rotating it only churns the
  cookie and races the client's read-then-send (a dashboard firing parallel
  requests left the echoed header and the cookie disagreeing → 403).
- Materialize the token in `CsrfCookieFilter` **before** `filterChain.doFilter`,
  so the `XSRF-TOKEN` cookie is written even when the handler commits a large
  body (PDF downloads through `apiGetBytes`).
- Make the session-cookie write additive. `POST /api/v1/users/login` and
  `POST /api/v1/users/logout` wrote their `Set-Cookie` through the
  `ResponseEntity` header map, which replaces every `Set-Cookie` already on the
  response. The token cookie only survived because it was materialized *after*
  the controller had run; materializing it before the chain (needed for committed
  bodies) exposed that ordering trap. Both endpoints now append their cookie to
  the `HttpServletResponse`, so delivery no longer depends on the order.
- Replace the `csrf()`-style test shortcuts: remove
  `SecurityMockMvcRequestPostProcessors.csrf()` (it reflectively replaces the
  shared `CsrfFilter` token repository, disabling enforcement for the rest of the
  JVM) and use real cookies/tokens instead.
- Correct the permanent documentation and the `TS-0044` E2E spec header, which
  still described CSRF as disabled.

No endpoint signatures, payloads or database objects change. **BREAKING** for no
client: the change restores the two documented client contracts.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An unsafe request authenticated by the session cookie must echo the raw `XSRF-TOKEN` cookie in `X-XSRF-TOKEN` | CU78 | Made explicit |
| A request carrying an explicit `Authorization: Bearer` token is not CSRF-protected | CU78 / OWASP CSRF guidance | New |
| Login and logout stay reachable without a CSRF token | CU78 / CU84 (login) | Made explicit |
| Every response of the `/api/**` chain issues the script-readable `XSRF-TOKEN` cookie, including login/logout | CU78 | New |
| The `XSRF-TOKEN` nonce is not replaced while the session cookie is unchanged | CU78 | New |

## Capabilities

### New Capabilities

- `csrf-browser-double-submit`: the double-submit contract between the Next BFF
  browser client and the API chain, plus the Bearer-token exemption and the
  session-bootstrap exemptions.

### Modified Capabilities

- none — the requirement introduced by the still-open
  `code-scanning-remediation` change (`httponly-jwt-cookie`) is completed here
  without editing that in-flight delta.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `SecurityAndCorsConfig` (Bearer exemption, `SpaCsrfTokenRequestHandler`, cookie filter order), `UserController` (append the session cookie instead of replacing `Set-Cookie`), CSRF integration tests |
| `frontend` | no | The client already reads `XSRF-TOKEN` and sends `X-XSRF-TOKEN` (`src/lib/api-client.ts`); no source change needed |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | `playwright-e2e.yml` turns green again with no workflow edit |

### Surface area

- Entities: none.
- Endpoints: no signature change. Behaviour changes on every mutating
  `/api/**` endpoint: Bearer requests no longer require a CSRF token, and
  cookie requests now require the `X-XSRF-TOKEN` header. `POST /api/v1/users/login`
  and `POST /api/v1/users/logout` additionally return the `XSRF-TOKEN` cookie
  alongside their existing `Set-Cookie`.
- Database (Flyway `V{n}`): none — no schema change.
- Configuration / `.env`: none. `cors.allowed-headers` already contains
  `X-XSRF-TOKEN`.
- Dependencies: none added.

### Architecture review

Follows the existing architecture: the security policy stays centralized in
`SecurityAndCorsConfig`, the CORS allow-list is unchanged, the Next BFF remains
the only browser entry point and no credential moves into script-readable
storage (the token cookie is not a credential — it is a random nonce). No ADR is
required: this is a defect fix inside the `#1618` design, not a new architecture
decision.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | Replace "CSRF stays disabled" with the double-submit posture and the Bearer/login exemptions |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | Replace the "CSRF does not apply / disabled" section with the configured policy and the Semgrep rule that forbids disabling it |
| `testing/e2e/tests/TS-0044-security-csrf-cors-qa.spec.ts` | Header comment describing the enforced posture + new double-submit/exemption tests |
| `CHANGELOG.md` | `[Unreleased]` → `fix(security): restore CSRF protection for browser and Bearer clients` |
| `CU78 – Security and Compliance` | Already covers this posture; no edit needed (verify wording) |

## Out of Scope

- Archiving the 32 stale OpenSpec changes and unarchived `code-scanning-remediation`
  (#1265): out of scope here; `main` archived and synced them in #1626 (`98bcb609`)
  while this branch was open.
- The actuator and default filter chains keep `csrf(withDefaults())`; their only
  reachable surfaces are GET/`permitAll` endpoints, so no browser double-submit
  applies there.
- `frontend/src/lib/api-client.ts` keeps reading `document.cookie` without an SSR
  guard: every consumer is a `"use client"` component and no query runs during
  SSR, so there is no reachable crash (latent-only risk).
- Rotating the `XSRF-TOKEN` cookie on logout.
